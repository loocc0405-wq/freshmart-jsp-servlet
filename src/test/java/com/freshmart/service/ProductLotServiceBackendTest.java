package com.freshmart.service;

import com.freshmart.entity.Product;
import com.freshmart.entity.ProductLot;
import com.freshmart.entity.Supplier;
import com.freshmart.repository.ProductLotRepository;
import com.freshmart.util.JpaExecutor;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

class ProductLotServiceBackendTest {

    @Test
    void importLot_shouldInitializeQtyLeftAndNormalizeNullPrice() throws Exception {
        Product product = product(10L, "Cá hồi");
        Supplier supplier = supplier(20L, "Nhà cung cấp A");

        EntityManagerState state = new EntityManagerState();
        state.put(Product.class, 10L, product);
        state.put(Supplier.class, 20L, supplier);
        EntityManager em = newEntityManagerProxy(state);

        ProductLotService service = new ProductLotService();
        ProductLot lot = service.importLot(
                10L,
                20L,
                LocalDate.of(2026, 3, 10),
                LocalDate.of(2026, 3, 20),
                15,
                null,
                em
        );

        assertSame(product, lot.getProduct());
        assertSame(supplier, lot.getSupplier());
        assertEquals(15, lot.getQtyIn());
        assertEquals(15, lot.getQtyLeft());
        assertEquals(BigDecimal.ZERO, lot.getImportPrice());
        assertTrue(state.persisted.contains(lot));
    }

    @Test
    void updateLot_shouldRecalculateQtyLeftBasedOnConsumedQty() throws Exception {
        Product product = product(10L, "Tôm sú");
        Supplier oldSupplier = supplier(20L, "NCC cũ");
        Supplier newSupplier = supplier(21L, "NCC mới");
        ProductLot existingLot = lot(99L, product, oldSupplier,
                LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 25),
                10, 4, new BigDecimal("80000"));

        EntityManagerState state = new EntityManagerState();
        state.put(ProductLot.class, 99L, existingLot);
        state.put(Product.class, 10L, product);
        state.put(Supplier.class, 21L, newSupplier);
        EntityManager em = newEntityManagerProxy(state);

        ProductLotService service = new ProductLotService();
        ProductLot updated = service.updateLot(
                99L,
                10L,
                21L,
                LocalDate.of(2026, 3, 2),
                LocalDate.of(2026, 3, 28),
                14,
                new BigDecimal("90000"),
                em
        );

