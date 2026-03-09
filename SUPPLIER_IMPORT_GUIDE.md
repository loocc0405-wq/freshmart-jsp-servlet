# Hướng dẫn Import Supplier

## Tổng quan
Tính năng import supplier từ file CSV UTF-8 đã được implement end-to-end theo kiến trúc JSP/Servlet hiện tại.

## Files đã sửa/thêm

### 1. **src/main/webapp/WEB-INF/jsp/staff/supplier_list.jsp**
- Thêm nút "Import CSV" bên cạnh nút "Add Supplier"
- Thêm modal Bootstrap để upload file CSV
- Hiển thị hướng dẫn format CSV và các lưu ý
- Sửa hiển thị error message để hỗ trợ HTML (hiển thị chi tiết lỗi từng dòng)

### 2. **src/main/java/com/freshmart/web/servlet/staff/SupplierManagementServlet.java**
- Thêm annotation `@MultipartConfig` để hỗ trợ upload file
- Thêm `SupplierImportService` vào servlet
- Thêm xử lý action "import" trong `doPost()`
- Thêm method `handleImport()` để xử lý upload và gọi service
- Hiển thị kết quả import: tổng số dòng, số thành công, số lỗi, chi tiết lỗi

### 3. **src/main/java/com/freshmart/service/SupplierImportService.java** (MỚI)
- Service chuyên xử lý import CSV
- Class `ImportResult` để lưu kết quả import
- Method `importFromCsv()`: đọc file, parse từng dòng, validate, save
- Method `parseCsvLine()`: parse và validate từng dòng CSV
- Method `parseCsvFields()`: parse CSV có hỗ trợ quoted fields
- Logic: nếu email đã tồn tại → update, nếu chưa → insert
- Validate đầy đủ: name, email format, phone format, leadTimeDays > 0

### 4. **src/main/java/com/freshmart/repository/SupplierRepository.java**
- Thêm method `findByEmail()` để tìm supplier theo email
- Dùng để check duplicate khi import

### 5. **sample_suppliers.csv** (MỚI)
- File CSV mẫu để test
- Chứa 5 suppliers với đầy đủ thông tin

### 6. **SUPPLIER_IMPORT_GUIDE.md** (MỚI)
- Tài liệu hướng dẫn này

## Format CSV

```csv
name,email,phone,address,certificate,leadTimeDays,note
"ABC Fresh Produce",abc@freshproduce.com,0123456789,"123 Market Street",ISO9001,3,Reliable supplier
```

### Các cột (theo thứ tự):
1. **name** (bắt buộc): Tên nhà cung cấp
2. **email** (bắt buộc): Email (dùng để check duplicate)
3. **phone** (bắt buộc): Số điện thoại (9-15 chữ số)
4. **address** (tùy chọn): Địa chỉ
5. **certificate** (tùy chọn): Chứng nhận (ISO9001, VietGAP, HACCP, v.v.)
6. **leadTimeDays** (tùy chọn): Thời gian giao hàng (số nguyên dương, mặc định: 1)
7. **note** (tùy chọn): Ghi chú

### Lưu ý:
- Dòng đầu tiên là header (sẽ bị bỏ qua)
- File phải encode UTF-8
- Có thể dùng dấu ngoặc kép `"` để bao các field có dấu phẩy
- Email dùng để check duplicate: nếu email đã tồn tại → update, chưa có → insert mới

## Validation Rules

### Name:
- Bắt buộc, không được rỗng

### Email:
- Bắt buộc
- Format: `user@domain.com`
- Regex: `^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$`

### Phone:
- Bắt buộc
- Chỉ chứa: số, dấu cách, +, -, (, )
- Phải có từ 9-15 chữ số (sau khi loại bỏ ký tự đặc biệt)

### Lead Time Days:
- Phải là số nguyên dương
- Mặc định: 1 nếu không nhập

## Cách test

### Bước 1: Compile project
```bash
mvn clean compile
```

### Bước 2: Deploy lên Tomcat
```bash
mvn package
# Copy target/freshmart.war vào Tomcat webapps/
```

