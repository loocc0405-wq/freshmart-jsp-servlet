<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>My Orders - FreshMart</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 0; background: #f6f8fa; }
        .container { max-width: 1100px; margin: 30px auto; background: #fff; padding: 24px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,.08); }
        h1 { margin-top: 0; }
        .msg-error { color: #c62828; margin-bottom: 12px; }
        form.filter-form { margin-bottom: 18px; display: flex; flex-wrap: wrap; gap: 12px; align-items: end; }
        .field { display: flex; flex-direction: column; }
        label { font-weight: bold; margin-bottom: 6px; }
        select, input, button { padding: 8px 12px; border-radius: 6px; border: 1px solid #ccc; }
        button { background: #2e7d32; color: #fff; border: none; cursor: pointer; }
        button:hover { background: #256628; }
        table { width: 100%; border-collapse: collapse; }
        th, td { padding: 12px; border-bottom: 1px solid #e5e5e5; text-align: left; }
        th { background: #f2f4f7; }
        a.btn { text-decoration: none; background: #1565c0; color: #fff; padding: 6px 10px; border-radius: 6px; display: inline-block; }
        .summary { margin: 12px 0 18px; color: #555; }
        .pagination { margin-top: 18px; display: flex; gap: 8px; align-items: center; flex-wrap: wrap; }
        .pagination a, .pagination span {
            text-decoration: none;
            padding: 8px 12px;
            border-radius: 6px;
            border: 1px solid #ccc;
            background: #fff;
            color: #333;
        }
        .pagination .current {
            background: #2e7d32;
            color: #fff;
            border-color: #2e7d32;
        }
        .empty { text-align: center; color: #666; }
    </style>
</head>
<body>
<jsp:include page="/WEB-INF/jsp/common/header.jsp" />

<div class="container">
    <h1>My Orders</h1>

    <c:if test="${not empty errorMessage}">
        <div class="msg-error">${errorMessage}</div>
    </c:if>

    <form class="filter-form" method="get" action="${pageContext.request.contextPath}/customer/orders">
        <div class="field">
            <label for="status">Status</label>
            <select name="status" id="status">
                <option value="">All</option>
                <option value="PENDING" ${selectedStatus == 'PENDING' ? 'selected' : ''}>PENDING</option>
                <option value="COMPLETED" ${selectedStatus == 'COMPLETED' ? 'selected' : ''}>COMPLETED</option>
                <option value="CANCELLED" ${selectedStatus == 'CANCELLED' ? 'selected' : ''}>CANCELLED</option>
            </select>
        </div>

        <div class="field">
            <label for="fromDate">From date</label>
            <input type="date" id="fromDate" name="fromDate" value="${fromDate}" />
        </div>

        <div class="field">
            <label for="toDate">To date</label>
            <input type="date" id="toDate" name="toDate" value="${toDate}" />
        </div>

        <div class="field">
            <button type="submit">Filter</button>
        </div>
    </form>

    <div class="summary">
        Total orders: <strong>${totalItems}</strong>
    </div>

    <table>
        <thead>
        <tr>
            <th>Order Code</th>
            <th>Status</th>
            <th>Type</th>
            <th>Payment</th>
            <th>Total</th>
            <th>Created At</th>
            <th>Action</th>
        </tr>
        </thead>
        <tbody>
        <c:choose>
            <c:when test="${not empty orders}">
                <c:forEach var="o" items="${orders}">
                    <tr>
                        <td>${o.orderCode}</td>
                        <td>${o.status}</td>
                        <td>${o.type}</td>
                        <td>${o.paymentMethod}</td>
                        <td><fmt:formatNumber value="${o.totalAmount}" type="number" minFractionDigits="0" maxFractionDigits="2"/></td>
                        <td>${o.createdAt}</td>
                        <td>
                            <a class="btn" href="${pageContext.request.contextPath}/customer/order-detail?id=${o.id}">View Detail</a>
                        </td>
                    </tr>
                </c:forEach>
            </c:when>
            <c:otherwise>
                <tr>
                    <td class="empty" colspan="7">No orders found.</td>
                </tr>
            </c:otherwise>
        </c:choose>
        </tbody>
    </table>

    <c:if test="${totalPages > 1}">
        <div class="pagination">
            <c:if test="${currentPage > 0}">
                <a href="${pageContext.request.contextPath}/customer/orders?status=${selectedStatus}&fromDate=${fromDate}&toDate=${toDate}&page=${currentPage - 1}">
                    Previous
                </a>
            </c:if>

            <c:forEach var="i" begin="0" end="${totalPages - 1}">
                <c:choose>
                    <c:when test="${i == currentPage}">
                        <span class="current">${i + 1}</span>
                    </c:when>
                    <c:otherwise>
                        <a href="${pageContext.request.contextPath}/customer/orders?status=${selectedStatus}&fromDate=${fromDate}&toDate=${toDate}&page=${i}">
                            ${i + 1}
                        </a>
                    </c:otherwise>
                </c:choose>
            </c:forEach>

            <c:if test="${currentPage + 1 < totalPages}">
                <a href="${pageContext.request.contextPath}/customer/orders?status=${selectedStatus}&fromDate=${fromDate}&toDate=${toDate}&page=${currentPage + 1}">
                    Next
                </a>
            </c:if>
        </div>
    </c:if>
</div>
</body>
</html>