## FreshMart - Hệ thống quản lý tồn kho theo lô và FEFO

### Giới thiệu

FreshMart sử dụng **mô hình quản lý tồn kho theo lô (batch/lot-based inventory)** kết hợp với **nguyên tắc FEFO (First Expired, First Out)** để đảm bảo rằng:

1. **Hàng hóa tươi sống được sử dụng đúng hạn** - Không để hàng hết hạn
2. **Giảm thiểu chất thải** - Tối ưu hóa sử dụng hàng hóa
3. **Quản lý chi phí rõ ràng** - Theo dõi giá nhập từng lô
4. **Tính minh bạch** - Rõ ràng từng đợt nhập và hạn sử dụng

### Các thành phần chính

#### 1. **ProductLot Entity** (Lô hàng)
```
- id: Long (ID lô hàng)
- product: Product (sản phẩm)
- supplier: Supplier (nhà cung cấp)
- importDate: LocalDate (ngày nhập)
- expiryDate: LocalDate (ngày hết hạn/HSD)
- qtyIn: Integer (số lượng nhập vào)
- qtyLeft: Integer (số lượng còn lại)
- importPrice: BigDecimal (giá nhập)
```

**Ứng dụng thực tế:**
- Hàng rau muống nhập 50 kg, hạn sử dụng 3 ngày
- Hàng cá tươi nhập 20 kg, hạn sử dụng 2 ngày
- Thịt đông lạnh nhập 100 kg, hạn sử dụng 6 tháng

#### 2. **FEFO Logic** (Nguyên tắc First Expired, First Out)

Khi bán hàng (order), hệ thống **tự động chọn lô sắp hết hạn trước**:

```
Ví dụ: Bán 30 kg rau muống
- Lô A: 50 kg, HSD: 3 ngày (CHỌN TRƯỚC)
- Lô B: 80 kg, HSD: 7 ngày

→ Hệ thống lấy từ Lô A trước (HSD sớm hơn)
→ Nếu Lô A không đủ (chỉ 30 kg), lấy thêm từ Lô B
```

**Quy tắc sắp xếp (FEFO):**
1. Ngày HSD sớm hơn → ưu tiên cao
2. Nếu cùng HSD → ngày nhập cũ hơn → ưu tiên cao
3. Nếu giống hết → theo ID lô (ưu tiên thấp nhất)

#### 3. **ProductLotService** - Quản lý lô hàng

**Các chức năng chính:**

```java
// Nhập lô hàng mới
importLot(productId, supplierId, importDate, expiryDate, qty, price);

// Lấy hết cả lô (lịch sử)
getAllLotsForProduct(productId);

// Lấy lô khả dụng (chưa hết hạn, còn số lượng)
getAvailableLotsForProduct(productId);

// Lấy lô đã hết hạn (cần loại bỏ)
getExpiredLotsForProduct(productId);

// Lấy lô sắp hết hạn (trong N ngày)
getLotsExpiringWithinDays(days);
```

#### 4. **InventoryService** - Trừ stock theo FEFO

```java
// Trừ tồn kho theo FEFO khi hoàn thành order
consumeStockFEFO(productId, qty, today);
```

**Luồng thực thi:**

```
Order bán 30 kg rau
↓
inventoryService.consumeStockFEFO(productId=1, qty=30)
↓
Tìm lô (HSD sớm nhất, chưa hết hạn, còn số lượng)
↓
Lô A: 50kg (HSD 3 ngày) → Lấy 30kg
↓
Update: Lô A.qtyLeft = 50 - 30 = 20kg
↓
Order status = COMPLETED
↓
Cập nhật revenue_daily
```

### Các Servlet & JSP

#### 1. **StaffImportLotServlet** (`/staff/import-lot`)
- URL: `POST /staff/import-lot`
- Chức năng: Nhập lô hàng mới
- Dữ liệu:
  - Sản phẩm
  - Nhà cung cấp (tùy chọn)
  - Ngày nhập
  - Hạn sử dụng
  - Số lượng nhập
  - Giá nhập (tùy chọn)

**Ví dụ:**
```
- Sản phẩm: Cá tươi
- Nhà cung cấp: Công ty Cá tươi XYZ
- Ngày nhập: 2024-03-01
- Hạn sử dụng: 2024-03-03
- Số lượng: 20 kg
- Giá nhập: 180,000 VND/kg
```

