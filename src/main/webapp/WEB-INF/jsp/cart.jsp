<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>

<c:set var="pageTitle" value="Your Cart"/>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"/>

<h3 class="mb-3">🛒 Your Cart</h3>

<table class="table table-bordered bg-white">
    <thead class="table-dark">
    <tr>
        <th>Product</th>
        <th>Price</th>
        <th width="150">Quantity</th>
        <th>Total</th>
        <th width="100">Action</th>
    </tr>
    </thead>

    <tbody>
    <c:forEach var="item" items="${items}">
        <tr>
            <td>${item.product.name}</td>
            <td>${item.product.sellPrice}</td>

            <td>
                <form action="${pageContext.request.contextPath}/cart" method="post" class="d-flex gap-2">

                    <!-- CSRF TOKEN -->
                    <input type="hidden" name="csrf_token" value="${sessionScope.CSRF_TOKEN}" />

                    <input type="hidden" name="action" value="update"/>
                    <input type="hidden" name="productId" value="${item.product.id}"/>
                    <input type="number" name="qty" min="1" value="${item.quantity}" class="form-control form-control-sm"/>
                    <button class="btn btn-primary btn-sm">Update</button>
                </form>
            </td>

            <td>${item.lineTotal}</td>

            <td>
                <form action="${pageContext.request.contextPath}/cart" method="post">

                    <!-- CSRF TOKEN -->
                    <input type="hidden" name="csrf_token" value="${sessionScope.CSRF_TOKEN}" />

                    <input type="hidden" name="action" value="remove"/>
                    <input type="hidden" name="productId" value="${item.product.id}"/>
                    <button class="btn btn-danger btn-sm">X</button>
                </form>
            </td>
        </tr>
    </c:forEach>
    </tbody>
</table>

<div class="mt-3">

    <a href="${pageContext.request.contextPath}/catalog" class="btn btn-secondary">
        ← Continue Shopping
    </a>

    <form action="${pageContext.request.contextPath}/customer/checkout"
          method="post"
          class="d-inline">

        <input type="hidden" name="csrf_token" value="${sessionScope.CSRF_TOKEN}" />

        <button type="submit" class="btn btn-success">
            Checkout
        </button>

    </form>

</div>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>