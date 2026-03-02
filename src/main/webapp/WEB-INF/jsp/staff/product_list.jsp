<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html>
<head>
    <title>Quản lý sản phẩm</title>
</head>
<body>

<h2>Danh sách sản phẩm</h2>

<!-- FORM TÌM KIẾM -->
<form method="get" action="${pageContext.request.contextPath}/staff/products">
    Tên:
    <input type="text" name="keyword" value="${param.keyword}" />

    Category:
    <input type="text" name="category" value="${param.category}" />

    <button type="submit">Tìm kiếm</button>
</form>

<br>

<a href="${pageContext.request.contextPath}/staff/products?action=add">
    ➕ Thêm sản phẩm
</a>

<br><br>

<table border="1" cellpadding="8">
    <tr>
        <th>ID</th>
        <th>Tên</th>
        <th>Category</th>
        <th>Đơn vị</th>
        <th>Giá</th>
        <th>Ảnh</th>
        <th>Mô tả</th>
        <th>Hành động</th>
    </tr>

    <c:forEach var="p" items="${products}">
        <tr>
            <td>${p.id}</td>
            <td>${p.name}</td>
            <td>${p.category}</td>
            <td>${p.unit}</td>
            <td>${p.sellPrice}</td>
            <td>
                <c:if test="${not empty p.imageUrl}">
                    <img src="${p.imageUrl}" width="80"/>
                </c:if>
            </td>
            <td>${p.description}</td>
            <td>
                <a href="${pageContext.request.contextPath}/staff/products?action=edit&id=${p.id}">
                    ✏️ Sửa
                </a>
                |
                <a href="${pageContext.request.contextPath}/staff/products?action=delete&id=${p.id}"
                   onclick="return confirm('Bạn có chắc muốn xóa?')">
                    🗑 Xóa
                </a>
            </td>
        </tr>
    </c:forEach>
</table>

</body>
</html>