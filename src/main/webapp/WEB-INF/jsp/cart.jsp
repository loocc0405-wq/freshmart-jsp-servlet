<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>

<c:set var="pageTitle" value="Your Cart"/>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"/>

<h3 class="mb-3">🛒 Your Cart</h3>

<!-- ===== ADDED: GUEST CART NOTICE ===== -->
<c:if test="${empty sessionScope.authUser}">
    <div class="alert alert-info">
        You are shopping as <b>Guest</b>.
        Your cart will be saved after you
        <a href="${pageContext.request.contextPath}/login">login</a>.
    </div>
</c:if>
<!-- ===== END ADDED ===== -->

<!-- ===== ADDED: SHOW EMPTY CART ===== -->
<c:if test="${empty items}">
    <div class="alert alert-warning">
        Your cart is empty.
    </div>
</c:if>
<!-- ===== END ADDED ===== -->

<!-- ===== ADDED: SHOW ERROR MESSAGE ===== -->
<c:if test="${not empty error}">
    <div class="alert alert-danger">
        ${error}
    </div>
</c:if>
<!-- ===== END ADDED ===== -->

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

    <!-- ===== ADDED: CART TOTAL CALCULATION ===== -->
    <c:set var="cartTotal" value="0"/>
    <!-- ===== END ADDED ===== -->

    <c:forEach var="item" items="${items}">
        <tr>
            <td>${item.product.name}</td>

            <td>${item.product.sellPrice}</td>

            <td>
                <form action="${pageContext.request.contextPath}/cart"
                      method="post"
                      class="d-flex gap-2">

                    <input type="hidden"
                           name="csrf_token"
                           value="${sessionScope.CSRF_TOKEN}" />

                    <input type="hidden" name="action" value="update"/>

                    <input type="hidden"
                           name="productId"
                           value="${item.product.id}"/>

                    <input type="number"
                           name="qty"
                           min="1"
                           value="${item.quantity}"
                           class="form-control form-control-sm"/>

                    <button class="btn btn-primary btn-sm">
                        Update
                    </button>

                </form>
            </td>

            <td>
                <c:choose>

                    <c:when test="${not empty item.lineTotal}">
                        ${item.lineTotal}

                        <c:set var="cartTotal"
                               value="${cartTotal + item.lineTotal}"/>

                    </c:when>

                    <c:otherwise>

                        <c:set var="line"
                               value="${item.product.sellPrice * item.quantity}"/>

                        ${line}

                        <c:set var="cartTotal"
                               value="${cartTotal + line}"/>

                    </c:otherwise>

                </c:choose>
            </td>

            <td>

                <form action="${pageContext.request.contextPath}/cart"
                      method="post">

                    <input type="hidden"
                           name="csrf_token"
                           value="${sessionScope.CSRF_TOKEN}" />

                    <input type="hidden" name="action" value="remove"/>

                    <input type="hidden"
                           name="productId"
                           value="${item.product.id}"/>

                    <button class="btn btn-danger btn-sm">
                        X
                    </button>

                </form>

            </td>

        </tr>
    </c:forEach>

    </tbody>
</table>

<div class="card mt-3 shadow-sm">
    <div class="card-body d-flex justify-content-between align-items-center">

        <h5 class="mb-0">
            Cart Total:
        </h5>

        <h4 class="text-success mb-0">
            ${cartTotal}
        </h4>

    </div>
</div>

<div class="mt-3">

    <a href="${pageContext.request.contextPath}/catalog"
       class="btn btn-secondary">
        ← Continue Shopping
    </a>

    <c:choose>

        <%-- ===== SỬA CHỖ NÀY ===== --%>
        <c:when test="${empty sessionScope.authUser}">

            <a href="${pageContext.request.contextPath}/login"
               class="btn btn-warning">
                Login to Checkout
            </a>

        </c:when>
        <%-- ===================== --%>

        <c:otherwise>

            <form action="${pageContext.request.contextPath}/customer/checkout"
                  method="post"
                  class="d-inline">

                <input type="hidden"
                       name="csrf_token"
                       value="${sessionScope.CSRF_TOKEN}" />

                <button type="submit"
                        class="btn btn-success">
                    Checkout
                </button>

            </form>

        </c:otherwise>

    </c:choose>

</div>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>