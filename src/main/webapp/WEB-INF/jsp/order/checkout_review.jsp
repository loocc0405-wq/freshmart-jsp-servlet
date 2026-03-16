<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:set var="pageTitle" value="Audit & Finalize Order | FreshMart Enterprise"/>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"/>

<div class="container py-5">
    <div class="row justify-content-center">
        <div class="col-xl-10">
            <div class="fm-page-header mb-5 border-bottom pb-4">
                <div>
                    <div class="fm-caption fw-bold text-primary mb-1 text-uppercase ls-wide">Procurement Sequence</div>
                    <h1 class="fm-page-title">Final Audit & Settlement</h1>
                    <p class="fm-page-subtitle">Verify your logistics allocation and financial settlement before initializing fulfillment.</p>
                </div>
                <div class="d-none d-md-block">
                    <div class="d-flex align-items-center gap-3">
                        <div class="text-end">
                            <div class="small fw-bold text-success"><i class="bi bi-shield-check"></i> Secure Checkout</div>
                            <div class="fm-caption opacity-50">SSL-Encrypted Transaction</div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="row g-5">
                <!-- Order Ledger Details -->
                <div class="col-lg-8">
                    <div class="fm-surface p-0 overflow-hidden shadow-sm border-0 mb-4">
                        <div class="p-4 bg-light border-bottom d-flex align-items-center justify-content-between">
                            <h2 class="fm-h3 mb-0">Procurement Ledger</h2>
                            <span class="badge bg-white text-dark border py-2 px-3 fw-bold rounded-pill shadow-xs">${items.size()} Unique SKU Entries</span>
                        </div>

                        <c:set var="cartTotal" value="0"/>
                        <c:set var="itemCount" value="0"/>

                        <div class="table-responsive">
                            <table class="table fm-data-table align-middle mb-0">
                                <thead class="bg-slate-50">
                                    <tr>
                                        <th class="ps-4">Product Attribution</th>
                                        <th class="text-end">Unit Price</th>
                                        <th class="text-center">Quantity</th>
                                        <th class="text-end pe-4">Settlement Line</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="item" items="${items}">
                                        <c:set var="lineSubtotal" value="${item.lineTotal}"/>
                                        <c:set var="cartTotal" value="${cartTotal + lineSubtotal}"/>
                                        <c:set var="itemCount" value="${itemCount + item.quantity}"/>
                                        <tr>
                                            <td class="ps-4">
                                                <div class="d-flex align-items-center gap-3 py-1">
                                                    <div class="fm-product-media" style="width: 52px; height: 52px; border-radius: 8px; flex-shrink: 0;">
                                                        <c:choose>
                                                            <c:when test="${not empty item.product.imageUrl}">
                                                                <img src="${item.product.imageUrl}" alt="${item.product.name}" class="w-100 h-100 object-fit-cover">
                                                            </c:when>
                                                            <c:otherwise>
                                                                <div class="w-100 h-100 bg-light d-flex align-items-center justify-content-center text-muted small">N/A</div>
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </div>
                                                    <div>
                                                        <div class="fw-bold text-dark"><c:out value="${item.product.name}"/></div>
                                                        <div class="fm-caption text-uppercase opacity-50 fw-bold">SKU-${item.product.id} • <c:out value="${item.product.category}"/></div>
                                                    </div>
                                                </div>
                                            </td>
                                            <td class="text-end fw-semibold">
                                                <fmt:formatNumber value="${item.product.sellPrice}" type="number" groupingUsed="true"/> ₫
                                            </td>
                                            <td class="text-center">
                                                <span class="badge bg-light text-dark border px-3 py-2 fw-bold font-monospace">${item.quantity}</span>
                                            </td>
                                            <td class="text-end fw-bold text-primary pe-4">
                                                <fmt:formatNumber value="${lineSubtotal}" type="number" groupingUsed="true"/> ₫
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </div>

                    <!-- Logistics Info Cards -->
                    <div class="row g-4">
                        <div class="col-md-6">
                            <div class="fm-surface p-4 border-0 shadow-sm">
                                <h3 class="fm-h3 mb-3"><i class="bi bi-truck text-primary me-2"></i> Logistics Destination</h3>
                                <div class="p-3 bg-light rounded-3 small fw-medium text-muted mb-0">
                                    Estimated Delivery Hub: <strong>Standard Fulfillment Route</strong><br>
                                    Delivery sequence will be initialized upon settlement.
                                </div>
                            </div>
                        </div>
                        <div class="col-md-6">
                            <div class="fm-surface p-4 border-0 shadow-sm">
                                <h3 class="fm-h3 mb-3"><i class="bi bi-credit-card text-primary me-2"></i> Payment Integrity</h3>
                                <div class="p-3 bg-light rounded-3 small fw-medium text-muted mb-0">
                                    Method: <strong>Standard Billing</strong><br>
                                    Transaction identity is verified for audit compliance.
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Final Settlement Sidebar -->
                <div class="col-lg-4">
                    <div class="sticky-top" style="top: 100px;">
                        <div class="fm-surface p-4 shadow-sm border-0 bg-slate-900 text-white">
                            <h2 class="fm-h3 mb-4 pb-3 border-bottom border-secondary text-white">Settlement Summary</h2>
                            
                            <div class="d-flex justify-content-between mb-3">
                                <span class="fm-caption fw-bold opacity-75 text-uppercase">Total Quantity</span>
                                <span class="fw-bold fs-5">${itemCount} Units</span>
                            </div>
                            
                            <div class="d-flex justify-content-between mb-3">
                                <span class="fm-caption fw-bold opacity-75 text-uppercase">Subtotal Value</span>
                                <span class="fw-bold">
                                    <fmt:formatNumber value="${cartTotal}" type="number"/> ₫
                                </span>
                            </div>
                            
                            <div class="d-flex justify-content-between mb-4">
                                <span class="fm-caption fw-bold opacity-75 text-uppercase">Logistics Allocation</span>
                                <span class="text-success small fw-bold">COMPLIMENTARY</span>
                            </div>

                            <hr class="border-secondary mb-4">

                            <div class="d-flex justify-content-between align-items-end mb-5">
                                <span class="fm-h3 mb-0 text-white">Grand Settlement</span>
                                <div class="text-end">
                                    <div class="fm-h1 mb-0 text-primary" style="font-size: 2.5rem;">
                                        <fmt:formatNumber value="${cartTotal}" type="number"/> 
                                        <span class="fs-4 opacity-50 text-white">₫</span>
                                    </div>
                                </div>
                            </div>

                            <form action="${pageContext.request.contextPath}/customer/checkout" method="post" id="finalizeForm">
                                <input type="hidden" name="csrf_token" value="${sessionScope.CSRF_TOKEN}"/>
                                <button type="submit" class="fm-btn fm-btn-primary w-100 py-3 fs-5 shadow-lg border-0">
                                    Finalize Procurement <i class="bi bi-chevron-right ms-2"></i>
                                </button>
                            </form>
                            
                            <a href="${pageContext.request.contextPath}/cart-view" class="btn btn-link link-light w-100 mt-3 small opacity-75 text-decoration-none">
                                <i class="bi bi-arrow-left"></i> Edit Logistics Allocation
                            </a>
                        </div>
                        
                        <div class="mt-4 p-3 bg-primary-subtle border-0 rounded-3 small text-primary d-flex gap-2 align-items-start">
                            <i class="bi bi-info-circle-fill mt-1"></i>
                            <div>By finalizing, you authorize the fulfillment sequence and FEFO-optimized logistics handling.</div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>