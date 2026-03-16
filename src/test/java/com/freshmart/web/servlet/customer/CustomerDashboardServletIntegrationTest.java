package com.freshmart.web.servlet.customer;

import com.freshmart.config.AppConstants;
import com.freshmart.entity.Order;
import com.freshmart.entity.User;
import com.freshmart.enums.OrderStatus;
import com.freshmart.enums.OrderType;
import com.freshmart.enums.PaymentMethod;
import com.freshmart.enums.Role;
import com.freshmart.service.CustomerDashboardService;
import com.freshmart.service.CustomerOrderService;
import com.freshmart.service.dto.CustomerDashboardSummary;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class CustomerDashboardServletIntegrationTest {

    @Test
    void doGet_shouldExposeSummaryAndLimitRecentOrdersToFive() throws Exception {
        User sessionUser = newUser(41L);

        CustomerDashboardSummary summary = new CustomerDashboardSummary();
        summary.setTotalOrders(6);
        summary.setPendingOrders(1);
        summary.setCompletedOrders(5);
        summary.setTotalSpent(new BigDecimal("123456.78"));
        summary.setSpentLast30Days(new BigDecimal("65432.10"));
        summary.setAverageCompletedOrderAmount(new BigDecimal("20000.00"));
        summary.setLatestCompletedOrderAmount(new BigDecimal("25000.00"));
        summary.setLatestCompletedAt(LocalDateTime.of(2026, 3, 15, 10, 30));

        List<Order> orders = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            orders.add(newOrder((long) (100 + i), "ORD-" + i, new BigDecimal("1000.00"), LocalDateTime.of(2026, 3, 10 + i, 9, 0)));
        }

        CustomerDashboardServlet servlet = new CustomerDashboardServlet();
        StubCustomerDashboardService dashboardService = new StubCustomerDashboardService();
        dashboardService.summaryToReturn = summary;
        StubCustomerOrderService orderService = new StubCustomerOrderService();
        orderService.ordersToReturn = orders;
        replaceField(servlet, "customerDashboardService", dashboardService);
        replaceField(servlet, "customerOrderService", orderService);

        SessionContext sessionCtx = new SessionContext();
        sessionCtx.attrs.put(AppConstants.SESSION_USER, sessionUser);
        HttpSession session = newSessionProxy(sessionCtx);

        RequestContext requestCtx = new RequestContext();
        requestCtx.contextPath = "/freshmart";

        servlet.doGet(newRequestProxy(requestCtx, session), newResponseProxy(new ResponseContext()));

        assertEquals(41L, dashboardService.lastCustomerId);
        assertEquals(41L, orderService.lastCustomerId);
        assertSame(summary, requestCtx.attrs.get("summary"));
        assertEquals("/WEB-INF/jsp/customer/dashboard.jsp", requestCtx.forwardedPath);

        @SuppressWarnings("unchecked")
        List<Order> recentOrders = (List<Order>) requestCtx.attrs.get("recentOrders");
        assertEquals(5, recentOrders.size());
        assertSame(orders.get(0), recentOrders.get(0));
        assertSame(orders.get(4), recentOrders.get(4));
    }

    private User newUser(Long id) throws Exception {
        User user = new User("customer01", "customer01@freshmart.local", "hash", Role.CUSTOMER);
        setField(user, "id", id);
        return user;
    }

    private Order newOrder(Long id, String orderCode, BigDecimal total, LocalDateTime createdAt) throws Exception {
        Order order = new Order();
        setField(order, "id", id);
        order.setOrderCode(orderCode);
        order.setStatus(OrderStatus.COMPLETED);
        order.setType(OrderType.ONLINE);
        order.setPaymentMethod(PaymentMethod.COD);
        order.setTotalAmount(total);
        order.setCreatedAt(createdAt);
        return order;
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
                        case "getSession":
                            return session;
                        case "setAttribute":
                            ctx.attrs.put((String) args[0], args[1]);
                            return null;
                        case "getAttribute":
                            return ctx.attrs.get(args[0]);
                        case "getRequestDispatcher":
                            return newDispatcherProxy(ctx, (String) args[0]);
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

    private static class StubCustomerDashboardService extends CustomerDashboardService {
        private CustomerDashboardSummary summaryToReturn;
        private Long lastCustomerId;

        @Override
        public CustomerDashboardSummary getSummary(Long customerId) {
            this.lastCustomerId = customerId;
            return summaryToReturn;
        }
    }

    private static class StubCustomerOrderService extends CustomerOrderService {
        private List<Order> ordersToReturn;
        private Long lastCustomerId;

        @Override
        public List<Order> getOrdersByCustomer(Long customerId) {
            this.lastCustomerId = customerId;
            return ordersToReturn;
        }
    }

    private static class SessionContext {
        private final Map<String, Object> attrs = new HashMap<>();
    }

    private static class RequestContext {
        private final Map<String, Object> attrs = new HashMap<>();
        private String contextPath;
        private String forwardedPath;
    }

    private static class ResponseContext {
    }
}