<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Customer Dashboard - FreshMart</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 0; background: #f6f8fa; }
        .container { max-width: 1100px; margin: 30px auto; }
        h1 { margin-bottom: 20px; }
        .msg-error { color: #c62828; margin-bottom: 12px; }
        .cards { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 24px; }
        .card { background: #fff; padding: 18px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,.08); }
        .card h3 { margin: 0 0 10px; font-size: 16px; color: #555; }
        .card .value { font-size: 28px; font-weight: bold; color: #222; }
        .panel { background: #fff; padding: 20px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,.08); }
        table { width: 100%; border-collapse: collapse; margin-top: 12px; }
        th, td { padding: 12px; border-bottom: 1px solid #e5e5e5; text-align: left; }
        th { background: #f2f4f7; }
        a.btn { text-decoration: none; background: #1565c0; color: #fff; padding: 6px 10px; border-radius: 6px; }
    </style>
</head>
<body>
<jsp:include page="/WEB-INF/jsp/common/header.jsp" />

<div class="container">
    <h1>Customer Dashboard</h1>

    <c:if test="${not empty errorMessage}">
        <div class="msg-error">${errorMessage}</div>
    </c:if>

    <c:if test="${not empty summary}">
        <div class="cards">
            <div class="card">
                <h3>Total Orders</h3>
                <div class="value">${summary.totalOrders}</div>
            </div>
            <div class="card">
                <h3>Pending Orders</h3>
                <div class="value">${summary.pendingOrders}</div>
            </div>
            <div class="card">
                <h3>Completed Orders</h3>
                <div class="value">${summary.completedOrders}</div>
            </div>
            <div class="card">
                <h3>Total Spent</h3>
                <div class="value">
                    <fmt:formatNumber value="${summary.totalSpent}" type="number" minFractionDigits="0" maxFractionDigits="2"/>
                </div>
            </div>
        </div>
    </c:if>

    <div class="panel">
        <h2>Recent Orders</h2>
        <table>
            <thead>
            <tr>
                <th>Order Code</th>
                <th>Status</th>
                <th>Total</th>
                <th>Created At</th>
                <th>Action</th>
            </tr>
            </thead>
            <tbody>
            <c:choose>
                <c:when test="${not empty recentOrders}">
                    <c:forEach var="o" items="${recentOrders}">
                        <tr>
                            <td>${o.orderCode}</td>
                            <td>${o.status}</td>
                            <td><fmt:formatNumber value="${o.totalAmount}" type="number" minFractionDigits="0" maxFractionDigits="2"/></td>
                            <td>${o.createdAt}</td>
                            <td>
                                <a class="btn" href="${pageContext.request.contextPath}/customer/order-detail?id=${o.id}">View</a>
                            </td>
                        </tr>
                    </c:forEach>
                </c:when>
                <c:otherwise>
                    <tr>
                        <td colspan="5">No recent orders.</td>
                    </tr>
                </c:otherwise>
            </c:choose>
            </tbody>
        </table>
    </div>
</div>
</body>
</html>