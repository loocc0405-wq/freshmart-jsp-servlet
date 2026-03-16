package com.freshmart.web.servlet.customer;

import com.freshmart.config.AppConstants;
import com.freshmart.entity.Order;
import com.freshmart.entity.OrderItem;
import com.freshmart.entity.Product;
import com.freshmart.entity.User;
import com.freshmart.enums.OrderStatus;
import com.freshmart.enums.OrderType;
import com.freshmart.enums.PaymentMethod;
import com.freshmart.enums.Role;
import com.freshmart.service.CustomerOrderService;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class CustomerOrdersServletIntegrationTest {

    @Test
    void ordersDoGet_shouldExposeFilterAndPagingState() throws Exception {
        User sessionUser = newUser(51L);
        List<Order> orders = List.of(
                newOrder(1L, "ORD-001", new BigDecimal("100.00"), LocalDateTime.of(2026, 3, 1, 10, 0)),
                newOrder(2L, "ORD-002", new BigDecimal("200.00"), LocalDateTime.of(2026, 3, 2, 10, 0))
        );

        CustomerOrdersServlet servlet = new CustomerOrdersServlet();
        StubCustomerOrderService orderService = new StubCustomerOrderService();
        orderService.ordersToReturn = orders;
        orderService.totalItemsToReturn = 7L;
        replaceField(servlet, "customerOrderService", orderService);

        SessionContext sessionCtx = new SessionContext();
        sessionCtx.attrs.put(AppConstants.SESSION_USER, sessionUser);
        HttpSession session = newSessionProxy(sessionCtx);

        RequestContext requestCtx = new RequestContext();
        requestCtx.contextPath = "/freshmart";
        requestCtx.params.put("status", "COMPLETED");
        requestCtx.params.put("fromDate", "2026-03-01");
        requestCtx.params.put("toDate", "2026-03-15");
        requestCtx.params.put("page", "1");

        servlet.doGet(newRequestProxy(requestCtx, session), newResponseProxy(new ResponseContext()));

        assertEquals(51L, orderService.lastCustomerId);
        assertEquals("COMPLETED", orderService.lastStatus);
        assertEquals("2026-03-01", orderService.lastFromDate);
        assertEquals("2026-03-15", orderService.lastToDate);
        assertEquals(1, orderService.lastPage);
        assertEquals(5, orderService.lastSize);
        assertSame(orders, requestCtx.attrs.get("orders"));
        assertEquals("COMPLETED", requestCtx.attrs.get("selectedStatus"));
        assertEquals("2026-03-01", requestCtx.attrs.get("fromDate"));
        assertEquals("2026-03-15", requestCtx.attrs.get("toDate"));
        assertEquals(1, requestCtx.attrs.get("currentPage"));
        assertEquals(5, requestCtx.attrs.get("pageSize"));
        assertEquals(7L, requestCtx.attrs.get("totalItems"));
        assertEquals(2L, requestCtx.attrs.get("totalPages"));
        assertEquals("/WEB-INF/jsp/customer/orders.jsp", requestCtx.forwardedPath);
    }

    @Test
    void detailDoGet_shouldLoadOrderDetailAndForward() throws Exception {
        User sessionUser = newUser(61L);
        Order order = newOrder(9L, "ORD-009", new BigDecimal("300.00"), LocalDateTime.of(2026, 3, 11, 9, 30));
        Product product = new Product("Apple", new BigDecimal("15.00"));
        setField(product, "id", 77L);
        order.addItem(new OrderItem(product, 2, new BigDecimal("15.00")));

        CustomerOrderDetailServlet servlet = new CustomerOrderDetailServlet();
        StubCustomerOrderService orderService = new StubCustomerOrderService();
        orderService.orderDetailToReturn = order;
        replaceField(servlet, "customerOrderService", orderService);

        SessionContext sessionCtx = new SessionContext();
        sessionCtx.attrs.put(AppConstants.SESSION_USER, sessionUser);
        HttpSession session = newSessionProxy(sessionCtx);

        RequestContext requestCtx = new RequestContext();
        requestCtx.contextPath = "/freshmart";
        requestCtx.params.put("id", "9");

        servlet.doGet(newRequestProxy(requestCtx, session), newResponseProxy(new ResponseContext()));

        assertEquals(61L, orderService.lastDetailCustomerId);
        assertEquals(9L, orderService.lastDetailOrderId);
        assertSame(order, requestCtx.attrs.get("order"));
        assertEquals("/WEB-INF/jsp/customer/order_detail.jsp", requestCtx.forwardedPath);
    }

    @Test
    void detailDoGet_shouldRedirectBackToOrderListWhenIdMissing() throws Exception {
        User sessionUser = newUser(71L);

        CustomerOrderDetailServlet servlet = new CustomerOrderDetailServlet();

        SessionContext sessionCtx = new SessionContext();
        sessionCtx.attrs.put(AppConstants.SESSION_USER, sessionUser);
        HttpSession session = newSessionProxy(sessionCtx);

        RequestContext requestCtx = new RequestContext();
        requestCtx.contextPath = "/freshmart";
        ResponseContext responseCtx = new ResponseContext();

        servlet.doGet(newRequestProxy(requestCtx, session), newResponseProxy(responseCtx));

        assertEquals("/freshmart/customer/orders", responseCtx.redirectedTo);
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
                        case "getParameter":
                            return ctx.params.get(args[0]);
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
                        ctx.redirectedTo = (String) args[0];
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

    private static class StubCustomerOrderService extends CustomerOrderService {
        private List<Order> ordersToReturn = new ArrayList<>();
        private long totalItemsToReturn;
        private Order orderDetailToReturn;
        private Long lastCustomerId;
        private String lastStatus;
        private String lastFromDate;
        private String lastToDate;
        private Integer lastPage;
        private Integer lastSize;
        private Long lastDetailCustomerId;
        private Long lastDetailOrderId;

        @Override
        public List<Order> getOrdersByFilters(Long customerId, String status, String fromDate, String toDate, int page, int size) {
            this.lastCustomerId = customerId;
            this.lastStatus = status;
            this.lastFromDate = fromDate;
            this.lastToDate = toDate;
            this.lastPage = page;
            this.lastSize = size;
            return ordersToReturn;
        }

        @Override
        public long countOrdersByFilters(Long customerId, String status, String fromDate, String toDate) {
            this.lastCustomerId = customerId;
            this.lastStatus = status;
            this.lastFromDate = fromDate;
            this.lastToDate = toDate;
            return totalItemsToReturn;
        }

        @Override
        public Order getOrderDetail(Long customerId, Long orderId) {
            this.lastDetailCustomerId = customerId;
            this.lastDetailOrderId = orderId;
            return orderDetailToReturn;
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
        private String redirectedTo;
    }
}
