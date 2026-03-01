<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <title>Thêm sản phẩm</title>
</head>
<body>

<h2>Thêm sản phẩm mới</h2>

<c:if test="${error != null}">
    <p style="color:red">${error}</p>
</c:if>

<form method="post">

    Tên sản phẩm:<br/>
    <input type="text" name="name" required/><br/><br/>

    Danh mục:<br/>
    <input type="text" name="category"/><br/><br/>

    Đơn vị:<br/>
    <input type="text" name="unit"/><br/><br/>

    Giá bán:<br/>
    <input type="number" step="0.01" name="sellPrice" required/><br/><br/>

    URL hình ảnh:<br/>
    <input type="text" name="imageUrl"/><br/><br/>

    Mô tả:<br/>
    <textarea name="description"></textarea><br/><br/>

    <button type="submit">Thêm</button>

</form>

<br/>
<a href="${pageContext.request.contextPath}/admin/home">
    Quay lại
</a>

</body>
</html>