<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Order Detail - FreshMart</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 0; background: #f6f8fa; }
        .container { max-width: 1100px; margin: 30px auto; background: #fff; padding: 24px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,.08); }
        h1 { margin-top: 0; }
        .msg-error { color: #c62828; margin-bottom: 12px; }
        .info { margin-bottom: 20px; line-height: 1.8; }
        table { width: 100%; border-collapse: collapse; }
        th, td { padding: 12px; border-bottom: 1px solid #e5e5e5; text-align: left; }
        th { background: #f2f4f7; }
        a.back { display: inline-block; margin-top: 18px; text-decoration: none; background: #666; color: #fff; padding: 8px 12px; border-radius: 6px; }
    </style>
</head>
<body>
<jsp:include page="/WEB-INF/jsp/common/header.jsp" />

<div class="container">
    <h1>Order Detail</h1>

    <c:if test="${not empty errorMessage}">
        <div class="msg-error">${errorMessage}</div>
    </c:if>

    <c:if test="${not empty order}">
        <div class="info">
            <div><strong>Order Code:</strong> ${order.orderCode}</div>
            <div><strong>Status:</strong> ${order.status}</div>
            <div><strong>Type:</strong> ${order.type}</div>
            <div><strong>Payment Method:</strong> ${order.paymentMethod}</div>
            <div><strong>Created At:</strong> ${order.createdAt}</div>
            <div><strong>Total Amount:</strong> <fmt:formatNumber value="${order.totalAmount}" type="number" minFractionDigits="0" maxFractionDigits="2"/></div>
        </div>

        <h3>Items</h3>
        <table>
            <thead>
            <tr>
                <th>Product</th>
                <th>Quantity</th>
                <th>Unit Price</th>
                <th>Line Total</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="item" items="${order.items}">
                <tr>
                    <td>${item.product.name}</td>
                    <td>${item.quantity}</td>
                    <td><fmt:formatNumber value="${item.unitPrice}" type="number" minFractionDigits="0" maxFractionDigits="2"/></td>
                    <td><fmt:formatNumber value="${item.lineTotal}" type="number" minFractionDigits="0" maxFractionDigits="2"/></td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
    </c:if>

    <a class="back" href="${pageContext.request.contextPath}/customer/orders">Back to Orders</a>
</div>
</body>
</html>