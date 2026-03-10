<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<html>
<head>
    <title>Quản lý sản phẩm</title>
    <style>
        body { font-family: Arial; background:#f4f6f9; padding:30px; }
        
        .search-box {
            background: white;
            padding: 20px;
            border-radius: 10px;
            margin-bottom: 20px;
            box-shadow: 0 4px 10px rgba(0,0,0,0.1);
        }
        
        .search-box input[type="text"] {
            padding: 8px;
            border: 1px solid #ddd;
            border-radius: 4px;
            width: 200px;
            margin-right: 10px;
        }
        
        .search-box button {
            background: #3498db;
            color: white;
            padding: 8px 15px;
            border: none;
            border-radius: 4px;
            cursor: pointer;
        }
        
        .search-box button:hover { background: #2980b9; }
        
        .pagination-info {
            margin-bottom: 10px;
            color: #666;
        }
        
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
        
        .pagination {
            display: flex;
            justify-content: center;
            list-style: none;
            padding: 20px 0;
            gap: 5px;
        }
        
        .pagination a, .pagination span {
            padding: 8px 12px;
            border: 1px solid #ddd;
            border-radius: 4px;
            text-decoration: none;
            color: #333;
        }
        
        .pagination a:hover {
            background: #3498db;
            color: white;
        }
        
        .pagination .active {
            background: #3498db;
            color: white;
            border-color: #3498db;
        }
        
        .pagination .disabled {
            color: #ccc;
            pointer-events: none;
        }
    </style>
</head>
<body>

<h2>Quản lý sản phẩm</h2>

<div class="top-bar">
    <a class="btn-add" href="${pageContext.request.contextPath}/admin/add-product">
        ➕ Thêm sản phẩm
    </a>
</div>

<!-- Search box -->
<div class="search-box">
    <form method="get" action="${pageContext.request.contextPath}/admin/products">
        <input type="text" name="keyword" value="${keyword}" placeholder="Tìm theo tên...">
        <input type="text" name="category" value="${category}" placeholder="Danh mục...">
        <label>
            <input type="checkbox" name="showInactive" ${showInactive ? 'checked' : ''}>
            Hiện cả inactive
        </label>
        <button type="submit">Tìm kiếm</button>
    </form>
</div>

<!-- Pagination info -->
<c:if test="${totalItems > 0}">
    <div class="pagination-info">
        Hiển thị ${(currentPage - 1) * pageSize + 1} đến ${currentPage * pageSize > totalItems ? totalItems : currentPage * pageSize} trong tổng số ${totalItems} sản phẩm
    </div>
</c:if>

<table>
    <tr>
        <th>ID</th>
        <th>Tên</th>
        <th>Danh mục</th>
        <th>Giá</th>
        <th>Trạng thái</th>
        <th>Hành động</th>
    </tr>

    <c:forEach var="p" items="${products}">
        <tr>
            <td>${p.id}</td>
            <td>${p.name}</td>
            <td>${p.category}</td>
            <td>${p.sellPrice}</td>
            <td>
                <c:choose>
                    <c:when test="${p.active}">
                        <span style="color: green;">Active</span>
                    </c:when>
                    <c:otherwise>
                        <span style="color: gray;">Inactive</span>
                    </c:otherwise>
                </c:choose>
            </td>
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
    
    <c:if test="${empty products}">
        <tr>
            <td colspan="6" style="text-align: center; color: #999; padding: 20px;">
                Không tìm thấy sản phẩm nào.
            </td>
        </tr>
    </c:if>
</table>

<!-- Pagination controls -->
<c:if test="${totalPages > 1}">
    <ul class="pagination">
        <!-- Previous button -->
        <li>
            <c:choose>
                <c:when test="${currentPage == 1}">
                    <span class="disabled">« Trước</span>
                </c:when>
                <c:otherwise>
                    <a href="${pageContext.request.contextPath}/admin/products?page=${currentPage - 1}&keyword=${keyword}&category=${category}&showInactive=${showInactive}">« Trước</a>
                </c:otherwise>
            </c:choose>
        </li>

        <!-- Page numbers -->
        <c:choose>
            <c:when test="${totalPages <= 7}">
                <c:forEach var="i" begin="1" end="${totalPages}">
                    <li>
                        <c:choose>
                            <c:when test="${i == currentPage}">
                                <span class="active">${i}</span>
                            </c:when>
                            <c:otherwise>
                                <a href="${pageContext.request.contextPath}/admin/products?page=${i}&keyword=${keyword}&category=${category}&showInactive=${showInactive}">${i}</a>
                            </c:otherwise>
                        </c:choose>
                    </li>
                </c:forEach>
            </c:when>
            <c:otherwise>
                <!-- First page -->
                <li>
                    <c:choose>
                        <c:when test="${1 == currentPage}">
                            <span class="active">1</span>
                        </c:when>
                        <c:otherwise>
                            <a href="${pageContext.request.contextPath}/admin/products?page=1&keyword=${keyword}&category=${category}&showInactive=${showInactive}">1</a>
                        </c:otherwise>
                    </c:choose>
                </li>

                <c:if test="${currentPage > 3}">
                    <li><span>...</span></li>
                </c:if>

                <!-- Pages around current -->
                <c:forEach var="i" begin="${currentPage - 1}" end="${currentPage + 1}">
                    <c:if test="${i > 1 && i < totalPages}">
                        <li>
                            <c:choose>
                                <c:when test="${i == currentPage}">
                                    <span class="active">${i}</span>
                                </c:when>
                                <c:otherwise>
                                    <a href="${pageContext.request.contextPath}/admin/products?page=${i}&keyword=${keyword}&category=${category}&showInactive=${showInactive}">${i}</a>
                                </c:otherwise>
                            </c:choose>
                        </li>
                    </c:if>
                </c:forEach>

                <c:if test="${currentPage < totalPages - 2}">
                    <li><span>...</span></li>
                </c:if>

                <!-- Last page -->
                <li>
                    <c:choose>
                        <c:when test="${totalPages == currentPage}">
                            <span class="active">${totalPages}</span>
                        </c:when>
                        <c:otherwise>
                            <a href="${pageContext.request.contextPath}/admin/products?page=${totalPages}&keyword=${keyword}&category=${category}&showInactive=${showInactive}">${totalPages}</a>
                        </c:otherwise>
                    </c:choose>
                </li>
            </c:otherwise>
        </c:choose>

        <!-- Next button -->
        <li>
            <c:choose>
                <c:when test="${currentPage == totalPages}">
                    <span class="disabled">Sau »</span>
                </c:when>
                <c:otherwise>
                    <a href="${pageContext.request.contextPath}/admin/products?page=${currentPage + 1}&keyword=${keyword}&category=${category}&showInactive=${showInactive}">Sau »</a>
                </c:otherwise>
            </c:choose>
        </li>
    </ul>
</c:if>

</body>
</html>