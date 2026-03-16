package com.freshmart.web.servlet.customer;

import com.freshmart.config.AppConstants;
import com.freshmart.entity.User;
import com.freshmart.enums.Gender;
import com.freshmart.enums.Role;
import com.freshmart.service.CustomerProfileService;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class CustomerProfileServletIntegrationTest {

    @Test
    void doGet_shouldLoadProfileUserAndForward() throws Exception {
        User sessionUser = newUser(11L, "session-user");
        User profileUser = newUser(11L, "fresh-profile");
        profileUser.setPhone("0912345678");

        CustomerProfileServlet servlet = new CustomerProfileServlet();
        StubCustomerProfileService profileService = new StubCustomerProfileService();
        profileService.userToReturn = profileUser;
        replaceField(servlet, "customerProfileService", profileService);

        SessionContext sessionCtx = new SessionContext();
        sessionCtx.attrs.put(AppConstants.SESSION_USER, sessionUser);
        HttpSession session = newSessionProxy(sessionCtx);

        RequestContext requestCtx = new RequestContext();
        requestCtx.contextPath = "/freshmart";

        servlet.doGet(newRequestProxy(requestCtx, session), newResponseProxy(new ResponseContext()));

        assertEquals(11L, profileService.lastUserId);
        assertEquals("/WEB-INF/jsp/customer/profile.jsp", requestCtx.forwardedPath);
        assertSame(profileUser, requestCtx.attrs.get("profileUser"));
    }

    @Test
    void doPost_shouldUpdateProfileAndRefreshSession() throws Exception {
        User sessionUser = newUser(21L, "old-name");
        User updatedUser = newUser(21L, "new-name");
        updatedUser.setGender(Gender.FEMALE);
        updatedUser.setDob(LocalDate.of(2000, 1, 2));
        updatedUser.setPhone("0911222333");
        updatedUser.setAddress("HCM City");

        CustomerProfileServlet servlet = new CustomerProfileServlet();
        StubCustomerProfileService profileService = new StubCustomerProfileService();
        profileService.updatedUserToReturn = updatedUser;
        replaceField(servlet, "customerProfileService", profileService);

        SessionContext sessionCtx = new SessionContext();
        sessionCtx.attrs.put(AppConstants.SESSION_USER, sessionUser);
        HttpSession session = newSessionProxy(sessionCtx);

        RequestContext requestCtx = new RequestContext();
        requestCtx.contextPath = "/freshmart";
        requestCtx.params.put("fullName", "  New Name  ");
        requestCtx.params.put("gender", "FEMALE");
        requestCtx.params.put("dob", "2000-01-02");
        requestCtx.params.put("phone", "0911222333");
        requestCtx.params.put("address", "HCM City");

        servlet.doPost(newRequestProxy(requestCtx, session), newResponseProxy(new ResponseContext()));

        assertEquals(21L, profileService.lastUserId);
        assertEquals("  New Name  ", profileService.lastFullName);
        assertEquals("FEMALE", profileService.lastGender);
        assertEquals("2000-01-02", profileService.lastDob);
        assertEquals("0911222333", profileService.lastPhone);
        assertEquals("HCM City", profileService.lastAddress);
        assertSame(updatedUser, sessionCtx.attrs.get(AppConstants.SESSION_USER));
        assertSame(updatedUser, requestCtx.attrs.get("profileUser"));
        assertEquals("Cập nhật hồ sơ thành công.", requestCtx.attrs.get("successMessage"));
        assertEquals("/WEB-INF/jsp/customer/profile.jsp", requestCtx.forwardedPath);
    }

    @Test
    void doPost_shouldKeepFormDataWhenServiceRejectsInput() throws Exception {
        User sessionUser = newUser(31L, "old-name");

        CustomerProfileServlet servlet = new CustomerProfileServlet();
        StubCustomerProfileService profileService = new StubCustomerProfileService();
        profileService.updateError = new IllegalArgumentException("Số điện thoại không hợp lệ.");
        replaceField(servlet, "customerProfileService", profileService);

        SessionContext sessionCtx = new SessionContext();
        sessionCtx.attrs.put(AppConstants.SESSION_USER, sessionUser);
        HttpSession session = newSessionProxy(sessionCtx);

        RequestContext requestCtx = new RequestContext();
        requestCtx.contextPath = "/freshmart";
        requestCtx.params.put("fullName", "Bad Input");
        requestCtx.params.put("gender", "MALE");
        requestCtx.params.put("dob", "2001-03-04");
        requestCtx.params.put("phone", "abc");
        requestCtx.params.put("address", "Test Address");

        servlet.doPost(newRequestProxy(requestCtx, session), newResponseProxy(new ResponseContext()));

        assertEquals("Số điện thoại không hợp lệ.", requestCtx.attrs.get("errorMessage"));
        assertSame(sessionUser, requestCtx.attrs.get("profileUser"));
        assertEquals("/WEB-INF/jsp/customer/profile.jsp", requestCtx.forwardedPath);

        @SuppressWarnings("unchecked")
        Map<String, String> formData = (Map<String, String>) requestCtx.attrs.get("formData");
        assertEquals("Bad Input", formData.get("fullName"));
        assertEquals("MALE", formData.get("gender"));
        assertEquals("2001-03-04", formData.get("dob"));
        assertEquals("abc", formData.get("phone"));
        assertEquals("Test Address", formData.get("address"));
    }

    private User newUser(Long id, String fullName) throws Exception {
        User user = new User("customer01", "customer01@freshmart.local", "hash", Role.CUSTOMER);
        setField(user, "id", id);
        user.setFullName(fullName);
        return user;
    }

    private void replaceField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private HttpSession newSessionProxy(SessionContext ctx) {
        return (HttpSession) Proxy.newProxyInstance(
                HttpSession.class.getClassLoader(),
                new Class[]{HttpSession.class},
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "getAttribute":
                            return ctx.attrs.get(args[0]);
                        case "setAttribute":
                            ctx.attrs.put((String) args[0], args[1]);
                            return null;
                        case "removeAttribute":
                            ctx.attrs.remove(args[0]);
                            return null;
                        case "getId":
                            return "customer-profile-session";
                        default:
                            return defaultValue(method.getReturnType());
                    }
                }
        );
    }

    private HttpServletRequest newRequestProxy(RequestContext ctx, HttpSession session) {
        return (HttpServletRequest) Proxy.newProxyInstance(
                HttpServletRequest.class.getClassLoader(),
                new Class[]{HttpServletRequest.class},
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "getParameter":
                            return ctx.params.get(args[0]);
                        case "setAttribute":
                            ctx.attrs.put((String) args[0], args[1]);
                            return null;
                        case "getAttribute":
                            return ctx.attrs.get(args[0]);
                        case "getRequestDispatcher":
                            return newDispatcherProxy(ctx, (String) args[0]);
                        case "getSession":
                            return session;
                        case "getContextPath":
                            return ctx.contextPath;
                        default:
                            return defaultValue(method.getReturnType());
                    }
                }
        );
    }

    private RequestDispatcher newDispatcherProxy(RequestContext ctx, String path) {
        return (RequestDispatcher) Proxy.newProxyInstance(
                RequestDispatcher.class.getClassLoader(),
                new Class[]{RequestDispatcher.class},
                (proxy, method, args) -> {
                    if ("forward".equals(method.getName())) {
                        ctx.forwardedPath = path;
                        return null;
                    }
                    return defaultValue(method.getReturnType());
                }
        );
    }

    private HttpServletResponse newResponseProxy(ResponseContext ctx) {
        return (HttpServletResponse) Proxy.newProxyInstance(
                HttpServletResponse.class.getClassLoader(),
                new Class[]{HttpServletResponse.class},
                (proxy, method, args) -> {
                    if ("sendRedirect".equals(method.getName())) {
                        return null;
                    }
                    return defaultValue(method.getReturnType());
                }
        );
    }

    private Object defaultValue(Class<?> returnType) {
        if (!returnType.isPrimitive()) {
            return null;
        }
        if (returnType == boolean.class) return false;
        if (returnType == byte.class) return (byte) 0;
        if (returnType == short.class) return (short) 0;
        if (returnType == int.class) return 0;
        if (returnType == long.class) return 0L;
        if (returnType == float.class) return 0f;
        if (returnType == double.class) return 0d;
        if (returnType == char.class) return '\0';
        return null;
    }

    private static class StubCustomerProfileService extends CustomerProfileService {
        private User userToReturn;
        private User updatedUserToReturn;
        private RuntimeException updateError;
        private Long lastUserId;
        private String lastFullName;
        private String lastGender;
        private String lastDob;
        private String lastPhone;
        private String lastAddress;

        @Override
        public User getById(Long userId) {
            this.lastUserId = userId;
            return userToReturn;
        }

        @Override
        public User updateProfile(Long userId, String fullName, String gender, String dob, String phone, String address) {
            this.lastUserId = userId;
            this.lastFullName = fullName;
            this.lastGender = gender;
            this.lastDob = dob;
            this.lastPhone = phone;
            this.lastAddress = address;
            if (updateError != null) {
                throw updateError;
            }
            return updatedUserToReturn;
        }
    }

    private static class SessionContext {
        private final Map<String, Object> attrs = new HashMap<>();
    }

    private static class RequestContext {
        private final Map<String, String> params = new LinkedHashMap<>();
        private final Map<String, Object> attrs = new HashMap<>();
        private String contextPath;
        private String forwardedPath;
    }

    private static class ResponseContext {
    }
}