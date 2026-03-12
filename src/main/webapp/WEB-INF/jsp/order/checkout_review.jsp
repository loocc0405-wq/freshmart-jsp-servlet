<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>

<c:set var="pageTitle" value="Review Order"/>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"/>

<h3 class="mb-4">🧾 Review Your Order</h3>

<c:set var="cartTotal" value="0"/>
<c:set var="itemCount" value="0"/>

<div class="row g-4">

    <!-- ORDER ITEMS -->
    <div class="col-lg-8">
        <div class="card shadow-sm border-0">
            <div class="card-body">

                <table class="table align-middle">
                    <thead class="table-dark">
                    <tr>
                        <th>Product</th>
                        <th>Price</th>
                        <th>Qty</th>
                        <th>Subtotal</th>
                    </tr>
                    </thead>

                    <tbody>

                    <c:forEach var="item" items="${items}">

                        <c:set var="lineSubtotal" value="${item.lineTotal}"/>
                        <c:set var="cartTotal" value="${cartTotal + lineSubtotal}"/>
                        <c:set var="itemCount" value="${itemCount + item.quantity}"/>

                        <tr>

                            <td>
                                <div class="d-flex gap-3 align-items-center">

                                    <img src="${item.product.imageUrl}"
                                         style="width:60px;height:60px;object-fit:cover;border-radius:8px"/>

                                    <div>
                                        <div class="fw-semibold">
                                            ${item.product.name}
                                        </div>

                                        <small class="text-muted">
                                            ${item.product.category}
                                        </small>
                                    </div>

                                </div>
                            </td>

                            <td>${item.product.sellPrice}</td>

                            <td>${item.quantity}</td>

                            <td class="fw-bold text-success">
                                ${lineSubtotal}
                            </td>

                        </tr>

                    </c:forEach>

                    </tbody>
                </table>

            </div>
        </div>
    </div>

    <!-- ORDER SUMMARY -->
    <div class="col-lg-4">

        <div class="card shadow-sm border-0">
            <div class="card-body">

                <h5 class="fw-bold mb-3">Order Summary</h5>

                <div class="d-flex justify-content-between mb-2">
                    <span>Items</span>
                    <span>${itemCount}</span>
                </div>

                <div class="d-flex justify-content-between mb-2">
                    <span>Subtotal</span>
                    <span>${cartTotal}</span>
                </div>

                <div class="d-flex justify-content-between mb-2">
                    <span>Shipping</span>
                    <span>0</span>
                </div>

                <hr>

                <div class="d-flex justify-content-between mb-3">
                    <span class="fw-bold">Grand Total</span>
                    <span class="fw-bold text-success fs-5">
                        ${cartTotal}
                    </span>
                </div>

                <form action="${pageContext.request.contextPath}/customer/checkout"
                      method="post">

                    <input type="hidden"
                           name="csrf_token"
                           value="${sessionScope.CSRF_TOKEN}"/>

                    <button class="btn btn-success w-100">
                        Confirm Order
                    </button>

                </form>

                <a href="${pageContext.request.contextPath}/cart-view"
                   class="btn btn-outline-secondary w-100 mt-2">

                    ← Back to Cart

                </a>

            </div>
        </div>

    </div>

</div>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>