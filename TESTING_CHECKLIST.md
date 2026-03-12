# 📋 FRESHMART — BỘ TEST MANUAL / UAT HOÀN CHỈNH

> **Phiên bản:** 1.0 — Ngày tạo: 12/03/2026
> **Project:** FreshMart JSP/Servlet — Hệ thống quản lý thực phẩm tươi cho siêu thị mini
> **Database:** SQL Server — `freshmart`

---

# A. TEST PLAN TỔNG QUAN

## Dữ liệu seed sẵn có

| Thực thể | Số lượng | Chi tiết |
|-----------|----------|----------|
| Users | 4 | admin/staff/seller/customer |
| Suppliers | 1 | "Default Supplier" |
| Products | 38 | 6 categories: Rau củ(8), Thịt(7), Hải sản(10), TPCBS(7), Trái cây(7) |
| Product Lots | 76 | 2 lots/product (lot1: qty/2, expiry +shelfLife; lot2: qty, expiry +shelfLife×2) |
| Orders | 0 | Tạo thủ công khi test |
| Revenue | 0 | Tự phát sinh khi order COMPLETED |
| App Settings | 8 | Tự seed khi app khởi động |
| Subscriptions | 0 | Tạo thủ công khi test |
| Tier History | 0 | Tự phát sinh khi thay đổi tier |

## Tài khoản test

| Username | Password | Role | Tier | Ghi chú |
|----------|----------|------|------|---------|
| admin | admin123 | ADMIN | PRO | Hết hạn +5 năm |
| staff | staff123 | STAFF | FREE | |
| seller | seller123 | SELLER | FREE | |
| customer | customer123 | CUSTOMER | PRO | Hết hạn +30 ngày |

## Module breakdown

### 1. Authentication / Role Access
- **Mục tiêu:** Xác minh login đúng role, redirect đúng trang, filter chặn truy cập trái phép
- **Rủi ro:** Bypass filter, truy cập URL trực tiếp mà không đăng nhập
- **Dữ liệu test:** 4 tài khoản seed ở trên

### 2. Product Catalog
- **Mục tiêu:** Hiển thị đúng danh sách, filter category, tên tiếng Việt, giá, unit, active status
- **Rủi ro:** Lỗi encoding UTF-8, sản phẩm inactive vẫn hiển thị cho customer
- **Dữ liệu test:** 38 sản phẩm, 6 categories (Rau củ, Thịt, Hải sản, Thực phẩm chế biến sẵn, Trái cây)

### 3. Supplier Management
- **Mục tiêu:** CRUD supplier, import CSV, hiển thị lead_time, certificate, email, phone
- **Rủi ro:** Import CSV format sai, trùng email, lead_time_days = null
- **Dữ liệu test:** "Default Supplier" (email: supplier@freshmart.local, lead_time: 2, phone: 0900000000)

### 4. Inventory Lots
- **Mục tiêu:** Xem lots theo product, import_date/expiry_date/qty_in/qty_left/import_price đúng
- **Rủi ro:** Tổng tồn sai, lot expired vẫn tính vào availableQty
- **Dữ liệu test:** 76 lots — mỗi product có 2 lots, import_price = 75% sell_price

### 5. FEFO (First Expired, First Out)
- **Mục tiêu:** Lot có expiry gần nhất được trừ trước khi bán hàng
- **Rủi ro:** Trừ lot sai thứ tự, lot expired vẫn bị trừ
- **Dữ liệu test:** Mỗi product có lot1 (expiry sớm hơn) và lot2 (expiry muộn hơn)

### 6. Low Stock / Out of Stock
- **Mục tiêu:** Cảnh báo sản phẩm availableQty < threshold (mặc định 50)
- **Rủi ro:** Dùng totalQtyLeft thay vì availableQty (bao gồm expired)
- **Dữ liệu test:** Nhiều sản phẩm hải sản có qty thấp (20-30 units)

### 7. Upcoming Expiry / Expired Lots
- **Mục tiêu:** Cảnh báo lot sắp hết hạn (mặc định 7 ngày), liệt kê lot expired còn qty_left > 0
- **Rủi ro:** Bỏ sót lot cận hạn, expired qty không hiển thị
- **Dữ liệu test:** Lots rau, hải sản (shelfLife 4-7 ngày)

### 8. Checkout / Cart / Orders
- **Mục tiêu:** Tạo order WALK_IN (seller) + ONLINE (customer), tổng tiền đúng, FEFO trừ kho
- **Rủi ro:** line_total ≠ quantity × unit_price, total_amount sai, inactive product vẫn bán
- **Dữ liệu test:** Tạo order mới qua giao diện POS / customer checkout

### 9. Order History + Filter
- **Mục tiêu:** Filter theo type (WALK_IN/ONLINE), status, xem chi tiết order
- **Rủi ro:** customer_id null cho WALK_IN bị hiển thị sai, filter kết hợp lỗi
- **Dữ liệu test:** Các order tạo ở bước checkout

### 10. Revenue Dashboard + Forecast
- **Mục tiêu:** Chart hiển thị doanh thu theo ngày, forecast, trend
- **Rủi ro:** revenue_daily không được cập nhật khi complete order, forecast thiếu data
- **Dữ liệu test:** Doanh thu phát sinh từ order COMPLETED

### 11. Subscription FREE/PRO
- **Mục tiêu:** Upgrade PRO, kiểm tra hết hạn, grace period, downgrade
- **Rủi ro:** PRO hết hạn vẫn truy cập PRO features, tier_history thiếu record
- **Dữ liệu test:** customer (PRO +30 ngày), admin (PRO +5 năm)

### 12. App Settings
- **Mục tiêu:** 8 settings keys đúng, thay đổi giá trị ảnh hưởng đúng logic
- **Rủi ro:** Giá trị âm được chấp nhận, thay đổi không phản ánh ngay
- **Dữ liệu test:** 8 keys với defaults

---

# B. TEST CASES CHI TIẾT

---

## MODULE 1: AUTHENTICATION / ROLE ACCESS

