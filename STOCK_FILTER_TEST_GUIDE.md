# Hướng dẫn Test Filter Trạng thái Tồn kho

## Tóm tắt thay đổi

### Files đã sửa:
1. **src/main/java/com/freshmart/web/servlet/CatalogServlet.java**
   - Thêm InventoryService và JpaExecutor
   - Tính toán availableQty cho mỗi product và lưu vào availableQtyMap
   - Truyền availableQtyMap vào JSP

2. **src/main/webapp/WEB-INF/jsp/catalog/catalog.jsp**
   - Thêm badge hiển thị trạng thái tồn kho (Còn hàng/Hết hàng) với số lượng
   - Disable nút "Thêm vào giỏ" khi hết hàng
   - Thêm max attribute cho input số lượng dựa trên tồn kho thực tế
   - Áp dụng cho cả chế độ grouped và filtered

3. **src/main/java/com/freshmart/repository/ProductRepository.java** (đã có sẵn)
   - Logic filter stockStatus đã được implement
   - Method getAvailableQty() tính tổng qtyLeft từ ProductLot

## Logic xác định trạng thái tồn kho

### Công thức tính Available Quantity:
```sql
SELECT COALESCE(SUM(l.qtyLeft), 0) 
FROM ProductLot l 
WHERE l.product.id = :productId 
  AND l.qtyLeft > 0 
  AND l.expiryDate >= :today
```

### Phân loại trạng thái:
- **inStock (Còn hàng)**: availableQty > 0
- **outOfStock (Hết hàng)**: availableQty = 0
- **all (Tất cả)**: Không filter

### Điều kiện:
- Chỉ tính các lot còn số lượng (qtyLeft > 0)
- Chỉ tính các lot chưa hết hạn (expiryDate >= today)
- Không tính các product inactive (active = false)

## Các bước test tay

### 1. Chuẩn bị dữ liệu test

#### Tạo sản phẩm có hàng:
1. Vào Staff → Products → Thêm sản phẩm mới (ví dụ: "Cá hồi")
2. Vào Staff → Inventory → Nhập lô hàng cho sản phẩm này
   - Số lượng: 100
   - Ngày hết hạn: Chọn ngày trong tương lai (ví dụ: 30 ngày sau)

#### Tạo sản phẩm hết hàng:
1. Thêm sản phẩm mới (ví dụ: "Tôm sú")
2. Không nhập lô hàng HOẶC nhập lô đã hết hạn

#### Tạo sản phẩm có lô hết hạn:
1. Thêm sản phẩm (ví dụ: "Rau cải")
2. Nhập lô với ngày hết hạn trong quá khứ
3. Sản phẩm này sẽ hiển thị là "Hết hàng"

### 2. Test filter "Tất cả" (all)

**Bước thực hiện:**
1. Truy cập `/catalog`
2. Dropdown "Trạng thái" chọn "Tất cả"
3. Click "Tìm"

**Kết quả mong đợi:**
- Hiển thị tất cả sản phẩm active
- Mỗi sản phẩm có badge:
  - Xanh "Còn hàng: X" nếu có tồn kho
  - Đỏ "Hết hàng" nếu không có tồn kho
- Sản phẩm còn hàng: Hiển thị form thêm vào giỏ
- Sản phẩm hết hàng: Nút "Hết hàng" bị disable

### 3. Test filter "Còn hàng" (inStock)

**Bước thực hiện:**
1. Truy cập `/catalog`
2. Dropdown "Trạng thái" chọn "Còn hàng"
3. Click "Tìm"

**Kết quả mong đợi:**
- Chỉ hiển thị sản phẩm có availableQty > 0
- Tất cả sản phẩm đều có badge xanh "Còn hàng: X"
- Tất cả đều có form thêm vào giỏ hoạt động
- Không hiển thị sản phẩm hết hàng

### 4. Test filter "Hết hàng" (outOfStock)

**Bước thực hiện:**
1. Truy cập `/catalog`
2. Dropdown "Trạng thái" chọn "Hết hàng"
3. Click "Tìm"

**Kết quả mong đợi:**
- Chỉ hiển thị sản phẩm có availableQty = 0
- Tất cả sản phẩm đều có badge đỏ "Hết hàng"
- Tất cả đều có nút "Hết hàng" bị disable
- Không hiển thị sản phẩm còn hàng

### 5. Test kết hợp với search keyword

**Bước thực hiện:**
1. Nhập keyword "cá" vào ô tìm kiếm
2. Chọn "Còn hàng" ở dropdown trạng thái
3. Click "Tìm"

**Kết quả mong đợi:**
- Chỉ hiển thị sản phẩm có tên chứa "cá" VÀ còn hàng
- Filter hoạt động đúng với cả 2 điều kiện

### 6. Test kết hợp với category

**Bước thực hiện:**
1. Chọn category "Hải sản"
2. Chọn "Hết hàng" ở dropdown trạng thái
3. Click "Tìm"