        assertSame(existingLot, updated);
        assertSame(newSupplier, updated.getSupplier());
        assertEquals(LocalDate.of(2026, 3, 2), updated.getImportDate());
        assertEquals(LocalDate.of(2026, 3, 28), updated.getExpiryDate());
        assertEquals(14, updated.getQtyIn());
        assertEquals(8, updated.getQtyLeft());
        assertEquals(new BigDecimal("90000"), updated.getImportPrice());
        assertSame(existingLot, state.merged);
    }

    @Test
    void updateLot_shouldRejectQtyLowerThanConsumed() throws Exception {
        Product product = product(10L, "Mực ống");
        ProductLot existingLot = lot(99L, product, null,
                LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 25),
                10, 4, new BigDecimal("80000"));

        EntityManagerState state = new EntityManagerState();
        state.put(ProductLot.class, 99L, existingLot);
        state.put(Product.class, 10L, product);
        EntityManager em = newEntityManagerProxy(state);

        ProductLotService service = new ProductLotService();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.updateLot(
                        99L,
                        10L,
                        null,
                        LocalDate.of(2026, 3, 2),
                        LocalDate.of(2026, 3, 28),
                        5,
                        BigDecimal.ZERO,
                        em
                ));

        assertTrue(ex.getMessage().contains("already consumed/disposed quantity: 6"));
    }

    @Test
    void updateLot_shouldRejectChangingProductAfterConsumption() throws Exception {
        Product oldProduct = product(10L, "Táo");
        Product newProduct = product(11L, "Cam");
        ProductLot existingLot = lot(99L, oldProduct, null,
                LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 25),
                10, 4, new BigDecimal("80000"));

        EntityManagerState state = new EntityManagerState();
        state.put(ProductLot.class, 99L, existingLot);
        state.put(Product.class, 11L, newProduct);
        EntityManager em = newEntityManagerProxy(state);

        ProductLotService service = new ProductLotService();

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> service.updateLot(
                        99L,
                        11L,
                        null,
                        LocalDate.of(2026, 3, 2),
                        LocalDate.of(2026, 3, 28),
                        14,
                        BigDecimal.ZERO,
                        em
                ));

        assertTrue(ex.getMessage().contains("Không thể thay đổi sản phẩm"));
    }

    @Test
    void getLotDetail_shouldDelegateToRepositoryAndWrapOptional() throws Exception {
        Product product = product(10L, "Bò Úc");
        ProductLot expectedLot = lot(77L, product, null,
                LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 30),
                20, 20, BigDecimal.TEN);

        RecordingLotRepository repo = new RecordingLotRepository(expectedLot);
        TestJpaExecutor executor = new TestJpaExecutor(newEntityManagerProxy(new EntityManagerState()));
        ProductLotService service = new ProductLotService(executor, repo, new InventoryAuditService());

        Optional<ProductLot> result = service.getLotDetail(77L);

        assertTrue(result.isPresent());
        assertSame(expectedLot, result.get());
        assertEquals(77L, repo.lastRequestedLotId);
    }

    @Test
    void deleteLot_shouldRejectActiveUnexpiredLot() throws Exception {
        Product product = product(10L, "Nho xanh");
        ProductLot activeLot = lot(55L, product, null,
                LocalDate.now().minusDays(1), LocalDate.now().plusDays(5),
                12, 12, BigDecimal.ONE);

        EntityManagerState state = new EntityManagerState();
        state.put(ProductLot.class, 55L, activeLot);
        ProductLotService service = new ProductLotService(
                new TestJpaExecutor(newEntityManagerProxy(state)),
                new ProductLotRepository(),
                new InventoryAuditService()
        );

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> service.deleteLot(55L));

        assertTrue(ex.getMessage().contains("Chỉ được loại bỏ lô đã hết hạn hoặc đã dùng hết"));
        assertNull(state.removed);
    }

    @Test
    void deleteLot_shouldRemoveExpiredLot() throws Exception {
        Product product = product(10L, "Rau muống");
        ProductLot expiredLot = lot(56L, product, null,
                LocalDate.now().minusDays(10), LocalDate.now().minusDays(1),
                12, 5, BigDecimal.ONE);

        EntityManagerState state = new EntityManagerState();
        state.put(ProductLot.class, 56L, expiredLot);
        ProductLotService service = new ProductLotService(
                new TestJpaExecutor(newEntityManagerProxy(state)),
                new ProductLotRepository(),
                new InventoryAuditService()
        );

        service.deleteLot(56L);

        assertSame(expiredLot, state.removed);
    }

    private static Product product(Long id, String name) throws Exception {
        Product product = new Product();
        setId(product, id);
        product.setName(name);
        product.setActive(true);
        product.setSellPrice(BigDecimal.ONE);
        return product;
    }

    private static Supplier supplier(Long id, String name) throws Exception {
        Supplier supplier = new Supplier();
        setId(supplier, id);
        supplier.setName(name);
        supplier.setEmail(name.toLowerCase().replace(' ', '.') + "@test.local");
        return supplier;
    }

    private static ProductLot lot(Long id,
                                  Product product,
                                  Supplier supplier,
                                  LocalDate importDate,
                                  LocalDate expiryDate,
                                  int qtyIn,
                                  int qtyLeft,
                                  BigDecimal importPrice) throws Exception {
        ProductLot lot = new ProductLot();
        setId(lot, id);
        lot.setProduct(product);
        lot.setSupplier(supplier);
        lot.setImportDate(importDate);
        lot.setExpiryDate(expiryDate);
        lot.setQtyIn(qtyIn);
        lot.setQtyLeft(qtyLeft);
        lot.setImportPrice(importPrice);
        return lot;
    }

    private static void setId(Object target, Long id) throws Exception {
        Field idField = target.getClass().getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(target, id);
    }

    private static EntityManager newEntityManagerProxy(EntityManagerState state) {
        return (EntityManager) Proxy.newProxyInstance(
                EntityManager.class.getClassLoader(),
                new Class[]{EntityManager.class},
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "find":
                            return state.find((Class<?>) args[0], args[1]);
                        case "persist":
                            state.persisted.add(args[0]);
                            return null;
                        case "merge":
                            state.merged = args[0];
                            return args[0];
                        case "remove":
                            state.removed = args[0];
                            return null;
                        default:
                            return defaultValue(method.getReturnType());
                    }
                }
        );
    }

    private static Object defaultValue(Class<?> returnType) {
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

    private static class EntityManagerState {
        private final Map<String, Object> store = new HashMap<>();
        private final java.util.List<Object> persisted = new java.util.ArrayList<>();
        private Object merged;
        private Object removed;

        void put(Class<?> type, Long id, Object entity) {
            store.put(type.getName() + "#" + id, entity);
        }

        Object find(Class<?> type, Object id) {
            return store.get(type.getName() + "#" + id);
        }
    }

    private static class TestJpaExecutor extends JpaExecutor {
        private final EntityManager em;

        private TestJpaExecutor(EntityManager em) {
            this.em = em;
        }

        @Override
        public <T> T execute(Function<EntityManager, T> work) {
            return work.apply(em);
        }

        @Override
        public void executeVoid(Consumer<EntityManager> work) {
            work.accept(em);
        }
    }

    private static class RecordingLotRepository extends ProductLotRepository {
        private final ProductLot result;
        private Long lastRequestedLotId;

        private RecordingLotRepository(ProductLot result) {
            this.result = result;
        }

        @Override
        public ProductLot findByIdWithRefs(EntityManager em, Long lotId) {
            this.lastRequestedLotId = lotId;
            return result;
        }
    }
}