### TC-AUTH-001: Đăng nhập thành công với ADMIN
- **Module:** Authentication
- **Priority:** High
- **Preconditions:** App đang chạy, chưa đăng nhập
- **Test data:** username=`admin`, password=`admin123`
- **Steps:**
  1. Truy cập `/login`
  2. Nhập username `admin`, password `admin123`
  3. Nhấn Đăng nhập
- **Expected:** Redirect sang trang admin dashboard. Session chứa user role=ADMIN, tier=PRO

### TC-AUTH-002: Đăng nhập với SELLER
- **Module:** Authentication
- **Priority:** High
- **Preconditions:** Chưa đăng nhập
- **Test data:** username=`seller`, password=`seller123`
- **Steps:**
  1. Truy cập `/login`
  2. Nhập username `seller`, password `seller123`
  3. Nhấn Đăng nhập
- **Expected:** Redirect sang trang POS (seller). Session chứa user role=SELLER

### TC-AUTH-003: Đăng nhập sai mật khẩu
- **Module:** Authentication
- **Priority:** High
- **Preconditions:** Chưa đăng nhập
- **Test data:** username=`admin`, password=`wrongpass`
- **Steps:**
  1. Truy cập `/login`
  2. Nhập username `admin`, password `wrongpass`
  3. Nhấn Đăng nhập
- **Expected:** Ở lại trang login, hiển thị thông báo lỗi "Sai tên đăng nhập hoặc mật khẩu"

### TC-AUTH-004: SELLER không truy cập được trang Admin
- **Module:** Authentication
- **Priority:** High
- **Preconditions:** Đăng nhập bằng seller
- **Test data:** N/A
- **Steps:**
  1. Đăng nhập bằng seller/seller123
  2. Truy cập trực tiếp URL `/admin/home`
- **Expected:** Bị chặn (403 hoặc redirect về trang của seller)

### TC-AUTH-005: Truy cập trang cần đăng nhập mà chưa login
- **Module:** Authentication
- **Priority:** High
- **Preconditions:** Chưa đăng nhập, clear session
- **Test data:** N/A
- **Steps:**
  1. Truy cập trực tiếp URL `/staff/inventory`
- **Expected:** Redirect về `/login`

### TC-AUTH-006: Logout xóa session
- **Module:** Authentication
- **Priority:** Medium
- **Preconditions:** Đang đăng nhập bằng admin
- **Test data:** N/A
- **Steps:**
  1. Đăng nhập admin
  2. Click Logout
  3. Truy cập lại URL admin
- **Expected:** Redirect về `/login`, session bị invalidate

---

## MODULE 2: PRODUCT CATALOG

### TC-CAT-001: Xem danh sách sản phẩm
- **Module:** Catalog
- **Priority:** High
- **Preconditions:** Đăng nhập bất kỳ role
- **Test data:** 38 sản phẩm seed
- **Steps:**
  1. Truy cập trang Catalog / danh sách sản phẩm
  2. Quan sát danh sách
- **Expected:** Hiển thị đủ 38 sản phẩm active, mỗi sản phẩm có tên, giá, unit, category

### TC-CAT-002: Filter theo category "Rau củ"
- **Module:** Catalog
- **Priority:** High
- **Preconditions:** Đang ở trang catalog
- **Test data:** 8 sản phẩm Rau củ: Rau muống, Cải thìa, Bông cải xanh, Cà rốt, Khoai tây, Hành lá, Nấm kim châm, Dưa leo
- **Steps:**
  1. Chọn filter category = "Rau củ"
  2. Quan sát kết quả
- **Expected:** Chỉ hiển thị 8 sản phẩm thuộc "Rau củ"

### TC-CAT-003: Filter theo category "Hải sản"
- **Module:** Catalog
- **Priority:** Medium
- **Preconditions:** Đang ở trang catalog
- **Test data:** 10 sản phẩm Hải sản
- **Steps:**
  1. Chọn filter category = "Hải sản"
- **Expected:** Hiển thị đúng 10 sản phẩm: Cá thu, Tôm sú, Tôm thẻ, Cá hồi phi lê, Cá basa, Mực ống, Mực nang, Cua biển, Ghẹ xanh, Nghêu, Sò điệp

### TC-CAT-004: Hiển thị đúng unit và giá
- **Module:** Catalog
- **Priority:** High
- **Preconditions:** Đang ở trang catalog
- **Test data:** Rau muống (15,000, bó), Cá hồi phi lê (450,000, kg), Xúc xích tiệt trùng (60,000, gói)
- **Steps:**
  1. Tìm sản phẩm "Rau muống" → kiểm tra giá 15,000 đ, unit "bó"
  2. Tìm "Cá hồi phi lê" → giá 450,000 đ, unit "kg"
  3. Tìm "Xúc xích tiệt trùng" → giá 60,000 đ, unit "gói"
- **Expected:** Giá và đơn vị hiển thị chính xác

### TC-CAT-005: Tiếng Việt hiển thị đúng dấu
- **Module:** Catalog
- **Priority:** High
- **Preconditions:** Đang ở trang catalog
- **Test data:** "Bông cải xanh", "Thịt gà ta", "Đậu hũ", "Sườn non", "Cải thìa"
- **Steps:**
  1. Kiểm tra các sản phẩm có dấu tiếng Việt phức tạp
  2. Xác nhận không bị lỗi encoding (mojibake, ?, □)
- **Expected:** Tất cả ký tự tiếng Việt hiển thị chính xác

### TC-CAT-006: Sản phẩm inactive không hiển thị trên catalog public
- **Module:** Catalog
- **Priority:** High
- **Preconditions:** Admin đã disable 1 sản phẩm
- **Test data:** Bất kỳ sản phẩm
- **Steps:**
  1. Admin vào product management, disable sản phẩm "Rau muống" (active=false)
  2. Đăng nhập customer, xem catalog
- **Expected:** "Rau muống" không xuất hiện trong catalog cho customer

---

## MODULE 3: SUPPLIERS

### TC-SUP-001: Xem danh sách supplier
- **Module:** Supplier
- **Priority:** High
- **Preconditions:** Đăng nhập staff
- **Test data:** "Default Supplier"
- **Steps:**
  1. Đăng nhập staff/staff123
  2. Vào trang Supplier Management
- **Expected:** Hiển thị ít nhất "Default Supplier" với email=supplier@freshmart.local, phone=0900000000, lead_time=2

