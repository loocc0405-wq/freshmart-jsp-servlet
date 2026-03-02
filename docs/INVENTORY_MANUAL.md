# FEFO Inventory System - Tài liệu Tính Năng

## 📋 Mục lục
1. [Tổng quan](#tổng-quan)
2. [Các thành phần chính](#các-thành-phần-chính)
3. [Hướng dẫn sử dụng](#hướng-dẫn-sử-dụng)
4. [Ví dụ thực tế](#ví-dụ-thực-tế)
5. [API Reference](#api-reference)

---

## Tổng quan

Hệ thống quản lý tồn kho của FreshMart sử dụng **mô hình tồn kho theo lô (lot-based inventory management)** với **nguyên tắc FEFO (First Expired, First Out)**.

### Tại sao FEFO?

| Vấn đề | Giải pháp FEFO |
|--------|-------------|
| Hàng hors hỏng trước khi bán | Tự động bán lô sắp hết hạn trước |
| Không biết giá thành chính xác | Theo dõi giá nhập từng lô |
| Quản lý hỗn loạn | Rõ ràng từng đợt nhập, HSD, nguồn gốc |
| Lãng phí hàng hóa | Giảm lãng phí bằng cảnh báo HSD sớm |

---

## Các thành phần chính

### 1️⃣ **ProductLot Entity** - Lô Hàng
Đại diện cho mỗi đợt nhập hàng.

```java
ProductLot {
  id: Long,                    // ID lô
  product: Product,            // Sản phẩm
  supplier: Supplier,          // Nhà cung cấp
  importDate: LocalDate,       // Ngày nhập
  expiryDate: LocalDate,       // Hạn sử dụng
  qtyIn: Integer,              // Số lượng nhập vào
  qtyLeft: Integer,            // Số lượng còn lại
  importPrice: BigDecimal      // Giá nhập (đơn vị)
}
```

### 2️⃣ **InventoryService** - Quản lý Tồn Kho
Xử lý các thao tác trừ stock khi bán hàng.

```java
// Lấy số lượng khả dụng của sản phẩm
getAvailableQty(productId, today) → int

// Trừ tồn kho theo FEFO
consumeStockFEFO(productId, qty, today) → List<LotConsumption>
```

### 3️⃣ **ProductLotService** - Quản lý Lô Hàng
Quản lý chu kỳ sống của lô hàng (nhập, xem, xóa).

```java
// Nhập lô hàng
importLot(productId, supplierId, importDate, expiryDate, qty, price)

// Xem lô
getAllLotsForProduct(productId)
getAvailableLotsForProduct(productId)
getExpiredLotsForProduct(productId)
getLotsExpiringWithinDays(days)

// Tính toán
getTotalStockValue(productId)
getProductLotSummary(productId)
```

### 4️⃣ **InventoryReportService** - Báo Cáo
Tạo các báo cáo và thống kê.

```java
// Báo cáo toàn bộ sản phẩm
getAllProductInventoryOverview() → List<ProductInventoryOverview>

// Cảnh báo
getLowStockProducts(threshold)
getProductsWithUpcomingExpiry(days)
getExpiredLotsForCleanup()

// Thống kê
getTotalInventoryValue()
getTotalActiveLots()
```

### 5️⃣ **FEFOUtil** - Hàm Tiện Ích
Các hàm hỗ trợ cho logic FEFO.

```java
sortByFEFO(lots)                // Sắp xếp theo FEFO
getAvailableLotsFEFO(lots, today)
getExpiredLots(lots, today)
getDaysUntilExpiry(lot, today)
needsUrgentUse(lot, today, days) // Cần bán gấp?
```

---

## Hướng dẫn Sử dụng

### 📌 Bước 1: Nhập Lô Hàng

**URL:** `GET/POST` `/staff/import-lot`

```
Chọn sản phẩm: [Rau muống              ]
Nhà cung cấp: [Công ty ABC            ]
Ngày nhập: [2024-03-10]
Hạn sử dụng: [2024-03-13]
Số lượng: [50]
Giá nhập: [15000]

[Nhập lô]
```

**Kết quả:** Tạo ProductLot mới
- qtyIn = 50
- qtyLeft = 50
- Không bị dùng cho đến khi có order

### 📌 Bước 2: Xem Tồn Kho

**URL:** `GET` `/staff/inventory?productId=1`

**Hiển thị:**
1. **Tóm tắt**
   - Tổng nhập: 130 kg
   - Còn lại: 95 kg
   - Đã dùng: 35 kg
   - HSD gần nhất: 2024-03-13

2. **Lô khả dụng (FEFO)**
   ```
   Lô ID | HSD        | Số ngày | Nhập | Còn | Giá
   ------|------------|---------|------|-----|------
   1     | 2024-03-13 |    3    | 50   | 20  | 15k
   2     | 2024-03-17 |    7    | 80   | 75  | 14.5k
   ```
   → Lô 1 sẽ được bán trước (HSD sớm)

3. **Cảnh báo sắp hết hạn (⚠️ ≤ 7 ngày)**
   ```
   Lô 1: 2024-03-13 (3 ngày nữa)
   ```
   → Nên ưu tiên bán ngay

4. **Lô đã hết hạn (❌ cần loại bỏ)**
   ```
   Lô 3: 2024-03-08 (đã hết)
   ```
   → Không được dùng, cần xóa

### 📌 Bước 3: Bán Hàng (Auto FEFO)

**Khi seller checkout tại POS:**

```
1. Seller chọn sản phẩm → Thêm 30 kg rau muống vào giỏ
2. Nhấn "Checkout (COMPLETED)"
3. Hệ thống tự động:
   a. Tìm lô khả dụng (FEFO)
   b. Lô 1 (HSD 3 ngày) → Lấy 20 kg (còn lại: 0 kg)
   c. Lô 2 (HSD 7 ngày) → Lấy 10 kg (còn lại: 65 kg)
   d. Cập nhật order status = COMPLETED
   e. Cập nhật revenue_daily
4. Hiển thị invoice
```

**Chi tiết trong database:**
```sql
BEFORE:
Lô 1: qtyLeft = 20
Lô 2: qtyLeft = 75

AFTER (bán 30 kg):
Lô 1: qtyLeft = 0   (đã dùng hết)
Lô 2: qtyLeft = 65  (lấy 10 kg)

Order:
  - Status: COMPLETED
  - Items: {Product: Rau muống, Qty: 30, UnitPrice: 15000}
  - TotalAmount: 450000

Revenue:
  - Date: 2024-03-10
  - Amount: 450000 (cộng thêm）
```

### 📌 Bước 4: Xem Báo Cáo

**URL:** `GET` `/staff/inventory-report`

**Các tab:**

1. **Toàn bộ sản phẩm** - Tổng quan kinh doanh
   ```
   Sản phẩm | Tổng nhập | Còn lại | Đã dùng | Lô | HSD      | Giá trị
   ---------|-----------|---------|---------|-------|----------|----------
   Rau      | 130       | 65      | 65      | 2     | 17/03    | 975,000
   Thịt     | 100       | 80      | 20      | 2     | 15/03    | 7,200,000
   Cá       | 60        | 50      | 10      | 2     | 12/03    | 7,000,000
   ```

2. **Hàng ít tồn kho** - Nên nhập thêm (< 50)
   ```
   Sản phẩm | Còn lại | Hành động
   ---------|---------|----------
   Rau      | 65      | Nhập thêm
   ```

3. **Sắp hết hạn** - Ưu tiên bán (≤ 7 ngày)
   ```
   Sản phẩm | HSD      | Ngày còn lại
   ---------|----------|-------------
   Rau      | 13/03    | 3 ngày
   Cá       | 12/03    | 2 ngày
   ```

4. **Đã hết hạn** - Không còn dùng được
   ```
   (Nếu không có → ✅ Tốt lắm!)
   ```

---

## Ví dụ Thực Tế

### Scenario 1: Cửa hàng bán rau

```
📅 2024-03-10 (Hôm nay)

Buổi sáng:
──────────
1. Staff nhập rau muống
   - Nhà cung cấp: Công ty ABC
   - Số lượng: 50 kg
   - Giá nhập: 15,000/kg
   - HSD: 3 ngày (13/03)
   [Lô 1 tạo thành công]

2. Staff nhập rau cải
   - Nhà cung cấp: Công ty DEF
   - Số lượng: 80 kg
   - Giá nhập: 14,500/kg
   - HSD: 7 ngày (17/03)
   [Lô 2 tạo thành công]

Buổi chiều:
──────────
3. Seller bán 30 kg rau muống
   → Hệ thống tự động:
      a. Lô 1 (HSD 13/03) → Lấy 20 kg (ưu tiên)
      b. Lô 2 (HSD 17/03) → Lấy 10 kg
   → Order status = COMPLETED
   → Doanh thu: 30 × 15,000 = 450,000 VND

4. Staff xem báo cáo:
   - Rau muống còn 65 kg (dư, có thể giảm giá)
   - Rau cải còn 70 kg
   - HSD gần nhất: 13/03 (3 ngày) - ⚠️ Ưu tiên bán

Ngày hôm sau (11/03):
──────────────────────
5. Staff xem báo cáo
   - Rau muống còn 30 kg, HSD 2 ngày → URGENT!
   - Có thể bán khuyến mãi để hạ tồn

Ngày 14/03:
──────────
6. Lô 1 đã hết hạn
   - Staff xem inventory → Hiển thị màu đỏ
   - Request xóa lô hoặc đánh dấu hỏng

Kết quả kinh doanh:
──────────────────
- Doanh thu: 450,000 VND
- Lãng phí: 0 (tất cả hàng đều bán được)
- Chi phí vốn: 130 × (15 × 50/130 + 14.5 × 80/130) ≈ 1,924,000 VND
- Lợi nhuận: 450,000 - (nếu bán giá cao hơn)
```

### Scenario 2: Hàng hết hạn

```
❌ Tình huống: Nhân viên quên ưu tiên hàng sắp hết hạn

1. Lô A (20 kg, HSD 10/03) → Hiển thị cảnh báo
2. Lô B (80 kg, HSD 20/03) → Ưu tiên bán từ Lô B
3. Kết quả: Lô A chỉ còn lại khi HSD gần hết
4. Hàng hỏng vì bán chậm

❓ Giải pháp FEFO:
- Hệ thống ưu tiên Lô A (HSD sớm)
- Tự động trừ stock từ Lô A trước
- Cảnh báo bằng màu vàng, đỏ
- Không để hàng hỏng
```

---

## API Reference

### **InventoryService**

```java
// Lấy số lượng khả dụng
int getAvailableQty(EntityManager em, Long productId, LocalDate today)

// Trừ tồn kho FEFO (gọi khi order COMPLETED)
List<LotConsumption> consumeStockFEFO(
  EntityManager em, 
  Long productId, 
  int qty, 
  LocalDate today
)

// Throws: InsufficientStockException
```

### **ProductLotService**

```java
// Nhập lô
ProductLot importLot(
  Long productId,           // Sản phẩm
  Long supplierId,          // Nhà cung cấp (tùy chọn)
  LocalDate importDate,     // Ngày nhập
  LocalDate expiryDate,     // HSD
  int quantity,             // Số lượng
  BigDecimal importPrice,   // Giá nhập
  EntityManager em
)

// Xem lô khả dụng
List<ProductLot> getAvailableLotsForProduct(Long productId)

// Xem lô sắp hết hạn
List<ProductLot> getLotsExpiringWithinDays(int days)

// Xem lô đã hết hạn
List<ProductLot> getExpiredLotsForProduct(Long productId)

// Xóa lô
void deleteLot(Long lotId)

// Tính tổng giá trị tồn kho
BigDecimal getTotalStockValue(Long productId)

// Thống kê lô
Map<String, Integer> getProductLotSummary(Long productId)
// → {totalIn, totalLeft, totalConsumed}
```

### **FEFOUtil**

```java
// Sắp xếp theo FEFO
List<ProductLot> sortByFEFO(List<ProductLot> lots)

// Lấy lô khả dụng (FEFO)
List<ProductLot> getAvailableLotsFEFO(
  List<ProductLot> lots, 
  LocalDate today
)

// Lấy lô sắp hết hạn
List<ProductLot> getLotsExpiringWithin(
  List<ProductLot> lots,
  LocalDate today,
  int days
)

// Kiểm tra lô có cần bán gấp không
boolean needsUrgentUse(ProductLot lot, LocalDate today, int urgentDays)

// Tính tổng khả dụng
int getTotalAvailableQty(List<ProductLot> lots, LocalDate today)
```

---

## ⚠️ Chú ý Quan trọng

1. **Transaction:** Tất cả thao tác import/consume phải trong transaction
2. **Expiry:** Lô hết hạn (expiryDate < today) KHÔNG được dùng
3. **Consume:** Khi bán hàng, LUÔN gọi `consumeStockFEFO()`
4. **Check:** Nếu không đủ stock → InsufficientStockException
5. **Báo cáo:** Nên check báo cáo hàng hết hạn hàng ngày

---

## 📞 Liên hệ & Hỗ Trợ

Nếu có câu hỏi về hệ thống FEFO, vui lòng xem file `FEFO_INVENTORY_GUIDE.md` hoặc liên hệ team DevOps.
