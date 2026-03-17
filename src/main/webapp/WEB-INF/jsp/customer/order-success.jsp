<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<c:set var="pageTitle" value="Fulfillment Confirmed | FreshMart Enterprise"/>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"/>

<div class="container py-5 mt-4">
    <div class="row justify-content-center">
        <div class="col-xl-8 text-center">
            <!-- Animated Success Icon -->
            <div class="mb-5">
                <div class="bg-success text-white rounded-circle d-inline-flex align-items-center justify-content-center shadow-lg hover-lift mb-4" style="width: 100px; height: 100px;">
                    <i class="bi bi-check-lg" style="font-size: 3.5rem;"></i>
                </div>
                <h1 class="display-5 fw-bold" style="font-family: 'Outfit', sans-serif;">Fulfillment Initialized</h1>
                <p class="fs-5 text-muted">Your procurement sequence has been successfully recorded and assigned to our hub.</p>
            </div>

            <c:if test="${not empty order}">
                <!-- Order Intelligence Card -->
                <div class="fm-surface p-0 overflow-hidden shadow-sm text-start mb-5 border-0">
                    <div class="bg-primary text-white p-4">
                        <div class="row align-items-center">
                            <div class="col-md-6">
                                <div class="fm-caption fw-bold opacity-75 text-white mb-1">SERVICE RECORD ID</div>
                                <div class="fm-h3 mb-0 font-monospace">${order.orderCode}</div>
                            </div>
                            <div class="col-md-6 text-md-end mt-3 mt-md-0">
                                <div class="fm-caption fw-bold opacity-75 text-white mb-1 text-uppercase">Capturing status</div>
                                <div class="badge bg-white text-primary border-0 rounded-pill px-3 py-2 fw-bold">${order.status}</div>
                            </div>
                        </div>
                    </div>

                    <div class="p-4 p-md-5">
                        <div class="row g-4 mb-5">
                            <div class="col-6 col-md-3">
                                <div class="fm-caption fw-bold opacity-50 mb-1">METHOD</div>
                                <div class="small fw-bold">${order.paymentMethod}</div>
                            </div>
                            <div class="col-6 col-md-3">
                                <div class="fm-caption fw-bold opacity-50 mb-1">TIMESTAMP</div>
                                <div class="small fw-bold">Captured Now</div>
                            </div>
                            <div class="col-12 col-md-6 text-md-end">
                                <div class="fm-caption fw-bold opacity-50 mb-1">SETTLEMENT VALUE</div>
                                <div class="fm-h2 text-primary mb-0">
                                    <fmt:formatNumber value="${order.totalAmount}" type="number"/> 
                                    <span class="fs-5 opacity-50">₫</span>
                                </div>
                            </div>
                        </div>

                        <div class="table-responsive">
                            <table class="table fm-data-table align-middle">
                                <thead class="bg-light">
                                    <tr>
                                        <th>Product Attribution</th>
                                        <th class="text-center">QTY</th>
                                        <th class="text-end">Line Total</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach items="${order.items}" var="item">
                                        <tr>
                                            <td><span class="fw-bold text-dark"><c:out value="${item.product.name}"/></span></td>
                                            <td class="text-center font-monospace small fw-bold">${item.quantity}</td>
                                            <td class="text-end fw-bold">
                                                <fmt:formatNumber value="${item.lineTotal}" type="number"/> ₫
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </c:if>

            <!-- Navigation Actions -->
            <div class="d-flex flex-wrap justify-content-center gap-3">
                <a class="fm-btn fm-btn-primary px-5 py-3 fs-5" href="${pageContext.request.contextPath}/customer/orders">
                    <i class="bi bi-journal-text me-2"></i> Audit My Orders
                </a>
                <a class="fm-btn btn-light border px-5 py-3 fs-5 fw-bold" href="${pageContext.request.contextPath}/catalog">
                    <i class="bi bi-cart3 me-2"></i> Resume Catalog
                </a>
            </div>

            <div class="mt-5 pt-4 text-muted small border-top d-flex align-items-center justify-content-center gap-4">
                <span><i class="bi bi-shield-lock-fill me-1"></i> SSL Protected Settlement</span>
                <span><i class="bi bi-truck me-1"></i> Hub Logistics Initialized</span>
            </div>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>