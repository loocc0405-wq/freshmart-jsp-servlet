<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="pageTitle" value="Real-time Inventory Ledger"/>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"/>

<div class="container-fluid px-4 py-4">
    <!-- Page Header & Filter Trigger -->
    <div class="d-flex flex-wrap align-items-center justify-content-between gap-3 mb-4">
        <div>
            <h1 class="fm-h1 mb-1">Inventory Management</h1>
            <p class="fm-text-secondary mb-0">Multi-batch FEFO tracking and supply chain visibility.</p>
        </div>
        <div class="d-flex gap-2">
            <button class="fm-btn btn-light border small" data-bs-toggle="collapse" data-bs-target="#filterPanel">
                <i class="bi bi-funnel me-2"></i>Advanced Filters
            </button>
            <a href="${pageContext.request.contextPath}/staff/import-lot" class="fm-btn fm-btn-primary small">
                <i class="bi bi-plus-lg me-2"></i>Import Batch
            </a>
        </div>
    </div>

    <!-- Filter Panel -->
    <div class="collapse ${filter != null ? 'show' : ''} mb-4" id="filterPanel">
        <div class="fm-surface p-4">
            <form method="get" action="${pageContext.request.contextPath}/staff/inventory" class="row g-3">
                <div class="col-md-3">
                    <label class="form-label fw-bold small">Product Master</label>
                    <select class="fm-form-control" name="productId">
                        <option value="">All Products</option>
                        <c:forEach items="${products}" var="p">
                            <option value="${p.id}" ${filter.productId == p.id ? 'selected' : ''}>
                                <c:out value="${p.name}"/> (${p.id})
                            </option>
                        </c:forEach>
                    </select>
                </div>
                <div class="col-md-3">
                    <label class="form-label fw-bold small">Supplier Origin</label>
                    <select class="fm-form-control" name="supplierId">
                        <option value="">All Suppliers</option>
                        <c:forEach items="${suppliers}" var="s">
                            <option value="${s.id}" ${filter.supplierId == s.id ? 'selected' : ''}>
                                <c:out value="${s.name}"/>
                            </option>
                        </c:forEach>
                    </select>
                </div>
                <div class="col-md-2">
                    <label class="form-label fw-bold small">Audit Status</label>
                    <select class="fm-form-control" name="status">
                        <option value="">All Statuses</option>
                        <option value="AVAILABLE" ${filter.status == 'AVAILABLE' ? 'selected' : ''}>Active / Available</option>
                        <option value="EXPIRING" ${filter.status == 'EXPIRING' ? 'selected' : ''}>Critical Expiry</option>
                        <option value="EXPIRED" ${filter.status == 'EXPIRED' ? 'selected' : ''}>Expired (Void)</option>
                    </select>
                </div>
                <div class="col-md-2">
                    <label class="form-label fw-bold small">Min Qty</label>
                    <input type="number" class="fm-form-control" name="minQtyLeft" value="${filter.minQtyLeft}">
                </div>
                <div class="col-md-2 d-flex align-items-end gap-2">
                    <button type="submit" class="fm-btn fm-btn-primary w-100">Apply</button>
                    <a href="${pageContext.request.contextPath}/staff/inventory" class="fm-btn btn-light border">Reset</a>
                </div>
            </form>
        </div>
    </div>

    <!-- Success/Error Messages -->
    <c:if test="${not empty successMessage}">
        <div class="alert alert-success border-0 bg-success-subtle mb-4"><i class="bi bi-check-circle me-2"></i><c:out value="${successMessage}"/></div>
    </c:if>
    <c:if test="${not empty errorMessage}">
        <div class="alert alert-danger border-0 bg-danger-subtle mb-4"><i class="bi bi-exclamation-octagon me-2"></i><c:out value="${errorMessage}"/></div>
    </c:if>

    <!-- Detailed Ledger Table -->
    <div class="fm-surface overflow-hidden">
        <table class="fm-data-table">
            <thead>
                <tr>
                    <th>Batch ID</th>
                    <th>Product & Logic</th>
                    <th>Supplier</th>
                    <th>Engagement Date</th>
                    <th>Expiry (FEFO)</th>
                    <th class="text-end">Initial</th>
                    <th class="text-end">Current</th>
                    <th>Compliance</th>
                    <th class="text-end">Actions</th>
                </tr>
            </thead>
            <tbody>
                <c:if test="${empty filteredLots}">
                    <tr>
                        <td colspan="9" class="text-center py-5">
                            <div class="fm-text-muted mb-2"><i class="bi bi-inbox fs-1 opacity-25"></i></div>
                            <div class="fw-bold">No active batches found</div>
                            <div class="small text-muted">Try adjusting your filters or import new inventory.</div>
                        </td>
                    </tr>
                </c:if>
                <c:forEach items="${filteredLots}" var="lot">
                    <c:set var="isExp" value="${lot.expiryDate lt today}"/>
                    <c:set var="isCrit" value="${lot.qtyLeft > 0 && lot.expiryDate ge today && (lot.expiryDate.toEpochDay() - today.toEpochDay()) <= 7}"/>
                    <c:set var="isCons" value="${lot.qtyLeft == 0}"/>
                    
                    <tr>
                        <td><span class="font-monospace text-muted small">#LOT-${lot.id}</span></td>
                        <td>
                            <div class="fw-bold"><c:out value="${lot.product.name}"/></div>
                            <div class="fm-caption fw-semibold text-primary">FEFO PROTECTED</div>
                        </td>
                        <td class="small"><c:out value="${lot.supplier != null ? lot.supplier.name : '-'}"/></td>
                        <td class="small text-muted"><c:out value="${lot.importDate}"/></td>
                        <td>
                            <div class="fw-semibold ${isExp ? 'text-danger' : (isCrit ? 'text-warning' : '')}">
                                <c:out value="${lot.expiryDate}"/>
                            </div>
                        </td>
                        <td class="text-end small"><c:out value="${lot.qtyIn}"/></td>
                        <td class="text-end fw-bold"><c:out value="${lot.qtyLeft}"/></td>
                        <td>
                            <c:choose>
                                <c:when test="${isExp}">
                                    <span class="fm-status-badge expired"><i class="bi bi-x-circle"></i> Expired</span>
                                </c:when>
                                <c:when test="${isCons}">
                                    <span class="fm-status-badge" style="background:#f1f5f9; color:#475569;">Consumed</span>
                                </c:when>
                                <c:when test="${isCrit}">
                                    <span class="fm-status-badge expiring"><i class="bi bi-clock-history"></i> Priority Sell</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="fm-status-badge available"><i class="bi bi-check2"></i> Optimized</span>
                                </c:otherwise>
                            </c:choose>
                        </td>
                        <td class="text-end">
                            <div class="dropdown">
                                <button class="btn btn-link link-dark p-0" data-bs-toggle="dropdown"><i class="bi bi-three-dots-vertical"></i></button>
                                <ul class="dropdown-menu dropdown-menu-end border-0 shadow-sm small">
                                    <li><a class="dropdown-item" href="${pageContext.request.contextPath}/staff/import-lot?id=${lot.id}"><i class="bi bi-pencil me-2"></i>Edit Lot Data</a></li>
                                    <c:if test="${isExp && lot.qtyLeft > 0}">
                                        <li><hr class="dropdown-divider"></li>
                                        <li>
                                            <form action="${pageContext.request.contextPath}/staff/delete-lot" method="post" onsubmit="return confirm('Confirm removal of expired stock?');">
                                                <input type="hidden" name="lotId" value="${lot.id}">
                                                <input type="hidden" name="csrf_token" value="${sessionScope.CSRF_TOKEN}">
                                                <button type="submit" class="dropdown-item text-danger"><i class="bi bi-trash me-2"></i>Mark for Disposal</button>
                                            </form>
                                        </li>
                                    </c:if>
                                </ul>
                            </div>
                        </td>
                    </tr>
                </c:forEach>
            </tbody>
        </table>
    </div>

    <!-- Summary Stats when specific product is selected -->
    <c:if test="${selectedProduct != null}">
        <div class="row g-4 mt-2">
            <div class="col-md-3">
                <div class="fm-card text-center">
                    <div class="fm-caption text-uppercase fw-bold opacity-75 mb-1">Total Intake</div>
                    <div class="fm-h3 mb-0"><c:out value="${stockSummary.totalIn}"/></div>
                </div>
            </div>
            <div class="col-md-3">
                <div class="fm-card text-center">
                    <div class="fm-caption text-uppercase fw-bold opacity-75 mb-1">Available Bio-Stock</div>
                    <div class="fm-h3 mb-0 text-success"><c:out value="${stockSummary.availableQty}"/></div>
                </div>
            </div>
            <div class="col-md-3">
                <div class="fm-card text-center border-danger-subtle">
                    <div class="fm-caption text-uppercase fw-bold opacity-75 mb-1">Waste Risk (Expired)</div>
                    <div class="fm-h3 mb-0 text-danger"><c:out value="${stockSummary.expiredQty}"/></div>
                </div>
            </div>
        </div>
    </c:if>
</div>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>