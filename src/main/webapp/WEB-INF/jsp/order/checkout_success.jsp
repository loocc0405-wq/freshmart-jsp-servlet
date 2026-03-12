<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>

<c:set var="pageTitle" value="Order Success"/>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"/>

<div class="text-center mt-5">

    <div class="card shadow-sm border-0 mx-auto" style="max-width:600px">

        <div class="card-body p-5">

            <div class="mb-4">

                <i class="bi bi-check-circle-fill text-success"
                   style="font-size:60px"></i>

            </div>

            <h3 class="fw-bold mb-3">
                Order Completed!
            </h3>

            <p class="text-muted">

                Thank you for your purchase.

                Your order has been successfully created.

            </p>

            <hr>

            <div class="mb-3">

                <strong>Order ID:</strong>
                ${order.id}

            </div>

            <div class="mb-4">

                <strong>Total:</strong>
                ${order.totalAmount}

            </div>

            <a href="${pageContext.request.contextPath}/catalog"
               class="btn btn-primary">

                Continue Shopping

            </a>

            <a href="${pageContext.request.contextPath}/customer/orders"
               class="btn btn-outline-secondary ms-2">

                View Orders

            </a>

        </div>

    </div>

</div>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>