### TC-SUP-002: Tìm supplier theo tên
- **Module:** Supplier
- **Priority:** Medium
- **Preconditions:** Đang ở trang supplier
- **Test data:** Keyword "Default"
- **Steps:**
  1. Nhập "Default" vào ô tìm kiếm
  2. Nhấn Search
- **Expected:** Hiển thị "Default Supplier"

### TC-SUP-003: Thêm supplier mới
- **Module:** Supplier
- **Priority:** High
- **Preconditions:** Đăng nhập staff
- **Test data:** name="Vinafood HCM", email="vinafood@test.vn", phone="0901234567", lead_time_days=5, certificate="VietGAP"
- **Steps:**
  1. Nhấn "Thêm supplier"
  2. Điền thông tin
  3. Nhấn Lưu
- **Expected:** Supplier mới xuất hiện trong danh sách

### TC-SUP-004: Import supplier từ CSV
- **Module:** Supplier
- **Priority:** Medium
- **Preconditions:** Đăng nhập staff, có file sample_suppliers.csv
- **Test data:** File sample_suppliers.csv có sẵn trong project root
- **Steps:**
  1. Vào chức năng Import CSV
  2. Upload file sample_suppliers.csv
  3. Xác nhận import
- **Expected:** Các supplier trong CSV được import thành công, không bị trùng

### TC-SUP-005: Kiểm tra lead_time_days hiển thị đúng
- **Module:** Supplier
- **Priority:** Medium
- **Preconditions:** Có ít nhất 2 supplier với lead_time khác nhau
- **Test data:** Default Supplier (lead_time=2), supplier mới (lead_time=5)
- **Steps:**
  1. Quan sát cột lead_time trong danh sách
- **Expected:** Giá trị lead_time hiển thị đúng: 2 ngày, 5 ngày

---

## MODULE 4: INVENTORY LOTS

### TC-INV-001: Xem lots của 1 sản phẩm (nhiều lot)
- **Module:** Inventory
- **Priority:** High
- **Preconditions:** Đăng nhập staff
- **Test data:** Sản phẩm "Rau muống" — Lot1: qty_in=25, expiry=today+7; Lot2: qty_in=50, expiry=today+14
- **Steps:**
  1. Vào Staff → Inventory
  2. Tìm sản phẩm "Rau muống"
  3. Xem chi tiết lots
- **Expected:** Hiển thị 2 lots với import_date, expiry_date, qty_in, qty_left, import_price=11,250 (75% × 15,000)

### TC-INV-002: Kiểm tra tổng tồn theo product
- **Module:** Inventory
- **Priority:** High
- **Preconditions:** Đăng nhập staff
- **Test data:** "Rau muống" — totalQtyLeft = 25 + 50 = 75 (ban đầu)
- **Steps:**
  1. Xem tổng availableQty cho "Rau muống"
  2. Cộng tay qty_left của từng lot
- **Expected:** availableQty = tổng qty_left của các lots chưa hết hạn

### TC-INV-003: Import_price = 75% sell_price
- **Module:** Inventory
- **Priority:** Medium
- **Preconditions:** Đăng nhập staff
- **Test data:** "Thịt bò" — sell_price=280,000 → import_price=210,000
- **Steps:**
  1. Xem lots của "Thịt bò"
  2. Kiểm tra import_price
- **Expected:** import_price = 210,000.00

### TC-INV-004: Import lot mới cho sản phẩm
- **Module:** Inventory
- **Priority:** High
- **Preconditions:** Đăng nhập staff
- **Test data:** Product "Cà rốt", supplier "Default Supplier", qty=100, expiry=today+30, import_price=18000
- **Steps:**
  1. Vào Import Lot
  2. Chọn product "Cà rốt", supplier, nhập qty/expiry/price
  3. Nhấn Import
- **Expected:** Lot mới xuất hiện, tổng tồn tăng lên

---

## MODULE 5: FEFO

### TC-FEFO-001: Lot expiry gần nhất được trừ trước
- **Module:** FEFO
- **Priority:** High
- **Preconditions:** Đăng nhập seller, "Rau muống" có 2 lots (lot1 expiry gần, lot2 expiry xa)
- **Test data:** "Rau muống" — Lot1 expiry=today+7; Lot2 expiry=today+14
- **Steps:**
  1. Seller tạo đơn WALK_IN, bán 10 "Rau muống", complete ngay
  2. Quay lại inventory xem lots
- **Expected:** Lot1 (expiry gần) bị trừ 10, Lot2 không đổi. qty_left lot1 giảm từ 25 → 15

### TC-FEFO-002: Khi lot1 hết, tiếp tục trừ lot2
- **Module:** FEFO
- **Priority:** High
- **Preconditions:** "Rau muống" — Lot1 còn 15 qty_left
- **Test data:** Bán 20 Rau muống
- **Steps:**
  1. Seller tạo đơn bán 20 "Rau muống"
  2. Kiểm tra inventory
- **Expected:** Lot1 giảm về 0, Lot2 giảm 5 (trừ tiếp phần dư)

### TC-FEFO-003: Lot expired không được coi là available
- **Module:** FEFO
- **Priority:** High
- **Preconditions:** Staff tạo lot có expiry_date = yesterday (hoặc chờ lot4-5 ngày hết hạn)
- **Test data:** Tạo lot cho "Nghêu" với expiry_date=yesterday, qty_left=40
- **Steps:**
  1. Import lot mới cho "Nghêu" với expiry=yesterday
  2. Kiểm tra availableQty trong inventory report
- **Expected:** Lot expired không tính vào availableQty. Nếu chỉ còn lot expired → sản phẩm unavailable

### TC-FEFO-004: Bán sản phẩm chỉ còn expired lots → lỗi
- **Module:** FEFO
- **Priority:** High
- **Preconditions:** Sản phẩm chỉ có lots đã expired
- **Test data:** Tạo scenario: product X chỉ còn expired lots
- **Steps:**
  1. Seller thêm sản phẩm X vào POS cart
  2. Thử checkout
- **Expected:** Hệ thống báo lỗi "Not enough stock" hoặc không cho checkout

---

## MODULE 6: LOW STOCK / OUT OF STOCK

