<%@ page contentType="text/html;charset=UTF-8" %>

<html>
<head>
    <title>Form sản phẩm</title>
</head>
<body>

<h2>${product == null ? "Thêm sản phẩm" : "Chỉnh sửa sản phẩm"}</h2>

<form method="post" action="${pageContext.request.contextPath}/staff/products">

    <!-- CSRF TOKEN (thêm) -->
    <input type="hidden" name="csrf_token" value="${sessionScope.CSRF_TOKEN}" />

    <input type="hidden" name="id" value="${product.id}" />

    Tên:<br>
    <input type="text" name="name" value="${product.name}" required />
    <br><br>

    Category:<br>
    <input type="text" name="category" value="${product.category}" />
    <br><br>

    Đơn vị:<br>
    <input type="text" name="unit" value="${product.unit}" />
    <br><br>

    Giá bán:<br>
    <input type="number" step="0.01" name="sellPrice" value="${product.sellPrice}" required />
    <br><br>

    URL ảnh:<br>
    <input type="text" name="imageUrl" value="${product.imageUrl}" />
    <br><br>

    Mô tả:<br>
    <textarea name="description" rows="4" cols="40">${product.description}</textarea>
    <br><br>

    <button type="submit">Lưu</button>
    <a href="${pageContext.request.contextPath}/staff/products">Quay lại</a>

</form>

</body>
</html>