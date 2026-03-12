<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<html>
<head>

<title>Order Success</title>

<style>

.container{
width:700px;
margin:auto;
font-family:Arial;
}

.card{
border:1px solid #ddd;
padding:20px;
border-radius:8px;
background:#fff;
}

.success{
color:#2ecc71;
font-size:22px;
font-weight:bold;
}

.table{
width:100%;
border-collapse:collapse;
margin-top:20px;
}

.table th,.table td{
padding:10px;
border-bottom:1px solid #eee;
}

.total{
text-align:right;
font-size:18px;
font-weight:bold;
}

.buttons{
margin-top:20px;
}

.btn{
padding:10px 16px;
border-radius:6px;
text-decoration:none;
margin-right:10px;
}

.btn-primary{
background:#27ae60;
color:white;
}

.btn-outline{
border:1px solid #ccc;
color:#333;
}

</style>

</head>

<body>

<div class="container">

<div class="card">

<div class="success">
✔ Order placed successfully
</div>

<!-- ===================== -->
<!--   NULL CHECK ADDED    -->
<!-- ===================== -->

<c:if test="${not empty order}">

<p>Order Code: <b>${order.orderCode}</b></p>
<p>Status: <b>${order.status}</b></p>
<p>Payment: <b>${order.paymentMethod}</b></p>

<table class="table">

<tr>
<th>Product</th>
<th>Qty</th>
<th>Price</th>
<th>Total</th>
</tr>

<c:forEach items="${order.items}" var="item">

<tr>
<td>${item.product.name}</td>
<td>${item.quantity}</td>
<td>$${item.unitPrice}</td>
<td>$${item.lineTotal}</td>
</tr>

</c:forEach>

</table>

<div class="total">
Total: $${order.totalAmount}
</div>

</c:if>

<!-- ===================== -->
<!-- END NULL CHECK ADDED  -->
<!-- ===================== -->

<div class="buttons">

<a class="btn btn-primary"
href="${pageContext.request.contextPath}/catalog">
Continue Shopping
</a>

<a class="btn btn-outline"
href="${pageContext.request.contextPath}/customer/orders">
View My Orders
</a>

</div>

</div>

</div>

</body>
</html>