### TC-LOW-001: Sản phẩm availableQty < 50 (threshold mặc định)
- **Module:** Low Stock
- **Priority:** High
- **Preconditions:** Đăng nhập staff, xem inventory report
- **Test data:** Hải sản có qty thấp: Cá hồi phi lê (20+20=30 total), Cua biển (20+10=30), Ghẹ xanh (20+10=30), Sò điệp (20+10=30)
- **Steps:**
  1. Vào Staff → Inventory Report
  2. Xem phần Low Stock
- **Expected:** Các sản phẩm có availableQty < 50 được highlight/liệt kê

### TC-LOW-002: Sản phẩm availableQty < 20
- **Module:** Low Stock
- **Priority:** Medium
- **Preconditions:** Bán bớt hàng để availableQty < 20
- **Test data:** Bán "Sò điệp" 15 units
- **Steps:**
  1. Sau khi bán, kiểm tra sò điệp availableQty=15
  2. Xem low stock report
- **Expected:** "Sò điệp" nằm trong danh sách low stock, cảnh báo rõ ràng

### TC-LOW-003: Sản phẩm availableQty = 0 (out of stock)
- **Module:** Low Stock
- **Priority:** High
- **Preconditions:** Bán hết toàn bộ available qty của 1 sản phẩm
- **Test data:** Sản phẩm có qty thấp
- **Steps:**
  1. Bán hết available qty
  2. Kiểm tra inventory report
- **Expected:** availableQty=0, hiển thị Out of Stock rõ ràng

### TC-LOW-004: Sản phẩm chỉ còn expired lots
- **Module:** Low Stock
- **Priority:** High
- **Preconditions:** Product có expired lots, không có active lots
- **Test data:** Tạo scenario thủ công
- **Steps:**
  1. Xem inventory report cho sản phẩm đó
- **Expected:** availableQty=0, expiredQty > 0, hệ thống phân biệt rõ "out of stock do expired"

---

## MODULE 7: UPCOMING EXPIRY / EXPIRED LOTS

### TC-EXP-001: Lô sắp hết hạn trong 7 ngày
- **Module:** Expiry
- **Priority:** High
- **Preconditions:** Đăng nhập staff
- **Test data:** Với seed hiện tại, nhiều lots rau/hải sản có shelfLife 4-7 ngày → lot1 expiry = today+4..7
- **Steps:**
  1. Vào Inventory Report
  2. Xem phần "Upcoming Expiry"
- **Expected:** Các lots có expiry trong 7 ngày tới được liệt kê. VD: lots của Nghêu (shelfLife=4), Sò điệp (4), Hải sản (5 ngày)

### TC-EXP-002: Lô sắp hết hạn trong 3 ngày
- **Module:** Expiry
- **Priority:** High
- **Preconditions:** Đổi app_settings upcoming_expiry_days = 3
- **Test data:** Lots có expiry trong 3 ngày
- **Steps:**
  1. Admin đổi setting `upcoming_expiry_days` = 3
  2. Xem lại Inventory Report
- **Expected:** Chỉ hiển thị lots expiry ≤ 3 ngày (ít hơn khi threshold=7)

### TC-EXP-003: Lot đã expired nhưng qty_left > 0
- **Module:** Expiry
- **Priority:** High
- **Preconditions:** Có lot expired chưa bị cleanup
- **Test data:** Tạo lot với expiry=yesterday, qty_left=20
- **Steps:**
  1. Xem inventory report → phần Expired Lots
- **Expected:** Lot expired hiển thị trong danh sách "Expired", có nút cleanup/xử lý

### TC-EXP-004: Dashboard highlight đúng cảnh báo
- **Module:** Expiry
- **Priority:** Medium
- **Preconditions:** Đăng nhập staff
- **Test data:** N/A
- **Steps:**
  1. Xem dashboard staff
  2. Kiểm tra badge/counter cho expired lots và upcoming expiry
- **Expected:** Số lượng hiển thị trên dashboard = số lượng trong report chi tiết

---

## MODULE 8: CHECKOUT / ORDERS

### TC-ORD-001: Tạo order WALK_IN qua Seller POS
- **Module:** Orders
- **Priority:** High
- **Preconditions:** Đăng nhập seller
- **Test data:** Bán 2 "Rau muống" (15,000/bó) + 1 "Thịt bò" (280,000/kg)
- **Steps:**
  1. Seller vào POS
  2. Thêm 2 Rau muống, 1 Thịt bò
  3. Chọn payment method = CASH
  4. Nhấn Checkout (complete ngay)
- **Expected:**
  - Order tạo thành công, type=WALK_IN, status=COMPLETED
  - line_total Rau muống = 2 × 15,000 = 30,000
  - line_total Thịt bò = 1 × 280,000 = 280,000
  - total_amount = 310,000
  - completed_at ≠ null
  - customer_id = null (WALK_IN)
  - created_by = seller

### TC-ORD-002: Tạo order ONLINE qua Customer checkout
- **Module:** Orders
- **Priority:** High
- **Preconditions:** Đăng nhập customer
- **Test data:** Thêm 3 "Cam sành" (45,000/kg) vào cart
- **Steps:**
  1. Customer thêm sản phẩm vào cart
  2. Nhấn Checkout
- **Expected:**
  - Order tạo: type=ONLINE, status=PENDING, payment_method=COD
  - customer_id = customer user ID
  - created_by = null
  - total_amount = 3 × 45,000 = 135,000
  - Cart được clear

### TC-ORD-003: Kiểm tra line_total = quantity × unit_price
- **Module:** Orders
- **Priority:** High
- **Preconditions:** Có order vừa tạo
- **Test data:** Order từ TC-ORD-001
- **Steps:**
  1. Xem chi tiết order
  2. Với mỗi order_item, tính quantity × unit_price
- **Expected:** line_total đúng cho mỗi item

### TC-ORD-004: Kiểm tra total_amount = sum(line_total)
- **Module:** Orders
- **Priority:** High
- **Preconditions:** Có order với nhiều items
- **Test data:** Order từ TC-ORD-001
- **Steps:**
  1. Cộng tay tất cả line_total
  2. So sánh với total_amount
- **Expected:** total_amount = sum(line_total)

