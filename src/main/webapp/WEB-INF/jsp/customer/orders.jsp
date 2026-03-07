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
        form { margin-bottom: 18px; }
        select, button { padding: 8px 12px; border-radius: 6px; border: 1px solid #ccc; }
        button { background: #2e7d32; color: #fff; border: none; cursor: pointer; }
        table { width: 100%; border-collapse: collapse; }
        th, td { padding: 12px; border-bottom: 1px solid #e5e5e5; text-align: left; }
        th { background: #f2f4f7; }
        a.btn { text-decoration: none; background: #1565c0; color: #fff; padding: 6px 10px; border-radius: 6px; }
    </style>
</head>
<body>
<jsp:include page="/WEB-INF/jsp/common/header.jsp" />

<div class="container">
    <h1>My Orders</h1>

    <c:if test="${not empty errorMessage}">
        <div class="msg-error">${errorMessage}</div>
    </c:if>

    <form method="get" action="${pageContext.request.contextPath}/customer/orders">
        <label for="status">Filter by status:</label>
        <select name="status" id="status">
            <option value="">All</option>
            <option value="PENDING" ${selectedStatus == 'PENDING' ? 'selected' : ''}>PENDING</option>
            <option value="COMPLETED" ${selectedStatus == 'COMPLETED' ? 'selected' : ''}>COMPLETED</option>
            <option value="CANCELLED" ${selectedStatus == 'CANCELLED' ? 'selected' : ''}>CANCELLED</option>
        </select>
        <button type="submit">Filter</button>
    </form>

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
                    <td colspan="7">No orders found.</td>
                </tr>
            </c:otherwise>
        </c:choose>
        </tbody>
    </table>
</div>
</body>
</html>