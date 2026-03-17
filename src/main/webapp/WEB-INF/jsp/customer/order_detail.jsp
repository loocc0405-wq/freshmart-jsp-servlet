<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:set var="pageTitle" value="Order Audit | FreshMart Enterprise"/>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"/>

<div class="container py-4">
    <div class="fm-page-header mb-5">
        <div>
            <div class="fm-caption fw-bold text-primary mb-1 text-uppercase">Logistics Record</div>
            <h1 class="fm-page-title">Fulfillment Intelligence</h1>
            <p class="fm-page-subtitle">Granular audit trail for Service ID: <span class="fw-bold text-dark">${order.orderCode}</span></p>
        </div>

        <a class="fm-btn btn-light border" href="${pageContext.request.contextPath}/customer/orders">
            <i class="bi bi-arrow-left me-1"></i> Return to Ledger
        </a>
    </div>

    <c:if test="${not empty errorMessage}">
        <div class="alert alert-danger fm-surface mb-4 border-0 shadow-none"><i class="bi bi-exclamation-triangle-fill me-2"></i> <c:out value="${errorMessage}"/></div>
    </c:if>

    <c:if test="${not empty order}">
        <div class="row g-4">
            <!-- Left Panel: Fulfillment Summary -->
            <div class="col-12 col-lg-4">
                <div class="fm-surface p-4 shadow-sm border-0 sticky-top" style="top: 100px;">
                    <div class="d-flex align-items-center justify-content-between mb-4 pb-3 border-bottom">
                        <h2 class="fm-h3 mb-0">Record Summary</h2>
                        <span class="fm-status-badge ${order.status == 'COMPLETED' ? 'available' : (order.status == 'CANCELED' ? 'disposed' : 'low-stock')} px-3">
                            ${order.status}
                        </span>
                    </div>

                    <div class="mb-4">
                        <div class="fm-caption fw-bold opacity-50 mb-2">INTERNAL SERVICE ID</div>
                        <div class="fm-h3 font-monospace mb-0 text-primary">${order.orderCode}</div>
                    </div>

                    <div class="row g-3 mb-4">
                        <div class="col-6">
                            <div class="fm-caption fw-bold opacity-50 mb-1 text-uppercase">Logistics</div>
                            <div class="small fw-bold">${order.type}</div>
                        </div>
                        <div class="col-6">
                            <div class="fm-caption fw-bold opacity-50 mb-1 text-uppercase">Payment</div>
                            <div class="small fw-bold">${order.paymentMethod}</div>
                        </div>
                    </div>

                    <div class="mb-5">
                        <div class="fm-caption fw-bold opacity-50 mb-1 text-uppercase">Capture Date</div>
                        <div class="small fw-medium text-muted">${order.createdAt}</div>
                    </div>

                    <div class="p-4 bg-primary text-white rounded-3 mb-0 shadow-sm">
                        <div class="fm-caption fw-bold opacity-75 mb-1 text-white text-uppercase">Settlement amount</div>
                        <div class="fm-h1 mb-0">
                            <fmt:formatNumber value="${order.totalAmount}" type="number" groupingUsed="true"/> 
                            <span class="fs-4 opacity-75">₫</span>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Right Panel: Line Items & Audit -->
            <div class="col-12 col-lg-8">
                <div class="fm-surface p-0 overflow-hidden shadow-sm border-0 mb-4">
                    <div class="p-4 border-bottom d-flex align-items-center justify-content-between bg-white">
                        <h2 class="fm-h3 mb-0">Itemized Breakdown</h2>
                        <span class="badge bg-light text-dark border py-2 px-3 fw-bold rounded-pill">${fn:length(order.items)} Unit Types</span>
                    </div>

                    <div class="table-responsive">
                        <table class="table fm-data-table align-middle mb-0">
                            <thead class="bg-light">
                                <tr>
                                    <th class="ps-4">Product Attribution</th>
                                    <th class="text-center">Quantity</th>
                                    <th class="text-end">Unit Value</th>
                                    <th class="text-end pe-4">Line Total</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="item" items="${order.items}">
                                    <tr>
                                        <td class="ps-4">
                                            <div class="d-flex align-items-center gap-3">
                                                <div class="fm-product-media" style="width: 48px; height: 48px; border-radius: 8px;">
                                                    <c:choose>
                                                        <c:when test="${not empty item.product.imageUrl}">
                                                            <img src="${item.product.imageUrl}" alt="${item.product.name}" class="w-100 h-100 object-fit-cover">
                                                        </c:when>
                                                        <c:otherwise>
                                                            <div class="w-100 h-100 bg-light d-flex align-items-center justify-content-center text-muted small"><i class="bi bi-image"></i></div>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </div>
                                                <div>
                                                    <a href="${pageContext.request.contextPath}/product?id=${item.product.id}" class="fw-bold text-dark text-decoration-none hover-primary"><c:out value="${item.product.name}"/></a>
                                                    <div class="fm-caption text-uppercase opacity-50 fw-bold">SKU-${item.product.id}</div>
                                                </div>
                                            </div>
                                        </td>
                                        <td class="text-center">
                                            <div class="badge bg-light text-dark border px-3 py-2 fw-bold font-monospace">${item.quantity}</div>
                                        </td>
                                        <td class="text-end fw-semibold">
                                            <fmt:formatNumber value="${item.unitPrice}" type="number"/> ₫
                                        </td>
                                        <td class="text-end fw-bold text-primary pe-4">
                                            <fmt:formatNumber value="${item.lineTotal}" type="number"/> ₫
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </div>

                <!-- Logistics Timeline Placeholder / Status Detail -->
                <div class="fm-surface p-4 shadow-sm border-0">
                    <h2 class="fm-h3 mb-4">Lifecycle Audit Trail</h2>
                    <div class="d-flex gap-4">
                        <div class="text-center d-flex flex-column align-items-center" style="width: 40px;">
                            <div class="bg-success text-white rounded-circle d-flex align-items-center justify-content-center shadow-sm" style="width: 32px; height: 32px; z-index: 1;">
                                <i class="bi bi-check2"></i>
                            </div>
                            <div class="bg-slate-200" style="width: 2px; flex-grow: 1;"></div>
                        </div>
                        <div class="pb-5">
                            <div class="fw-bold">Procurement Record Initialized</div>
                            <div class="small fm-text-secondary">${order.createdAt}</div>
                            <p class="mt-2 small mb-0">Service ID <span class="fw-bold">${order.orderCode}</span> was captured and assigned to the fulfillment queue.</p>
                        </div>
                    </div>
                    
                    <div class="d-flex gap-4">
                        <div class="text-center d-flex flex-column align-items-center" style="width: 40px;">
                            <div class="bg-primary text-white rounded-circle d-flex align-items-center justify-content-center shadow-sm" style="width: 32px; height: 32px; z-index: 1;">
                                <i class="bi bi-gear-fill"></i>
                            </div>
                            <c:if test="${order.status != 'PENDING' && order.status != 'PROCESSING'}"><div class="bg-slate-200" style="width: 2px; flex-grow: 1;"></div></c:if>
                        </div>
                        <div class="pb-2">
                            <div class="fw-bold">Latest Operational Phase: ${order.status}</div>
                            <p class="mt-2 small mb-0">
                                <c:choose>
                                    <c:when test="${order.status == 'PENDING'}">Record is awaiting administrative validation.</c:when>
                                    <c:when test="${order.status == 'PROCESSING'}">Produce is being prepared using FEFO sequence.</c:when>
                                    <c:when test="${order.status == 'SHIPPING'}">Logistics dispatch has initialized delivery sequence.</c:when>
                                    <c:when test="${order.status == 'COMPLETED'}">Service target achieved. Fulfillment successfully verified.</c:when>
                                    <c:otherwise>Logistics sequence terminated or voided.</c:otherwise>
                                </c:choose>
                            </p>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </c:if>
</div>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>