### TC-ORD-005: Order status transitions
- **Module:** Orders
- **Priority:** High
- **Preconditions:** Có order ONLINE (PENDING) từ TC-ORD-002
- **Test data:** Order vừa tạo
- **Steps:**
  1. Staff đổi status PENDING → PROCESSING
  2. Staff đổi PROCESSING → SHIPPING
  3. Staff đổi SHIPPING → COMPLETED
  4. Kiểm tra completed_at được set
- **Expected:** Mỗi transition thành công. Valid: PENDING→PROCESSING→SHIPPING→COMPLETED. completed_at ≠ null khi COMPLETED

### TC-ORD-006: Từ chối transition không hợp lệ
- **Module:** Orders
- **Priority:** High
- **Preconditions:** Có order PENDING
- **Test data:** Order PENDING
- **Steps:**
  1. Thử đổi PENDING → COMPLETED (bỏ qua PROCESSING/SHIPPING)
- **Expected:** Báo lỗi "Invalid status transition"

### TC-ORD-007: Cancel order
- **Module:** Orders
- **Priority:** Medium
- **Preconditions:** Có order PENDING hoặc PROCESSING
- **Test data:** Order PENDING
- **Steps:**
  1. Staff đổi status → CANCELED
- **Expected:** Order chuyển CANCELED. Không thể chuyển lại status khác

---

## MODULE 9: ORDER HISTORY

### TC-HIST-001: Filter theo type WALK_IN
- **Module:** Order History
- **Priority:** Medium
- **Preconditions:** Có cả WALK_IN và ONLINE orders
- **Test data:** Orders từ TC-ORD-001, TC-ORD-002
- **Steps:**
  1. Vào Order list
  2. Filter type = WALK_IN
- **Expected:** Chỉ hiển thị orders loại WALK_IN

### TC-HIST-002: Filter theo type ONLINE
- **Module:** Order History
- **Priority:** Medium
- **Preconditions:** Có orders ONLINE
- **Test data:** N/A
- **Steps:**
  1. Filter type = ONLINE
- **Expected:** Chỉ hiển thị orders ONLINE, customer_id ≠ null

### TC-HIST-003: Filter theo status COMPLETED
- **Module:** Order History
- **Priority:** Medium
- **Preconditions:** Có orders COMPLETED
- **Steps:**
  1. Filter status = COMPLETED
- **Expected:** Chỉ orders COMPLETED, tất cả có completed_at

### TC-HIST-004: Xem chi tiết order
- **Module:** Order History
- **Priority:** High
- **Preconditions:** Có order
- **Steps:**
  1. Click vào 1 order trong danh sách
- **Expected:** Hiển thị đầy đủ: order_code, type, status, items (product, qty, price, line_total), total_amount, created_at

### TC-HIST-005: customer_id null hợp lệ cho WALK_IN
- **Module:** Order History
- **Priority:** Medium
- **Preconditions:** Có order WALK_IN
- **Steps:**
  1. Xem detail order WALK_IN
- **Expected:** customer_id = null/trống. Không hiển thị lỗi, ghi "Khách vãng lai" hoặc tương tự

---

## MODULE 10: REVENUE DASHBOARD + FORECAST

### TC-REV-001: Revenue cập nhật khi order COMPLETED
- **Module:** Revenue
- **Priority:** High
- **Preconditions:** Vừa complete 1 order
- **Test data:** Order 310,000 từ TC-ORD-001
- **Steps:**
  1. Complete order
  2. Xem revenue dashboard (cần PRO)
- **Expected:** revenue_daily cho ngày hôm nay += 310,000

### TC-REV-002: Revenue chart hiển thị đúng
- **Module:** Revenue
- **Priority:** High
- **Preconditions:** Đăng nhập PRO user (admin hoặc customer PRO)
- **Steps:**
  1. Vào Pro Dashboard → Revenue tab
  2. Kiểm tra chart
- **Expected:** Chart hiển thị dữ liệu theo ngày, chính xác theo revenue_daily

### TC-REV-003: Forecast tab hiển thị
- **Module:** Revenue
- **Priority:** Medium
- **Preconditions:** Có ít nhất vài ngày revenue data
- **Steps:**
  1. Vào Pro Dashboard → Forecast tab
  2. Kiểm tra forecast data
- **Expected:** Forecast hiển thị predicted values dựa trên historical data

### TC-REV-004: Doanh thu 30 ngày gần nhất
- **Module:** Revenue
- **Priority:** Medium
- **Preconditions:** Có revenue data
- **Steps:**
  1. Xem revenue summary 30 ngày
  2. So sánh với SQL query
- **Expected:** Tổng hiển thị khớp với query DB

---

## MODULE 11: SUBSCRIPTION

### TC-SUB-001: Customer FREE hiển thị đúng
- **Module:** Subscription
- **Priority:** High
- **Preconditions:** Đăng nhập customer đã hết PRO (hoặc tạo customer mới FREE)
- **Test data:** Customer FREE
- **Steps:**
  1. Vào trang Upgrade
- **Expected:** Hiển thị tier=FREE, có nút Upgrade PRO, hiển thị 3 plans: 30 ngày (99,000đ), 90 ngày (249,000đ), 365 ngày (799,000đ)

### TC-SUB-002: Customer PRO còn hạn
- **Module:** Subscription
- **Priority:** High
- **Preconditions:** Đăng nhập customer (PRO, +30 ngày)
- **Test data:** customer/customer123
- **Steps:**
  1. Xem trang subscription status
- **Expected:** Hiển thị tier=PRO, remaining days ≈ 30, status=PRO_ACTIVE

### TC-SUB-003: Customer PRO sắp hết hạn (≤7 ngày)
- **Module:** Subscription
- **Priority:** High
- **Preconditions:** Customer có expired_date = today + 5
- **Test data:** Cần set expired_date qua Admin
- **Steps:**
  1. Admin grant PRO 5 ngày cho customer test
  2. Đăng nhập customer
  3. Xem subscription status
- **Expected:** Status=PRO_EXPIRING_SOON, cảnh báo sắp hết hạn, đề xuất gia hạn