#### 2. **StaffInventoryViewServlet** (`/staff/inventory`)
- URL: `GET /staff/inventory?productId=1`
- Chức năng: Xem chi tiết tồn kho theo lô
- Hiển thị:
  - Tóm tắt (Tổng nhập, Còn lại, Đã dùng, HSD gần nhất)
  - **Lô khả dụng (FEFO)** - theo thứ tự ưu tiên
  - **Lô sắp hết hạn** - cảnh báo (≤ 7 ngày)
  - **Lô đã hết hạn** - cần loại bỏ
  - Lịch sử toàn bộ lô

**Color coding:**
- 🟢 Xanh: Lô khả dụng bình thường
- 🟡 Vàng: Lô sắp hết hạn
- 🔴 Đỏ: Lô đã hết hạn

#### 3. **InventoryReportService** - Báo cáo tồn kho

**Các báo cáo:**

1. **Toàn bộ sản phẩm** - Tổng quan kinh doanh
   - Tổng nhập, Còn lại, Đã dùng
   - Số lô
   - HSD gần nhất
   - Giá trị tồn kho

2. **Hàng ít tồn kho** - Cảnh báo (< 50 đơn vị)
   - Nên nhập thêm

3. **Sắp hết hạn** - Ưu tiên bán (7 ngày)
   - Giảm rủi ro hỏng hàng

4. **Đã hết hạn** - Không còn dùng được
   - Cần loại bỏ/thải

### Luồng tổng hợp (End-to-end)

#### **Tình huống: Bán hàng tại quầy (Seller POS)**

```
1. Người bán chọn sản phẩm → Thêm vào giỏ
   GET /seller/pos

2. Người bán nhấn Checkout (COMPLETED)
   POST /seller/pos/checkout

3. SellerCheckoutServlet gọi:
   OrderService.createSellerWalkInOrder(seller, items, completeNow=true)

4. Trong transaction:
   - Tạo Order với status=COMPLETED
   - Với mỗi item:
     * Gọi InventoryService.consumeStockFEFO()
     * Tìm lô FEFO (sắp hết hạn trước)
     * Trừ qtyLeft từ lô đó
     * Nếu lô không đủ, lấy tiếp lô khác
   - Cập nhật Order.totalAmount
   - Cập nhật revenue_daily

5. Hiển thị invoice
   GET /seller/invoice
```

### Cảnh báo & Tính năng an toàn

1. **Lô hết hạn không được dùng**
   - `expiryDate < today` → Loại khỏi danh sách khả dụng
   - Nếu cố tình dùng → Lỗi `InsufficientStockException`

2. **Cảnh báo sắp hết hạn**
   - Hiển thị màu vàng trong inventory view
   - Staff nên ưu tiên bán

3. **Báo cáo hàng dôi dư**
   - Nếu qty_left = 0 → Lô đã dùng hết
   - Không tính vào khả dụng

4. **Kiểm tra transactional**
   - Tất cả thao tác import/consume đều trong transaction
   - Rollback nếu có lỗi → Đảm bảo tính nhất quán dữ liệu

### Ví dụ SQL (Truy vấn)

```sql
-- Tìm lô khả dụng (FEFO)
SELECT l.* FROM product_lots l
WHERE l.product_id = 1
  AND l.qty_left > 0
  AND l.expiry_date >= CURDATE()
ORDER BY l.expiry_date ASC, l.import_date ASC, l.id ASC;

-- Tổng tồn kho khả dụng của sản phẩm
SELECT SUM(l.qty_left) 
FROM product_lots l
WHERE l.product_id = 1
  AND l.qty_left > 0
  AND l.expiry_date >= CURDATE();

-- Lô sắp hết hạn (7 ngày)
SELECT * FROM product_lots l
WHERE l.qty_left > 0
  AND l.expiry_date BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 7 DAY)
ORDER BY l.expiry_date ASC;

-- Lô đã hết hạn (cần loại bỏ)
SELECT * FROM product_lots l
WHERE l.expiry_date < CURDATE()
  AND l.qty_left > 0;
```

### Tổng kết

Hệ thống FEFO trong FreshMart:

| Tính năng | Lợi ích |
|-----------|---------|
| **Quản lý theo lô** | Rõ ràng chi phí, HSD, nguồn gốc |
| **FEFO logic** | Tự động sử dụng hàng sắp hết hạn |
| **Báo cáo chi tiết** | Kiểm soát tồn kho, ngăn chặn lãng phí |
| **Cảnh báo HSD** | Tránh bán hàng hỏng, mất tiếp cận |
| **Transactional** | Đảm bảo tính nhất quán dữ liệu |
