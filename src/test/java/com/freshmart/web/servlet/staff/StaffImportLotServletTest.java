package com.freshmart.web.servlet.staff;

import com.freshmart.entity.ProductLot;
import com.freshmart.repository.ProductRepository;
import com.freshmart.repository.SupplierRepository;
import com.freshmart.service.ProductLotService;
import com.freshmart.util.JpaExecutor;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

import jakarta.persistence.EntityManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class StaffImportLotServletTest {

    @Test
    void doGet_shouldLoadEditingLotByIdAndForwardToForm() throws Exception {
        ProductLot editingLot = new ProductLot();
        setId(editingLot, 15L);

        RecordingProductLotService lotService = new RecordingProductLotService();
        lotService.detailToReturn = Optional.of(editingLot);

        StaffImportLotServlet servlet = new StaffImportLotServlet(
                new NoOpJpaExecutor(),
                new EmptyProductRepository(),
                new EmptySupplierRepository(),
                lotService);

        Map<String, String> params = new HashMap<>();
        params.put("id", "15");
        RequestContext context = new RequestContext(params);

        servlet.doGet(context.request, newResponseProxy());

        assertEquals(Long.valueOf(15L), lotService.detailLotId);
        assertSame(editingLot, context.attributes.get("editingLot"));
        assertEquals("/WEB-INF/jsp/staff/import_lot.jsp", context.forwardedTo.get());
    }

    @Test
    void doPost_shouldCreateNewLotAndExposeSuccessMessage() throws Exception {
        ProductLot savedLot = new ProductLot();
        setId(savedLot, 88L);

        RecordingProductLotService lotService = new RecordingProductLotService();
        lotService.importResult = savedLot;

        StaffImportLotServlet servlet = new StaffImportLotServlet(
                new NoOpJpaExecutor(),
                new EmptyProductRepository(),
                new EmptySupplierRepository(),
                lotService);

        Map<String, String> params = new HashMap<>();
        params.put("productId", "10");
        params.put("supplierId", "20");
        params.put("importDate", "2026-03-10");
        params.put("expiryDate", "2026-03-20");
        params.put("quantity", "12");
        params.put("importPrice", "95000.50");
        RequestContext context = new RequestContext(params);

        servlet.doPost(context.request, newResponseProxy());

        assertEquals(Long.valueOf(10L), lotService.importProductId);
        assertEquals(Long.valueOf(20L), lotService.importSupplierId);
        assertEquals(LocalDate.of(2026, 3, 10), lotService.importDate);
        assertEquals(LocalDate.of(2026, 3, 20), lotService.expiryDate);
        assertEquals(12, lotService.quantity);
        assertEquals(new BigDecimal("95000.50"), lotService.importPrice);
        assertEquals(null, lotService.performedByUserId);
        assertEquals("Nhập lô thành công! Lô ID: 88", context.attributes.get("successMessage"));
        assertEquals("/WEB-INF/jsp/staff/import_lot.jsp", context.forwardedTo.get());
    }

    @Test
    void doPost_shouldUpdateExistingLotWhenLotIdProvided() throws Exception {
        ProductLot savedLot = new ProductLot();
        setId(savedLot, 99L);

        RecordingProductLotService lotService = new RecordingProductLotService();
        lotService.updateResult = savedLot;

        StaffImportLotServlet servlet = new StaffImportLotServlet(
                new NoOpJpaExecutor(),
                new EmptyProductRepository(),
                new EmptySupplierRepository(),
                lotService);

        Map<String, String> params = new HashMap<>();
        params.put("lotId", "99");
        params.put("productId", "10");
        params.put("importDate", "2026-03-10");
        params.put("expiryDate", "2026-03-22");
        params.put("quantity", "16");
        params.put("importPrice", "120000");
        RequestContext context = new RequestContext(params);

        servlet.doPost(context.request, newResponseProxy());

        assertEquals(Long.valueOf(99L), lotService.updateLotId);
        assertEquals(null, lotService.performedByUserId);
        assertEquals("Cập nhật lô thành công! Lô ID: 99", context.attributes.get("successMessage"));
        assertSame(savedLot, context.attributes.get("editingLot"));
    }

    @Test
    void doPost_shouldPreserveSubmittedValuesWhenServiceFails() throws Exception {
        RecordingProductLotService lotService = new RecordingProductLotService();
        lotService.importFailure = new IllegalArgumentException("Quantity must be greater than 0");

        StaffImportLotServlet servlet = new StaffImportLotServlet(
                new NoOpJpaExecutor(),
                new EmptyProductRepository(),
                new EmptySupplierRepository(),
                lotService);

        Map<String, String> params = new HashMap<>();
        params.put("productId", "10");
        params.put("supplierId", "20");
        params.put("importDate", "2026-03-10");
        params.put("expiryDate", "2026-03-20");
        params.put("quantity", "0");
        params.put("importPrice", "1000");
        RequestContext context = new RequestContext(params);

        servlet.doPost(context.request, newResponseProxy());

        assertEquals("Quantity must be greater than 0", context.attributes.get("errorMessage"));
        assertEquals("10", context.attributes.get("formProductId"));
        assertEquals("20", context.attributes.get("formSupplierId"));
        assertEquals("2026-03-10", context.attributes.get("formImportDate"));
        assertEquals("2026-03-20", context.attributes.get("formExpiryDate"));
        assertEquals("0", context.attributes.get("formQuantity"));
        assertEquals("1000", context.attributes.get("formImportPrice"));
        assertEquals("/WEB-INF/jsp/staff/import_lot.jsp", context.forwardedTo.get());
    }

    private static void setId(Object target, Long id) throws Exception {
        Field idField = target.getClass().getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(target, id);
    }

    private HttpServletResponse newResponseProxy() {
        return (HttpServletResponse) Proxy.newProxyInstance(
                HttpServletResponse.class.getClassLoader(),
                new Class[] { HttpServletResponse.class },
                (proxy, method, args) -> defaultValue(method.getReturnType()));
    }

    private Object defaultValue(Class<?> returnType) {
        if (!returnType.isPrimitive()) {
            return null;
        }
        if (returnType == boolean.class)
            return false;
        if (returnType == byte.class)
            return (byte) 0;
        if (returnType == short.class)
            return (short) 0;
        if (returnType == int.class)
            return 0;
        if (returnType == long.class)
            return 0L;
        if (returnType == float.class)
            return 0f;
        if (returnType == double.class)
            return 0d;
        if (returnType == char.class)
            return '\0';
        return null;
    }

    private static class RequestContext {
        private final Map<String, String> params;
        private final Map<String, Object> attributes = new HashMap<>();
        private final AtomicReference<String> forwardedTo = new AtomicReference<>();
        private final HttpServletRequest request;

        private static class SessionContext {
            private final Map<String, Object> attributes = new HashMap<>();
            private final HttpSession proxy;

            private SessionContext() {
                this.proxy = (HttpSession) Proxy.newProxyInstance(
                        HttpSession.class.getClassLoader(),
                        new Class[] { HttpSession.class },
                        (proxy, method, args) -> {
                            switch (method.getName()) {
                                case "getAttribute":
                                    return attributes.get(args[0]);
                                case "setAttribute":
                                    attributes.put((String) args[0], args[1]);
                                    return null;
                                default:
                                    return defaultValue(method.getReturnType());
                            }
                        });
            }

            private Object defaultValue(Class<?> returnType) {
                if (!returnType.isPrimitive()) {
                    return null;
                }
                if (returnType == boolean.class)
                    return false;
                if (returnType == byte.class)
                    return (byte) 0;
                if (returnType == short.class)
                    return (short) 0;
                if (returnType == int.class)
                    return 0;
                if (returnType == long.class)
                    return 0L;
                if (returnType == float.class)
                    return 0f;
                if (returnType == double.class)
                    return 0d;
                if (returnType == char.class)
                    return '\0';
                return null;
            }
        }

        private final SessionContext sessionContext = new SessionContext();

        private RequestContext(Map<String, String> params) {
            this.params = params;
            this.request = (HttpServletRequest) Proxy.newProxyInstance(
                    HttpServletRequest.class.getClassLoader(),
                    new Class[] { HttpServletRequest.class },
                    (proxy, method, args) -> {
                        switch (method.getName()) {
                            case "getParameter":
                                return this.params.get(args[0]);
                            case "setAttribute":
                                attributes.put((String) args[0], args[1]);
                                return null;
                            case "getAttribute":
                                return attributes.get(args[0]);
                            case "getRequestDispatcher":
                                return newDispatcherProxy((String) args[0], forwardedTo);
                            case "getSession":
                                return sessionContext.proxy;
                            default:
                                return defaultValue(method.getReturnType());
                        }
                    });
        }

        private Object defaultValue(Class<?> returnType) {
            if (!returnType.isPrimitive()) {
                return null;
            }
            if (returnType == boolean.class)
                return false;
            if (returnType == byte.class)
                return (byte) 0;
            if (returnType == short.class)
                return (short) 0;
            if (returnType == int.class)
                return 0;
            if (returnType == long.class)
                return 0L;
            if (returnType == float.class)
                return 0f;
            if (returnType == double.class)
                return 0d;
            if (returnType == char.class)
                return '\0';
            return null;
        }

        private RequestDispatcher newDispatcherProxy(String path, AtomicReference<String> forwardedTo) {
            return (RequestDispatcher) Proxy.newProxyInstance(
                    RequestDispatcher.class.getClassLoader(),
                    new Class[] { RequestDispatcher.class },
                    (proxy, method, args) -> {
                        if ("forward".equals(method.getName())) {
                            forwardedTo.set(path);
                            return null;
                        }
                        return defaultValue(method.getReturnType());
                    });
        }
    }

    private static class NoOpJpaExecutor extends JpaExecutor {
        private final EntityManager em = (EntityManager) Proxy.newProxyInstance(
                EntityManager.class.getClassLoader(),
                new Class[] { EntityManager.class },
                (proxy, method, args) -> null);

        @Override
        public <T> T execute(Function<EntityManager, T> work) {
            return work.apply(em);
        }

        @Override
        public void executeVoid(Consumer<EntityManager> work) {
            work.accept(em);
        }
    }

    private static class EmptyProductRepository extends ProductRepository {
        @Override
        public java.util.List<com.freshmart.entity.Product> findAll(EntityManager em, boolean showInactive) {
            return Collections.emptyList();
        }
    }

    private static class EmptySupplierRepository extends SupplierRepository {
        @Override
        public java.util.List<com.freshmart.entity.Supplier> findAll(EntityManager em) {
            return Collections.emptyList();
        }
    }

    private static class RecordingProductLotService extends ProductLotService {
        private Optional<ProductLot> detailToReturn = Optional.empty();
        private Long detailLotId;

        private ProductLot importResult;
        private RuntimeException importFailure;
        private Long importProductId;
        private Long importSupplierId;
        private LocalDate importDate;
        private LocalDate expiryDate;
        private int quantity;
        private BigDecimal importPrice;
        private Long performedByUserId;

        private ProductLot updateResult;
        private Long updateLotId;

        @Override
        public Optional<ProductLot> getLotDetail(Long lotId) {
            this.detailLotId = lotId;
            return detailToReturn;
        }

        @Override
        public ProductLot importLot(Long productId,
                Long supplierId,
                LocalDate importDate,
                LocalDate expiryDate,
                int quantity,
                BigDecimal importPrice,
                Long performedByUserId,
                EntityManager em) {
            if (importFailure != null) {
                throw importFailure;
            }
            this.importProductId = productId;
            this.importSupplierId = supplierId;
            this.importDate = importDate;
            this.expiryDate = expiryDate;
            this.quantity = quantity;
            this.importPrice = importPrice;
            this.performedByUserId = performedByUserId;
            return importResult;
        }

        @Override
        public ProductLot updateLot(Long lotId,
                Long productId,
                Long supplierId,
                LocalDate importDate,
                LocalDate expiryDate,
                int newQtyIn,
                BigDecimal importPrice,
                Long performedByUserId,
                EntityManager em) {
            this.updateLotId = lotId;
            this.importProductId = productId;
            this.importSupplierId = supplierId;
            this.importDate = importDate;
            this.expiryDate = expiryDate;
            this.quantity = newQtyIn;
            this.importPrice = importPrice;
            this.performedByUserId = performedByUserId;
            return updateResult;
        }
    }
}