### TC-SUB-004: Customer hết hạn PRO → downgrade về FREE
- **Module:** Subscription
- **Priority:** High
- **Preconditions:** Customer với expired_date = yesterday
- **Test data:** Cần set expired_date qua DB hoặc admin
- **Steps:**
  1. Truy cập bất kỳ trang nào yêu cầu refresh tier
  2. Kiểm tra tier
- **Expected:** Tier tự chuyển FREE. tier_history ghi nhận record: change_type=EXPIRE, old_tier=PRO, new_tier=FREE

### TC-SUB-005: Upgrade PRO thành công
- **Module:** Subscription
- **Priority:** High
- **Preconditions:** Customer FREE
- **Steps:**
  1. Vào trang Upgrade
  2. Chọn plan 30 ngày
  3. Nhấn Upgrade
- **Expected:** Tier=PRO, expired_date=today+30, subscription_payment record tạo (amount=99,000), tier_history ghi nhận UPGRADE

### TC-SUB-006: Admin revoke PRO
- **Module:** Subscription
- **Priority:** High
- **Preconditions:** Đăng nhập admin, customer đang PRO
- **Steps:**
  1. Vào Admin → Subscription Management
  2. Chọn customer
  3. Nhấn Revoke PRO
- **Expected:** Customer tier=FREE, expired_date=null, tier_history ghi nhận ADMIN_REVOKE

### TC-SUB-007: Kiểm tra tier_history phản ánh đúng
- **Module:** Subscription
- **Priority:** Medium
- **Preconditions:** Đã thực hiện upgrade/renew/revoke
- **Steps:**
  1. Xem tier_history cho customer
- **Expected:** Có đủ records với đúng change_type (UPGRADE/RENEW/EXPIRE/ADMIN_GRANT/ADMIN_REVOKE)

### TC-SUB-008: PRO user truy cập PRO features
- **Module:** Subscription
- **Priority:** High
- **Preconditions:** User PRO active
- **Steps:**
  1. Truy cập /pro/dashboard
- **Expected:** Truy cập thành công, hiển thị dashboard PRO

### TC-SUB-009: FREE user không truy cập PRO features
- **Module:** Subscription
- **Priority:** High
- **Preconditions:** Customer FREE
- **Steps:**
  1. Truy cập /pro/dashboard
- **Expected:** Bị chặn (redirect hoặc hiển thị trang upgrade)

---

## MODULE 12: APP SETTINGS

### TC-SET-001: Kiểm tra đủ 8 setting keys
- **Module:** Settings
- **Priority:** High
- **Preconditions:** Đăng nhập admin
- **Test data:** 8 keys mặc định
- **Steps:**
  1. Vào Admin → App Settings
  2. Kiểm tra danh sách keys
- **Expected:** Có đủ 8 keys với default values:

| Key | Default | Mô tả |
|-----|---------|-------|
| low_stock_threshold | 50 | Ngưỡng cảnh báo tồn kho thấp |
| upcoming_expiry_days | 7 | Số ngày cảnh báo cận hạn |
| replenish_history_days | 30 | Số ngày lịch sử gợi ý nhập hàng |
| replenish_lead_days | 3 | Lead time mặc định |
| replenish_buffer_days | 2 | Buffer days mặc định |
| replenish_safety_days | 2 | Safety days mặc định |
| subscription_notify_days | 7 | Ngày cảnh báo trước hết hạn |
| subscription_grace_period_days | 3 | Grace period sau hết hạn |

### TC-SET-002: Thay đổi low_stock_threshold ảnh hưởng report
- **Module:** Settings
- **Priority:** High
- **Preconditions:** Admin
- **Steps:**
  1. Đổi low_stock_threshold từ 50 → 20
  2. Xem Inventory Report → Low Stock
- **Expected:** Danh sách low stock giảm (chỉ sản phẩm availableQty < 20)

### TC-SET-003: Giá trị âm bị reject
- **Module:** Settings
- **Priority:** Medium
- **Preconditions:** Admin
- **Steps:**
  1. Thử set low_stock_threshold = -10
- **Expected:** Hệ thống sanitize về default (50) hoặc giữ giá trị cũ. Không chấp nhận giá trị âm

---

# C. TỔNG HỢP TEST CASES

| Module | Số TC | High | Medium | Low |
|--------|-------|------|--------|-----|
| Authentication | 6 | 5 | 1 | 0 |
| Catalog | 6 | 4 | 1 | 1 |
| Suppliers | 5 | 2 | 3 | 0 |
| Inventory Lots | 4 | 3 | 1 | 0 |
| FEFO | 4 | 4 | 0 | 0 |
| Low Stock | 4 | 2 | 1 | 1 |
| Expiry | 4 | 2 | 2 | 0 |
| Orders | 7 | 5 | 2 | 0 |
| Order History | 5 | 1 | 4 | 0 |
| Revenue | 4 | 2 | 2 | 0 |
| Subscription | 9 | 6 | 2 | 1 |
| App Settings | 3 | 2 | 1 | 0 |
| **TỔNG** | **61** | **38** | **20** | **3** |

---

# D. SQL VALIDATION QUERIES

