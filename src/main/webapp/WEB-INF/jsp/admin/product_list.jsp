<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<html>
<head>
    <title>Quản lý sản phẩm</title>
    <style>
        body { font-family: Arial; background:#f4f6f9; padding:30px; }
        table {
            width:100%;
            border-collapse: collapse;
            background:white;
            border-radius:10px;
            overflow:hidden;
            box-shadow:0 4px 10px rgba(0,0,0,0.1);
        }
        th, td {
            padding:12px;
            text-align:center;
        }
        th {
            background:#2c3e50;
            color:white;
        }
        tr:nth-child(even) { background:#f2f2f2; }

        .btn-delete {
            background:#e74c3c;
            color:white;
            padding:6px 12px;
            border:none;
            border-radius:6px;
            cursor:pointer;
        }

        .btn-delete:hover { background:#c0392b; }

        .top-bar {
            margin-bottom:20px;
        }

        .btn-add {
            background:#27ae60;
            color:white;
            padding:8px 15px;
            border-radius:6px;
            text-decoration:none;
        }

        .btn-add:hover { background:#1e8449; }
    </style>
</head>
<body>

<h2>Quản lý sản phẩm</h2>

<div class="top-bar">
    <a class="btn-add" href="${pageContext.request.contextPath}/admin/add-product">
        ➕ Thêm sản phẩm
    </a>
</div>

<table>
    <tr>
        <th>ID</th>
        <th>Tên</th>
        <th>Danh mục</th>
        <th>Giá</th>
        <th>Hành động</th>
    </tr>

    <c:forEach var="p" items="${products}">
        <tr>
            <td>${p.id}</td>
            <td>${p.name}</td>
            <td>${p.category}</td>
            <td>${p.sellPrice}</td>
            <td>
                <form method="post"
                      action="${pageContext.request.contextPath}/admin/delete-product"
                      onsubmit="return confirm('Bạn chắc chắn muốn xóa?');">
                    <input type="hidden" name="id" value="${p.id}" />
                    <button class="btn-delete">Xóa</button>
                </form>
            </td>
        </tr>
    </c:forEach>
</table>

</body>
</html>