**Kết quả mong đợi:**
- Chỉ hiển thị sản phẩm thuộc "Hải sản" VÀ hết hàng
- Filter hoạt động đúng với cả 2 điều kiện

### 7. Test kết hợp với sort

**Bước thực hiện:**
1. Chọn "Còn hàng"
2. Chọn sort "Giá tăng"
3. Click "Tìm"

**Kết quả mong đợi:**
- Hiển thị sản phẩm còn hàng
- Sắp xếp theo giá tăng dần
- Cả filter và sort hoạt động cùng lúc

### 8. Test pagination

**Bước thực hiện:**
1. Đảm bảo có > 12 sản phẩm còn hàng
2. Chọn "Còn hàng"
3. Click "Tìm"
4. Click nút "Tiếp" để sang trang 2

**Kết quả mong đợi:**
- Trang 1 hiển thị 12 sản phẩm đầu
- Trang 2 hiển thị các sản phẩm tiếp theo
- Filter vẫn được giữ khi chuyển trang
- URL có param: `?stockStatus=inStock&page=2`

### 9. Test thêm vào giỏ hàng

**Bước thực hiện:**
1. Tìm sản phẩm còn hàng (ví dụ: 50 cái)
2. Nhập số lượng = 10
3. Click "Thêm"

**Kết quả mong đợi:**
- Thêm vào giỏ thành công
- Input số lượng có max="${availableQtyMap[p.id]}"
- Không thể nhập số lượng > tồn kho

### 10. Test product detail không bị ảnh hưởng

**Bước thực hiện:**
1. Từ catalog, click "Chi tiết" trên bất kỳ sản phẩm nào
2. Kiểm tra trang product detail

**Kết quả mong đợi:**
- Trang product detail hiển thị bình thường
- Không có lỗi
- Thông tin sản phẩm đầy đủ

### 11. Test với sản phẩm inactive

**Bước thực hiện:**
1. Vào Staff → Products
2. Set một sản phẩm thành inactive (active = false)
3. Quay lại catalog với filter "Tất cả"

**Kết quả mong đợi:**
- Sản phẩm inactive KHÔNG hiển thị
- Logic ẩn inactive vẫn hoạt động đúng

### 12. Test xóa filter

**Bước thực hiện:**
1. Áp dụng filter "Còn hàng" + keyword + category
2. Click nút "X" (Xóa bộ lọc)

**Kết quả mong đợi:**
- Quay về catalog mặc định
- Hiển thị tất cả sản phẩm theo nhóm category
- Tất cả filter bị reset

## Các trường hợp edge case cần test

### 1. Sản phẩm có nhiều lô
- Tạo sản phẩm với 3 lô: 10, 20, 30 cái
- Kết quả: Badge hiển thị "Còn hàng: 60"

### 2. Sản phẩm có lô hết hạn và chưa hết hạn
- Lô 1: 50 cái, hết hạn hôm qua
- Lô 2: 30 cái, hết hạn 10 ngày sau
- Kết quả: Badge hiển thị "Còn hàng: 30"

### 3. Sản phẩm có lô qtyLeft = 0
- Lô 1: qtyIn=100, qtyLeft=0 (đã bán hết)
- Lô 2: qtyIn=50, qtyLeft=50
- Kết quả: Badge hiển thị "Còn hàng: 50"

### 4. Không có sản phẩm nào match filter
- Filter "Hết hàng" nhưng tất cả sản phẩm đều còn hàng
- Kết quả: Hiển thị danh sách rỗng (không có sản phẩm)

## Checklist tổng hợp

- [ ] Filter "Tất cả" hiển thị đúng
- [ ] Filter "Còn hàng" chỉ hiển thị sản phẩm có tồn kho
- [ ] Filter "Hết hàng" chỉ hiển thị sản phẩm không có tồn kho
- [ ] Badge trạng thái hiển thị đúng màu và số lượng
- [ ] Nút "Thêm vào giỏ" disable khi hết hàng
- [ ] Input số lượng có max dựa trên tồn kho thực tế
- [ ] Filter hoạt động với search keyword
- [ ] Filter hoạt động với category
- [ ] Filter hoạt động với sort
- [ ] Filter hoạt động với pagination
- [ ] URL params được giữ khi chuyển trang
- [ ] Product detail không bị ảnh hưởng
- [ ] Sản phẩm inactive vẫn bị ẩn
- [ ] Grouped mode hiển thị đúng trạng thái
- [ ] Filtered mode hiển thị đúng trạng thái
- [ ] Xóa filter hoạt động đúng

## Lưu ý khi test

1. **Làm mới dữ liệu**: Sau mỗi lần test thêm/xóa lô hàng, refresh trang catalog
2. **Kiểm tra console**: Mở Developer Tools để xem có lỗi JavaScript không
3. **Kiểm tra network**: Xem request có đúng params không
4. **Test trên nhiều trình duyệt**: Chrome, Firefox, Edge
5. **Test responsive**: Kiểm tra trên mobile, tablet
