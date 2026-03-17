<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<c:set var="pageTitle" value="Order Ledger | FreshMart Enterprise"/>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"/>

<div class="container-fluid px-4 py-4">
    <div class="fm-page-header mb-5">
        <div>
            <div class="fm-caption fw-bold text-primary mb-1 text-uppercase">Financial Audit</div>
            <h1 class="fm-page-title">Procurement History</h1>
            <p class="fm-page-subtitle">Full historical ledger of all fulfillments and logistics requests.</p>
        </div>

        <a class="fm-btn fm-btn-primary" href="${pageContext.request.contextPath}/catalog">
            <i class="bi bi-cart-plus me-1"></i> New Procurement
        </a>
    </div>

    <c:if test="${not empty errorMessage}">
        <div class="alert alert-danger fm-surface mb-4 shadow-none border-0 bg-danger-subtle"><i class="bi bi-exclamation-octagon me-2"></i> <c:out value="${errorMessage}"/></div>
    </c:if>

    <!-- Advanced Filter Bar -->
    <div class="fm-surface p-4 mb-5 border-0 shadow-sm">
        <form class="row g-3 align-items-end" method="get" action="${pageContext.request.contextPath}/customer/orders">
            <div class="col-12 col-md-3">
                <label class="fm-caption fw-bold d-block mb-2">Service Status</label>
                <select name="status" class="fm-form-control">
                    <option value="">All Statuses</option>
                    <option value="PENDING"    ${selectedStatus == 'PENDING'    ? 'selected' : ''}>PENDING</option>
                    <option value="PROCESSING" ${selectedStatus == 'PROCESSING' ? 'selected' : ''}>PROCESSING</option>
                    <option value="SHIPPING"   ${selectedStatus == 'SHIPPING'   ? 'selected' : ''}>SHIPPING</option>
                    <option value="COMPLETED"  ${selectedStatus == 'COMPLETED'  ? 'selected' : ''}>COMPLETED</option>
                    <option value="CANCELED"   ${(selectedStatus == 'CANCELED' || selectedStatus == 'CANCELLED') ? 'selected' : ''}>CANCELED</option>
                </select>
            </div>

            <div class="col-12 col-md-3">
                <label class="fm-caption fw-bold d-block mb-2">From Date</label>
                <input type="date" class="fm-form-control" name="fromDate" value="${fromDate}"/>
            </div>

            <div class="col-12 col-md-3">
                <label class="fm-caption fw-bold d-block mb-2">To Date</label>
                <input type="date" class="fm-form-control" name="toDate" value="${toDate}"/>
            </div>

            <div class="col-12 col-md-3 d-flex gap-2">
                <button type="submit" class="fm-btn fm-btn-primary flex-grow-1">Execute Query</button>
                <a href="${pageContext.request.contextPath}/customer/orders" class="fm-btn btn-light border" title="Reset Filters"><i class="bi bi-arrow-counterclockwise"></i></a>
            </div>
        </form>
        
        <div class="mt-3 small fm-text-secondary">
            Query returned <strong>${totalItems}</strong> fulfillment records.
        </div>
    </div>

    <!-- Results Table -->
    <div class="fm-surface p-0 overflow-hidden shadow-sm border-0">
        <div class="table-responsive">
            <table class="table fm-data-table align-middle mb-0">
                <thead class="bg-light">
                    <tr>
                        <th class="ps-4">Service ID</th>
                        <th>Lifecycle Stage</th>
                        <th>Method</th>
                        <th>Category</th>
                        <th class="text-end">Fulfillment Value</th>
                        <th>Timestamp</th>
                        <th class="text-end pe-4">Thao tác</th>
                    </tr>
                </thead>
                <tbody>
                <c:choose>
                    <c:when test="${not empty orders}">
                        <c:forEach var="o" items="${orders}">
                            <tr>
                                <td class="ps-4"><span class="font-monospace fw-bold text-primary">${o.orderCode}</span></td>
                                <td>
                                    <span class="fm-status-badge ${o.status == 'COMPLETED' ? 'available' : (o.status == 'CANCELED' ? 'disposed' : 'low-stock')} px-2 small">
                                        ${o.status}
                                    </span>
                                </td>
                                <td><span class="small fw-bold opacity-75">${o.type}</span></td>
                                <td><span class="small fw-bold opacity-75">${o.paymentMethod}</span></td>
                                <td class="text-end fw-bold">
                                    <fmt:formatNumber value="${o.totalAmount}" type="number" groupingUsed="true"/> ₫
                                </td>
                                <td class="small fm-text-secondary">${o.createdAt}</td>
                                <td class="text-end pe-4">
                                    <a class="fm-btn btn-light border btn-sm small fw-bold"
                                       href="${pageContext.request.contextPath}/customer/order-detail?id=${o.id}">
                                        Audit Details
                                    </a>
                                </td>
                            </tr>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <tr>
                            <td colspan="7" class="p-5 text-center fm-text-secondary">
                                <i class="bi bi-inbox fs-2 opacity-25 d-block mb-3"></i>
                                No procurement data matched the current query parameters.
                            </td>
                        </tr>
                    </c:otherwise>
                </c:choose>
                </tbody>
            </table>
        </div>

        <!-- Pagination -->
        <c:if test="${totalPages > 1}">
            <div class="p-4 bg-light border-top">
                <nav>
                    <ul class="pagination pagination-enterprise mb-0 justify-content-center gap-2">
                        <c:if test="${currentPage > 0}">
                            <li class="page-item">
                                <a class="page-link rounded"
                                   href="${pageContext.request.contextPath}/customer/orders?status=${selectedStatus}&fromDate=${fromDate}&toDate=${toDate}&page=${currentPage - 1}">
                                    <i class="bi bi-chevron-left"></i> Previous
                                </a>
                            </li>
                        </c:if>

                        <c:forEach var="i" begin="0" end="${totalPages - 1}">
                            <li class="page-item ${i == currentPage ? 'active' : ''}">
                                <a class="page-link rounded fw-bold"
                                   href="${pageContext.request.contextPath}/customer/orders?status=${selectedStatus}&fromDate=${fromDate}&toDate=${toDate}&page=${i}">
                                    ${i + 1}
                                </a>
                            </li>
                        </c:forEach>

                        <c:if test="${currentPage + 1 < totalPages}">
                            <li class="page-item">
                                <a class="page-link rounded"
                                   href="${pageContext.request.contextPath}/customer/orders?status=${selectedStatus}&fromDate=${fromDate}&toDate=${toDate}&page=${currentPage + 1}">
                                    Next <i class="bi bi-chevron-right"></i>
                                </a>
                            </li>
                        </c:if>
                    </ul>
                </nav>
            </div>
        </c:if>
    </div>
</div>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>