```sql
-- 1. Count users theo role
SELECT role, COUNT(*) AS cnt FROM users GROUP BY role;
-- Expected: ADMIN=1, STAFF=1, SELLER=1, CUSTOMER=1

-- 2. Count products theo category
SELECT category, COUNT(*) AS cnt FROM products WHERE active = 1 GROUP BY category ORDER BY cnt DESC;
-- Expected: Hải sản=10, Rau củ=8, Thịt=7, TPCBS=7, Trái cây=7 (total=39 nếu thêm, 38 ban đầu)

-- 3. Lots: Active vs Expired vs Expiring Soon
SELECT
    SUM(CASE WHEN expiry_date >= CAST(GETDATE() AS DATE) AND qty_left > 0 THEN 1 ELSE 0 END) AS active_lots,
    SUM(CASE WHEN expiry_date < CAST(GETDATE() AS DATE) AND qty_left > 0 THEN 1 ELSE 0 END) AS expired_lots,
    SUM(CASE WHEN expiry_date BETWEEN CAST(GETDATE() AS DATE) AND DATEADD(day, 7, CAST(GETDATE() AS DATE)) AND qty_left > 0 THEN 1 ELSE 0 END) AS expiring_soon
FROM product_lots;

-- 4. Top 10 sản phẩm availableQty thấp nhất
SELECT TOP 10
    p.id, p.name,
    COALESCE(SUM(CASE WHEN l.expiry_date >= CAST(GETDATE() AS DATE) THEN l.qty_left ELSE 0 END), 0) AS available_qty
FROM products p
LEFT JOIN product_lots l ON l.product_id = p.id AND l.qty_left > 0
WHERE p.active = 1
GROUP BY p.id, p.name
ORDER BY available_qty ASC;

-- 5. Orders theo type và status
SELECT type, status, COUNT(*) AS cnt FROM orders GROUP BY type, status ORDER BY type, status;

-- 6. Đối chiếu order total vs sum(line_total)
SELECT o.id, o.order_code, o.total_amount,
    (SELECT SUM(oi.line_total) FROM order_items oi WHERE oi.order_id = o.id) AS calc_total,
    o.total_amount - (SELECT COALESCE(SUM(oi.line_total), 0) FROM order_items oi WHERE oi.order_id = o.id) AS diff
FROM orders o
ORDER BY o.id;
-- Expected: diff phải = 0 cho tất cả orders

-- 7. Kiểm tra line_total = quantity * unit_price
SELECT oi.id, oi.order_id, oi.quantity, oi.unit_price, oi.line_total,
    (oi.quantity * oi.unit_price) AS expected_line_total,
    oi.line_total - (oi.quantity * oi.unit_price) AS diff
FROM order_items oi WHERE oi.line_total <> (oi.quantity * oi.unit_price);
-- Expected: không có dòng nào (0 rows)

-- 8. Revenue 30 ngày gần nhất
SELECT revenue_date, total_revenue
FROM revenue_daily
WHERE revenue_date >= DATEADD(day, -30, CAST(GETDATE() AS DATE))
ORDER BY revenue_date DESC;

-- 9. Users theo tier và subscription status
SELECT u.id, u.username, u.role, u.tier, u.expired_date,
    CASE
        WHEN u.tier = 'PRO' AND u.expired_date >= CAST(GETDATE() AS DATE) THEN 'PRO_ACTIVE'
        WHEN u.tier = 'PRO' AND u.expired_date < CAST(GETDATE() AS DATE) THEN 'PRO_EXPIRED'
        WHEN u.tier = 'FREE' THEN 'FREE'
    END AS sub_status
FROM users u WHERE u.role = 'CUSTOMER';

-- 10. Tier history log
SELECT th.id, u.username, th.old_tier, th.new_tier, th.change_type, th.note, th.created_at
FROM tier_history th JOIN users u ON th.user_id = u.id
ORDER BY th.created_at DESC;

-- 11. Subscription payments
SELECT sp.payment_code, u.username, sp.plan_name, sp.amount, sp.payment_status, sp.start_date, sp.end_date
FROM subscription_payments sp JOIN users u ON sp.user_id = u.id
ORDER BY sp.created_at DESC;

-- 12. Tổng giá trị tồn kho (available only)
SELECT COALESCE(SUM(l.qty_left * COALESCE(l.import_price, 0)), 0) AS total_inventory_value
FROM product_lots l
WHERE l.qty_left > 0 AND l.expiry_date >= CAST(GETDATE() AS DATE);
```

---

# E. BUG ORACLE / KỲ VỌNG NGHIỆP VỤ

## ✅ Hành vi ĐÚNG

| # | Hành vi | Giải thích |
|---|---------|------------|
| 1 | availableQty chỉ tính lots chưa hết hạn (expiry >= today) | Expired lots phải loại trừ |
| 2 | FEFO trừ lot expiry sớm nhất trước | Giảm lãng phí thực phẩm |
| 3 | WALK_IN order có customer_id = NULL | Khách vãng lai không cần tài khoản |
| 4 | ONLINE order không trừ tồn khi tạo, chỉ trừ khi COMPLETED | Tránh lock hàng sớm, hủy order thì ko mất tồn |
| 5 | completed_at chỉ có giá trị khi status=COMPLETED | Logic thời gian hoàn thành |
| 6 | PRO hết hạn → tự chuyển FREE + ghi tier_history | Downgrade tự động |
| 7 | line_total = quantity × unit_price (chính xác) | Integrity tài chính |
| 8 | total_amount = SUM(line_total) | Integrity tài chính |
| 9 | revenue_daily chỉ cộng khi order COMPLETED | Doanh thu thực tế |
| 10 | Settings giá trị âm bị sanitize về default | Bảo vệ logic |

## ❌ Hành vi là BUG

| # | Hành vi | Mức độ |
|---|---------|--------|
| 1 | Lot expired vẫn tính vào availableQty | 🔴 Critical |
| 2 | FEFO trừ lot có expiry xa hơn trước | 🔴 Critical |
| 3 | Bán được sản phẩm inactive | 🔴 Critical |
| 4 | total_amount ≠ SUM(line_total) | 🔴 Critical |
| 5 | line_total ≠ quantity × unit_price | 🔴 Critical |
| 6 | Order COMPLETED mà completed_at = null | 🟡 Major |
| 7 | Order CANCELED vẫn cho chuyển status khác | 🟡 Major |
| 8 | PENDING → COMPLETED (skip PROCESSING/SHIPPING) | 🟡 Major |
| 9 | Customer FREE truy cập được Pro features | 🟡 Major |
| 10 | PRO hết hạn nhưng tier không chuyển FREE | 🟡 Major |
| 11 | Revenue cộng khi order ở trạng thái khác COMPLETED | 🟡 Major |
| 12 | Tiếng Việt bị lỗi encoding (mojibake) | 🟡 Major |
| 13 | Low stock threshold thay đổi nhưng report không phản ánh | 🟠 Minor |
| 14 | Settings chấp nhận string/text cho field integer | 🟠 Minor |
| 15 | Import lot với qty_in ≤ 0 thành công | 🟠 Minor |
| 16 | Import lot với expiry_date < import_date thành công | 🟠 Minor |

## ⚠️ Lỗi phổ biến ở hệ thống thực phẩm tươi có FEFO