### Bước 3: Truy cập trang Suppliers
```
http://localhost:8080/freshmart/staff/suppliers
```

### Bước 4: Test import
1. Click nút "Import CSV" (màu xanh lá)
2. Modal hiện ra với form upload
3. Chọn file `sample_suppliers.csv`
4. Click "Upload & Import"
5. Trang reload và hiển thị kết quả:
   - Nếu thành công: "Import completed: 5 success, 0 errors (Total: 5 rows)"
   - Nếu có lỗi: Hiển thị chi tiết lỗi từng dòng

### Bước 5: Test các trường hợp lỗi

#### Test 1: File không phải CSV
- Upload file .txt hoặc .xlsx
- Kết quả: "Only CSV files are supported."

#### Test 2: Email không hợp lệ
Tạo file `test_invalid_email.csv`:
```csv
name,email,phone,address,certificate,leadTimeDays,note
"Bad Email Supplier",invalid-email,0123456789,"123 Street",,1,Test
```
- Kết quả: "Line 2: Invalid email format: invalid-email"

#### Test 3: Phone không hợp lệ
Tạo file `test_invalid_phone.csv`:
```csv
name,email,phone,address,certificate,leadTimeDays,note
"Bad Phone Supplier",test@test.com,123,"123 Street",,1,Test
```
- Kết quả: "Line 2: Phone must have between 9 and 15 digits"

#### Test 4: Lead time âm
Tạo file `test_negative_leadtime.csv`:
```csv
name,email,phone,address,certificate,leadTimeDays,note
"Bad Lead Supplier",test2@test.com,0123456789,"123 Street",,-5,Test
```
- Kết quả: "Line 2: Lead time must be positive"

#### Test 5: Update supplier đã tồn tại
- Import `sample_suppliers.csv` lần 1 → 5 suppliers mới
- Sửa file, thay đổi phone/address của "ABC Fresh Produce"
- Import lại → supplier này được update (không tạo duplicate)

### Bước 6: Kiểm tra các chức năng cũ vẫn hoạt động
- ✅ List suppliers
- ✅ Search (keyword, certificate, date range)
- ✅ Pagination
- ✅ Statistics cards
- ✅ Charts (Certificate Status, Top Suppliers)
- ✅ Create supplier
- ✅ Edit supplier
- ✅ Delete supplier

## Kết quả Import

### Thành công:
```
Import completed: 5 success, 0 errors (Total: 5 rows)
```

### Có lỗi:
```
Import completed: 3 success, 2 errors (Total: 5 rows)

Errors:
• Line 2: Invalid email format: abc@
• Line 4: Phone must have between 9 and 15 digits
```

## Technical Details

### CSV Parser
- Tự implement parser đơn giản (không dùng thư viện ngoài)
- Hỗ trợ quoted fields: `"value with, comma"`
- Encoding: UTF-8
- Line separator: `\n` hoặc `\r\n`

### Transaction Handling
- Mỗi dòng CSV được xử lý trong 1 transaction riêng
- Nếu 1 dòng lỗi → skip, tiếp tục dòng tiếp theo
- Không rollback toàn bộ import khi có lỗi

### Duplicate Detection
- Dùng email làm unique key
- Query: `SELECT s FROM Supplier s WHERE s.email = :email`
- Nếu tìm thấy → update (giữ nguyên ID và createdAt)
- Nếu không → insert mới

### File Size Limits
- `fileSizeThreshold`: 1 MB
- `maxFileSize`: 5 MB
- `maxRequestSize`: 10 MB

## Coding Style
- Giữ nguyên style hiện tại: camelCase, indent 4 spaces
- Package structure: service, repository, entity, servlet
- Không refactor code cũ
- Tách logic import ra service riêng (không nhét vào servlet)
- Validation giống với form create/edit

## Dependencies
- Không thêm dependency mới
- Dùng Java built-in: BufferedReader, InputStreamReader
- Dùng Jakarta Servlet API có sẵn: Part, MultipartConfig
