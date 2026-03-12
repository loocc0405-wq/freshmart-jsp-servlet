# Supplier Recommendation Feature - Implementation Summary

## Mục tiêu
Mở rộng feature "gợi ý nhập hàng" để tự động chọn NCC tốt nhất cho từng sản phẩm dựa trên lịch sử nhập hàng.

## Files đã sửa

### 1. DTO mới
- `src/main/java/com/freshmart/service/dto/SupplierCandidate.java` (NEW)
  - Chứa thông tin supplier candidate từ lịch sử nhập hàng
  - Fields: supplierId, supplierName, supplierLeadTimeDays, avgImportPrice, lastImportDate, lotCount, totalQtyIn

### 2. Repository
- `src/main/java/com/freshmart/repository/ProductLotRepository.java`
  - Thêm method `findSupplierCandidates(EntityManager em, Long productId)`
  - Aggregate supplier từ product_lots theo productId
  - Tính AVG(importPrice), MAX(importDate), COUNT(lots), SUM(qtyIn)

### 3. Service
- `src/main/java/com/freshmart/service/ReplenishmentService.java`
  - Thêm method `recommendSupplier()` - gọi sau khi tính suggestedQty
  - Thêm method `rankSuppliers()` - ranking theo rule ưu tiên
  - Set thêm expectedDemand, safetyStock, reorderPoint vào suggestion
  - Không thay đổi logic tính suggestedQty hiện có

### 4. DTO mở rộng
- `src/main/java/com/freshmart/service/dto/ReplenishSuggestion.java`
  - Thêm fields: expectedDemand, safetyStock, reorderPoint
  - Thêm fields: recommendedSupplierId, recommendedSupplierName, recommendedSupplierLeadTimeDays
  - Thêm fields: recommendedSupplierAvgImportPrice, recommendedSupplierLastImportDate
  - Thêm field: recommendationReason
  - Backward compatible với constructor cũ

### 5. UI - Staff Forecast
- `src/main/webapp/WEB-INF/jsp/staff/forecast.jsp`
  - Thêm cột: Best Supplier, Lead Time, Avg Price, Last Import, Action
  - Hiển thị nút "Nhập lô với NCC này" khi suggestedQty > 0 và có recommendedSupplierId
  - Link: `/staff/import-lot?productId=...&supplierId=...`

### 6. UI - PRO Replenishment
- `src/main/webapp/WEB-INF/jsp/pro/replenishment.jsp`
  - Thêm cột: ExpectedDemand, SafetyStock, ReorderPoint
  - Thêm cột: Best Supplier, Lead Time, Avg Price, Last Import, Reason, Action
  - Hiển thị recommendationReason hoặc note
  - Hiển thị nút "Nhập lô với NCC này"

## Rule chọn NCC tốt nhất (ưu tiên theo thứ tự)
1. `leadTimeDays` nhỏ hơn
2. `avgImportPrice` thấp hơn
3. `lastImportDate` mới hơn (reverse order)
4. `lotCount` nhiều hơn (reverse order)
5. `totalQtyIn` nhiều hơn (reverse order)
6. `supplierId` nhỏ hơn

## Xử lý edge case
- Nếu product chưa có lịch sử NCC:
  - Không crash
  - Set recommendationReason = "Không có lịch sử supplier cho sản phẩm này"
  - Vẫn giữ suggestedQty
  - UI hiển thị "-" cho các field supplier

## Test
- `src/test/java/com/freshmart/service/ReplenishmentServiceSupplierTest.java` (NEW)
  - Test ranking by leadTime
  - Test ranking by price
  - Test ranking by lastImportDate
  - Test ranking by lotCount
  - Test ranking by totalQtyIn
  - Test ranking by supplierId
  - Test empty list
  - Test null values

## Kết quả compile/test
- Compile: SUCCESS
- Tests run: 122, Failures: 0, Errors: 0, Skipped: 0
- Tất cả test cũ vẫn pass (backward compatible)
- 8 test mới cho supplier ranking đều pass

## Chưa làm
- Không có - tất cả yêu cầu đã hoàn thành