1. **Không exclude expired lots khỏi available qty** — Đây là lỗi #1 phổ biến nhất
2. **FEFO order sai khi nhiều lots cùng expiry_date** — Cần deterministic ordering (by lot ID)
3. **Race condition khi 2 seller bán cùng lúc** — lot qty_left có thể bị âm
4. **Lô hết hạn trong ngày bán** — expiry_date = today: lot này available hay expired? (Hệ thống hiện tại: `expiry >= today` → available ✅)
5. **Không alert khi hàng sắp hết hạn mà tồn kho lớn** — Rủi ro lãng phí
6. **Import_price thay đổi nhưng sell_price cũ** — Margin bị ảnh hưởng
7. **Lot qty_left âm** — Khi bán vượt quá tồn, phải validate trước

---

# F. TEST EXECUTION ORDER

## 🏃 Phase 1: SMOKE TEST (10-15 phút)

| # | Test Case | Thời gian |
|---|-----------|-----------|
| 1 | TC-AUTH-001: Login admin | 1 phút |
| 2 | TC-AUTH-002: Login seller | 1 phút |
| 3 | TC-CAT-001: Xem catalog | 1 phút |
| 4 | TC-CAT-005: Tiếng Việt | 1 phút |
| 5 | TC-INV-001: Xem lots | 2 phút |
| 6 | TC-ORD-001: Tạo WALK_IN order | 3 phút |
| 7 | TC-SUB-002: Customer PRO check | 1 phút |
| 8 | TC-SET-001: Kiểm tra settings | 1 phút |
| 9 | TC-LOW-001: Low stock report | 1 phút |
| 10 | TC-SUP-001: Xem suppliers | 1 phút |

**Tổng: ~13 phút. Nếu 1 trong 10 TC này fail → DỪNG, report bug trước khi chạy tiếp.**

## 🔄 Phase 2: REGRESSION TEST — Core Flows (45-60 phút)

**Flow 1: Seller checkout end-to-end**
1. TC-AUTH-002 → TC-ORD-001 → TC-FEFO-001 → TC-FEFO-002 → TC-ORD-003 → TC-ORD-004

**Flow 2: Customer order lifecycle**
2. TC-ORD-002 → TC-ORD-005 → TC-ORD-006 → TC-ORD-007

**Flow 3: Inventory management**
3. TC-INV-001 → TC-INV-002 → TC-INV-004 → TC-LOW-001 → TC-LOW-003

**Flow 4: Expiry monitoring**
4. TC-EXP-001 → TC-EXP-002 → TC-EXP-003 → TC-EXP-004

**Flow 5: Subscription lifecycle**
5. TC-SUB-001 → TC-SUB-005 → TC-SUB-002 → TC-SUB-003 → TC-SUB-004 → TC-SUB-007

**Flow 6: Revenue tracking**
6. TC-REV-001 → TC-REV-002 → TC-REV-004

**Flow 7: Settings → Report impact**
7. TC-SET-001 → TC-SET-002

## 🔬 Phase 3: EDGE CASES & NEGATIVE (20-30 phút)

| # | Test | Mục đích |
|---|------|----------|
| 1 | TC-AUTH-004: SELLER → Admin URL | Authorization bypass |
| 2 | TC-AUTH-005: No login → Protected URL | Authentication bypass |
| 3 | TC-FEFO-003: Expired lot = unavailable | FEFO integrity |
| 4 | TC-FEFO-004: Bán khi chỉ còn expired | Edge case critical |
| 5 | TC-ORD-006: Invalid transition | State machine |
| 6 | TC-CAT-006: Inactive product ẩn | Access control |
| 7 | TC-LOW-004: Chỉ expired lots | Out of stock edge |
| 8 | TC-SUB-009: FREE → Pro features | Tier filter |
| 9 | TC-SUB-006: Admin revoke | Admin power |
| 10 | TC-SET-003: Giá trị âm | Input validation |

---

# G. SẢN PHẨM TIÊU BIỂU ĐỂ TEST NHANH

## Quick Reference — Sản phẩm hay dùng

| Sản phẩm | Category | Price | Qty seed | ShelfLife | Dùng test |
|-----------|----------|-------|----------|-----------|-----------|
| Rau muống | Rau củ | 15,000 | 50 | 7 ngày | FEFO, low stock |
| Thịt bò | Thịt | 280,000 | 40 | 5 ngày | Checkout tổng tiền lớn |
| Cá hồi phi lê | Hải sản | 450,000 | 20 | 5 ngày | Low stock (qty nhỏ nhất) |
| Nghêu | Hải sản | 80,000 | 40 | 4 ngày | Expiry sớm nhất |
| Sò điệp | Hải sản | 260,000 | 20 | 4 ngày | Low stock + expiry sớm |
| Xúc xích tiệt trùng | TPCBS | 60,000 | 80 | 60 ngày | ShelfLife dài nhất |
| Cá viên chiên | TPCBS | 45,000 | 80 | 90 ngày | Qty lớn, shelf dài |
| Đậu hũ | TPCBS | 15,000 | 80 | 10 ngày | Tiếng Việt có dấu đặc biệt |

## Lot details (seed-generated, tính cho ngày hôm nay)

Mỗi product có 2 lots:
- **Lot 1:** qty_in = seedQty/2, import_date = today-2, expiry = today + shelfLifeDays
- **Lot 2:** qty_in = seedQty, import_date = today-1, expiry = today + shelfLifeDays×2

VD "Nghêu" (shelfLife=4, qty=40):
- Lot 1: qty_in=20, expiry=today+4, import_price=60,000
- Lot 2: qty_in=40, expiry=today+8, import_price=60,000
- **AvailableQty = 60** (cả 2 lots chưa expired)

VD "Cá hồi phi lê" (shelfLife=5, qty=20):
- Lot 1: qty_in=10, expiry=today+5, import_price=337,500
- Lot 2: qty_in=20, expiry=today+10, import_price=337,500
- **AvailableQty = 30**

---

> **Ghi chú cuối:** Chạy Phase 1 smoke test trước khi bắt đầu UAT chính thức. Nếu smoke test pass 100%, tiến hành Phase 2 + Phase 3. Mỗi bug tìm được ghi lại kèm: TC-ID, mô tả, screenshot, severity, reproduction steps.
