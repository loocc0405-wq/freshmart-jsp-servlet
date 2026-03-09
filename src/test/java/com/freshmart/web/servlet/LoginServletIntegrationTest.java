package com.freshmart.web.servlet;

import com.freshmart.config.AppConstants;
import com.freshmart.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;

import java.lang.reflect.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LoginServletIntegrationTest {

    @Test
    void doGet_shouldRedirectStaffToProDashboard_whenUserAlreadyInSession() throws Exception {
        LoginServlet servlet = new LoginServlet();

        User user = newUser("STAFF", null);

        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put(AppConstants.SESSION_USER, user);

        HttpSession session = newSessionProxy(sessionAttrs);
        HttpServletRequest request = newRequestProxy(session, "/freshmart");
        AtomicReference<String> redirectedTo = new AtomicReference<>();
        HttpServletResponse response = newResponseProxy(redirectedTo);

        servlet.doGet(request, response);

        assertEquals("/freshmart/pro/dashboard", redirectedTo.get());
    }

    @Test
    void doGet_shouldRedirectProCustomerToProDashboard_whenUserAlreadyInSession() throws Exception {
        LoginServlet servlet = new LoginServlet();

        User user = newUser("CUSTOMER", "PRO");

        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put(AppConstants.SESSION_USER, user);

        HttpSession session = newSessionProxy(sessionAttrs);
        HttpServletRequest request = newRequestProxy(session, "/freshmart");
        AtomicReference<String> redirectedTo = new AtomicReference<>();
        HttpServletResponse response = newResponseProxy(redirectedTo);

        servlet.doGet(request, response);

        assertEquals("/freshmart/pro/dashboard", redirectedTo.get());
    }

    private User newUser(String roleName, String tierName) throws Exception {
        User user = new User();
        setEnumProperty(user, "role", roleName);
        if (tierName != null) {
            setEnumProperty(user, "tier", tierName);
        }
        return user;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void setEnumProperty(Object target, String property, String enumValue) throws Exception {
        String setterName = "set" + Character.toUpperCase(property.charAt(0)) + property.substring(1);

        for (Method m : target.getClass().getMethods()) {
            if (m.getName().equals(setterName) && m.getParameterCount() == 1 && m.getParameterTypes()[0].isEnum()) {
                Class enumType = m.getParameterTypes()[0];
                Object value = Enum.valueOf(enumType, enumValue);
                m.invoke(target, value);
                return;
            }
        }

        Field f = target.getClass().getDeclaredField(property);
        f.setAccessible(true);
        Class enumType = f.getType();
        Object value = Enum.valueOf(enumType.asSubclass(Enum.class), enumValue);
        f.set(target, value);
    }

    private HttpSession newSessionProxy(Map<String, Object> attrs) {
        return (HttpSession) Proxy.newProxyInstance(
                HttpSession.class.getClassLoader(),
                new Class[]{HttpSession.class},
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "getAttribute":
                            return attrs.get(args[0]);
                        case "setAttribute":
                            attrs.put((String) args[0], args[1]);
                            return null;
                        case "removeAttribute":
                            attrs.remove(args[0]);
                            return null;
                        case "invalidate":
                            attrs.clear();
                            return null;
                        case "getId":
                            return "test-session";
                        default:
                            return defaultValue(method.getReturnType());
                    }
                }
        );
    }

    private HttpServletRequest newRequestProxy(HttpSession session, String contextPath) {
        return (HttpServletRequest) Proxy.newProxyInstance(
                HttpServletRequest.class.getClassLoader(),
                new Class[]{HttpServletRequest.class},
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "getSession":
                            return session;
                        case "getContextPath":
                            return contextPath;
                        default:
                            return defaultValue(method.getReturnType());
                    }
                }
        );
    }

    private HttpServletResponse newResponseProxy(AtomicReference<String> redirectedTo) {
        return (HttpServletResponse) Proxy.newProxyInstance(
                HttpServletResponse.class.getClassLoader(),
                new Class[]{HttpServletResponse.class},
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "sendRedirect":
                            redirectedTo.set((String) args[0]);
                            return null;
                        default:
                            return defaultValue(method.getReturnType());
                    }
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
}