package com.freshmart.web.servlet.staff;

import com.freshmart.service.ProductLotService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StaffDeleteLotServletTest {

    @Test
    void doPost_shouldDeleteLotAndSanitizeUnsafeRedirect() throws Exception {
        RecordingProductLotService lotService = new RecordingProductLotService();
        StaffDeleteLotServlet servlet = new StaffDeleteLotServlet(lotService);

        Map<String, String> params = new HashMap<>();
        params.put("lotId", "42");
        params.put("redirect", "https://evil.example/steal");

        Map<String, Object> sessionAttrs = new HashMap<>();
        HttpSession session = newSessionProxy(sessionAttrs);
        AtomicReference<String> redirectedTo = new AtomicReference<>();

        HttpServletRequest request = newRequestProxy(params, session, "/freshmart");
        HttpServletResponse response = newResponseProxy(redirectedTo);

        servlet.doPost(request, response);

        assertEquals(Long.valueOf(42L), lotService.deletedLotId);
        assertEquals("Lô #42 đã được loại bỏ.", sessionAttrs.get("successMessage"));
        assertEquals("/freshmart/staff/inventory", redirectedTo.get());
    }

    @Test
    void doPost_shouldKeepAllowedInventoryReportRedirect() throws Exception {
        RecordingProductLotService lotService = new RecordingProductLotService();
        StaffDeleteLotServlet servlet = new StaffDeleteLotServlet(lotService);

        Map<String, String> params = new HashMap<>();
        params.put("lotId", "77");
        params.put("redirect", "/staff/inventory-report?status=expired");

        Map<String, Object> sessionAttrs = new HashMap<>();
        HttpSession session = newSessionProxy(sessionAttrs);
        AtomicReference<String> redirectedTo = new AtomicReference<>();

        HttpServletRequest request = newRequestProxy(params, session, "/freshmart");
        HttpServletResponse response = newResponseProxy(redirectedTo);

        servlet.doPost(request, response);

        assertEquals("/freshmart/staff/inventory-report?status=expired", redirectedTo.get());
    }

    @Test
    void doPost_shouldStoreBackendErrorWhenDeletionFails() throws Exception {
        RecordingProductLotService lotService = new RecordingProductLotService();
        lotService.deleteFailure = new IllegalStateException("Chỉ được loại bỏ lô đã hết hạn");
        StaffDeleteLotServlet servlet = new StaffDeleteLotServlet(lotService);

        Map<String, String> params = new HashMap<>();
        params.put("lotId", "99");

        Map<String, Object> sessionAttrs = new HashMap<>();
        HttpSession session = newSessionProxy(sessionAttrs);
        AtomicReference<String> redirectedTo = new AtomicReference<>();

        HttpServletRequest request = newRequestProxy(params, session, "/freshmart");
        HttpServletResponse response = newResponseProxy(redirectedTo);

        servlet.doPost(request, response);

        String message = (String) sessionAttrs.get("errorMessage");
        assertTrue(message.contains("Xảy ra lỗi khi xóa lô"));
        assertTrue(message.contains("Chỉ được loại bỏ lô đã hết hạn"));
        assertEquals("/freshmart/staff/inventory", redirectedTo.get());
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
                        default:
                            return defaultValue(method.getReturnType());
                    }
                }
        );
    }

    private HttpServletRequest newRequestProxy(Map<String, String> params, HttpSession session, String contextPath) {
        return (HttpServletRequest) Proxy.newProxyInstance(
                HttpServletRequest.class.getClassLoader(),
                new Class[]{HttpServletRequest.class},
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "getParameter":
                            return params.get(args[0]);
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
                    if ("sendRedirect".equals(method.getName())) {
                        redirectedTo.set((String) args[0]);
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

    private static class RecordingProductLotService extends ProductLotService {
        private Long deletedLotId;
        private RuntimeException deleteFailure;

        @Override
        public void deleteLot(Long lotId) {
            if (deleteFailure != null) {
                throw deleteFailure;
            }
            this.deletedLotId = lotId;
        }
    }
}
