# Hướng dẫn Upload Ảnh Product

## Tóm tắt thay đổi

Đã bổ sung chức năng upload file ảnh thật cho create/edit product mà không thay đổi kiến trúc hiện có.

## File đã sửa/thêm

### 1. `src/main/webapp/WEB-INF/jsp/staff/product_form.jsp`
- Thêm `enctype="multipart/form-data"` vào form
- Thêm input file `imageFile` với accept image types
- Thêm text hướng dẫn người dùng có thể chọn URL hoặc upload file

### 2. `src/main/java/com/freshmart/web/servlet/staff/ProductManagementServlet.java`
- Thêm annotation `@MultipartConfig` với giới hạn file 5MB
- Thêm xử lý upload file trong `doPost()`:
  - Đọc Part từ request
  - Validate extension (jpg, jpeg, png, gif, webp)
  - Sanitize filename để tránh path traversal
  - Lưu file với tên: `timestamp_sanitizedName`
  - Trả về đường dẫn web-accessible
- Thêm 4 helper methods:
  - `getSubmittedFileName()`: Extract tên file từ Part header
  - `isValidImageFile()`: Validate extension
  - `saveUploadedFile()`: Lưu file và trả về path
  - `sanitizeFileName()`: Làm sạch tên file
- Logic ưu tiên: uploaded file > imageUrl > giữ ảnh cũ

### 3. `src/main/webapp/assets/uploads/products/.gitkeep`
- Tạo thư mục lưu ảnh upload
- File .gitkeep để git track thư mục rỗng

### 4. `.gitignore`
- Thêm ignore cho file upload: `src/main/webapp/assets/uploads/products/*`
- Giữ lại .gitkeep: `!src/main/webapp/assets/uploads/products/.gitkeep`

## Nơi lưu ảnh

Ảnh được lưu tại: `src/main/webapp/assets/uploads/products/`

Đường dẫn web-accessible: `{contextPath}/assets/uploads/products/{timestamp}_{filename}`

Ví dụ: `/freshmart/assets/uploads/products/1234567890_product_image.jpg`

## Các bước test tay

1. **Test Create Product với Upload File:**
   - Vào `/staff/products?action=add`
   - Điền thông tin product (name, category, unit, price)
   - Chọn file ảnh (jpg/png/gif/webp, < 5MB)
   - Không điền Image URL
   - Click "Create Product"
   - Kiểm tra: Product được tạo, ảnh hiển thị trong list và detail

2. **Test Create Product với Image URL:**
   - Vào `/staff/products?action=add`
   - Điền thông tin product
   - Không chọn file
   - Điền Image URL (ví dụ: https://via.placeholder.com/300)
   - Click "Create Product"
   - Kiểm tra: Product được tạo, ảnh từ URL hiển thị

3. **Test Ưu tiên Upload File:**
   - Vào `/staff/products?action=add`
   - Điền thông tin product
   - Chọn file ảnh
   - Điền Image URL
   - Click "Create Product"
   - Kiểm tra: Ảnh upload được sử dụng (không phải URL)

4. **Test Edit Product - Giữ ảnh cũ:**
   - Vào edit product có ảnh
   - Không chọn file mới
   - Không đổi Image URL
   - Click "Update Product"
   - Kiểm tra: Ảnh cũ vẫn còn

5. **Test Edit Product - Thay ảnh mới:**
   - Vào edit product
   - Chọn file ảnh mới
   - Click "Update Product"
   - Kiểm tra: Ảnh mới được hiển thị

6. **Test Validation:**
   - Upload file không phải ảnh (.txt, .pdf) → Báo lỗi
   - Upload file > 5MB → Báo lỗi
   - Điền Image URL không hợp lệ → Báo lỗi

7. **Test Hiển thị:**
   - Kiểm tra `/staff/products` (list) → Ảnh hiển thị link "View"
   - Kiểm tra `/catalog/product?id=X` (detail) → Ảnh hiển thị đầy đủ
   - Click vào ảnh → Mở được trong tab mới

## Lưu ý kỹ thuật

- Tên file được sanitize để tránh path traversal attack
- Extension được validate chặt chẽ
- File size giới hạn 5MB
- Thư mục upload được tạo tự động nếu chưa có
- Tương thích ngược 100% với code cũ (vẫn dùng imageUrl)
- Không thay đổi database schema
- Không refactor các module khác
