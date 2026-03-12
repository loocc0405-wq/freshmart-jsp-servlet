<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>

<c:set var="pageTitle" value="Your Cart"/>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"/>

<h3 class="mb-3">🛒 Your Cart</h3>

<!-- ===== ADDED: GUEST CART NOTICE ===== -->
<c:if test="${empty sessionScope.authUser}">
    <div class="alert alert-info border-0 shadow-sm">
        You are shopping as <b>Guest</b>.
        Your cart will be saved after you
        <a href="${pageContext.request.contextPath}/login">login</a>.
    </div>
</c:if>
<!-- ===== END ADDED ===== -->

<!-- ===== ADDED: SHOW EMPTY CART ===== -->
<c:if test="${empty items}">
    <div class="alert alert-warning border-0 shadow-sm">
        Your cart is empty.
    </div>
</c:if>
<!-- ===== END ADDED ===== -->

<!-- ===== ADDED: SHOW ERROR MESSAGE ===== -->
<c:if test="${not empty error}">
    <div class="alert alert-danger border-0 shadow-sm">
        ${error}
    </div>
</c:if>
<!-- ===== END ADDED ===== -->

<c:if test="${not empty items}">

    <c:set var="cartTotal" value="${0}"/>
    <c:set var="itemCount" value="${0}"/>

    <div class="row g-4">

        <!-- LEFT: CART TABLE -->
        <div class="col-lg-8">
            <div class="card border-0 shadow-sm">
                <div class="card-body p-0">

                    <div class="table-responsive">

                        <table class="table table-bordered align-middle bg-white mb-0">

                            <thead class="table-dark">
                            <tr>
                                <th>Product</th>
                                <th width="120">Price</th>
                                <th width="170">Quantity</th>
                                <th width="140">Subtotal</th>
                                <th width="100">Action</th>
                            </tr>
                            </thead>

                            <tbody>

                            <c:forEach var="item" items="${items}">

                                <c:set var="lineSubtotal" value="${item.lineTotal}"/>
                                <c:set var="cartTotal" value="${cartTotal + lineSubtotal}"/>
                                <c:set var="itemCount" value="${itemCount + item.quantity}"/>

                                <tr>

                                    <td>
                                        <div class="d-flex align-items-center gap-3">

                                            <div class="flex-shrink-0">

                                                <c:choose>

                                                    <c:when test="${not empty item.product.imageUrl}">
                                                        <img src="${item.product.imageUrl}"
                                                             alt="${item.product.name}"
                                                             style="width:72px;height:72px;object-fit:cover;border-radius:12px;border:1px solid #e9ecef;">
                                                    </c:when>

                                                    <c:otherwise>
                                                        <div class="d-flex align-items-center justify-content-center bg-light text-secondary"
                                                             style="width:72px;height:72px;border-radius:12px;border:1px solid #e9ecef;">
                                                            <span class="small">No image</span>
                                                        </div>
                                                    </c:otherwise>

                                                </c:choose>

                                            </div>

                                            <div>

                                                <div class="fw-semibold">
                                                    ${item.product.name}
                                                </div>

                                                <div class="small text-muted mt-1">

                                                    <c:if test="${not empty item.product.category}">
                                                        <span class="me-2">
                                                            Category: ${item.product.category}
                                                        </span>
                                                    </c:if>

                                                    <c:if test="${not empty item.product.unit}">
                                                        <span>
                                                            Unit: ${item.product.unit}
                                                        </span>
                                                    </c:if>

                                                </div>

                                                <c:if test="${not item.product.active}">
                                                    <div class="mt-2">
                                                        <span class="badge bg-danger">
                                                            Unavailable
                                                        </span>
                                                    </div>
                                                </c:if>

                                            </div>

                                        </div>
                                    </td>

                                    <td class="fw-medium">
                                        ${item.product.sellPrice}
                                    </td>

                                    <td>

                                        <form action="${pageContext.request.contextPath}/cart"
                                              method="post"
                                              class="d-flex gap-2 align-items-center">

                                            <input type="hidden"
                                                   name="csrf_token"
                                                   value="${sessionScope.CSRF_TOKEN}" />

                                            <input type="hidden"
                                                   name="action"
                                                   value="update"/>

                                            <input type="hidden"
                                                   name="productId"
                                                   value="${item.product.id}"/>

                                            <input type="number"
                                                   name="qty"
                                                   min="1"
                                                   max="999"
                                                   value="${item.quantity}"
                                                   class="form-control form-control-sm"/>

                                            <button class="btn btn-primary btn-sm">
                                                Update
                                            </button>

                                        </form>

                                        <div class="small text-muted mt-2">
                                            Quantity must be at least 1.
                                        </div>

                                    </td>

                                    <td>
                                        <div class="fw-bold text-success">
                                            ${lineSubtotal}
                                        </div>
                                    </td>

                                    <td>

                                        <form action="${pageContext.request.contextPath}/cart"
                                              method="post">

                                            <input type="hidden"
                                                   name="csrf_token"
                                                   value="${sessionScope.CSRF_TOKEN}" />

                                            <input type="hidden"
                                                   name="action"
                                                   value="remove"/>

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

                    </div>
                </div>
            </div>
        </div>

        <!-- RIGHT: ORDER SUMMARY -->

        <div class="col-lg-4">

            <div class="card border-0 shadow-sm sticky-top" style="top: 90px;">

                <div class="card-body">

                    <h5 class="fw-bold mb-3">
                        Order Summary
                    </h5>

                    <div class="d-flex justify-content-between mb-2">
                        <span class="text-muted">Items</span>
                        <span>${itemCount}</span>
                    </div>

                    <div class="d-flex justify-content-between mb-2">
                        <span class="text-muted">Subtotal</span>
                        <span>${cartTotal}</span>
                    </div>

                    <div class="d-flex justify-content-between mb-2">
                        <span class="text-muted">Shipping</span>
                        <span>0</span>
                    </div>

                    <div class="d-flex justify-content-between mb-3">
                        <span class="text-muted">Discount</span>
                        <span>0</span>
                    </div>

                    <hr>

                    <div class="d-flex justify-content-between align-items-center mb-3">
                        <span class="fw-semibold">
                            Grand Total
                        </span>
                        <span class="fw-bold fs-4 text-success">
                            ${cartTotal}
                        </span>
                    </div>

                    <c:choose>

                        <c:when test="${empty sessionScope.authUser}">
                            <a href="${pageContext.request.contextPath}/login"
                               class="btn btn-warning w-100 mb-2">
                                Login to Checkout
                            </a>
                        </c:when>

                        <c:otherwise>

                            <!-- ===== CHỈ SỬA CHỖ NÀY ===== -->

                            <form action="${pageContext.request.contextPath}/checkout-review"
                                  method="get"
                                  class="d-grid mb-2">

                                <button type="submit"
                                        class="btn btn-success">
                                    Checkout
                                </button>

                            </form>

                        </c:otherwise>

                    </c:choose>

                    <a href="${pageContext.request.contextPath}/catalog"
                       class="btn btn-outline-secondary w-100">
                        ← Continue Shopping
                    </a>

                    <div class="small text-muted mt-3">
                        Review your items before checkout. Prices and stock are subject to availability.
                    </div>

                </div>
            </div>
        </div>

    </div>

</c:if>

<c:if test="${empty items}">
    <div class="mt-3">
        <a href="${pageContext.request.contextPath}/catalog"
           class="btn btn-secondary">
            ← Continue Shopping
        </a>

        <c:if test="${empty sessionScope.authUser}">
            <a href="${pageContext.request.contextPath}/login"
               class="btn btn-warning ms-2">
                Login
            </a>
        </c:if>
    </div>
</c:if>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>