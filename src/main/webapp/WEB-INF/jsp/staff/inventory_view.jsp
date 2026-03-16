<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="pageTitle" value="Inventory Ledger | FreshMart Ops"/>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"/>

<style>
    .fm-inventory-header {
        background: var(--fm-surface);
        border-bottom: 1px solid var(--fm-slate-100);
        margin: calc(var(--fm-sp-4) * -1) calc(var(--fm-sp-4) * -1) var(--fm-sp-6);
        padding: var(--fm-sp-8) var(--fm-sp-10);
    }
    .fm-kpi-card {
        background: var(--fm-surface);
        border: 1px solid var(--fm-slate-100);
        padding: var(--fm-sp-5);
        border-radius: var(--fm-radius-card);
        display: flex;
        align-items: center;
        gap: var(--fm-sp-4);
        transition: all 0.2s ease;
    }
    .fm-kpi-card:hover {
        border-color: var(--fm-primary-200);
        background: var(--fm-slate-50);
    }
    .fm-kpi-icon {
        width: 48px;
        height: 48px;
        border-radius: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 1.25rem;
    }
    .fm-inventory-table thead th {
        background: var(--fm-slate-50);
        font-size: 0.7rem;
        font-weight: 800;
        text-transform: uppercase;
        letter-spacing: 0.05em;
        color: var(--fm-slate-500);
        padding: var(--fm-sp-4) var(--fm-sp-5);
        border-bottom: 2px solid var(--fm-slate-100);
    }
    .fm-inventory-table tbody td {
        padding: var(--fm-sp-4) var(--fm-sp-5);
        vertical-align: middle;
        border-bottom: 1px solid var(--fm-slate-50);
    }
    .fm-batch-id {
        font-family: var(--fm-font-mono);
        font-size: 0.75rem;
        background: var(--fm-slate-100);
        color: var(--fm-slate-600);
        padding: 0.2rem 0.5rem;
        border-radius: 4px;
        font-weight: 600;
    }
    .status-dot {
        width: 8px;
        height: 8px;
        border-radius: 50%;
        display: inline-block;
        margin-right: 6px;
    }
    .status-dot.available { background-color: var(--fm-primary-500); }
    .status-dot.expiring { background-color: var(--fm-warning); }
    .status-dot.expired { background-color: var(--fm-danger); }
    .status-dot.consumed { background-color: var(--fm-slate-300); }

    .fm-filter-strip {
        background: var(--fm-slate-50);
        border-radius: var(--fm-radius-card);
        padding: var(--fm-sp-4);
        margin-bottom: var(--fm-sp-6);
    }
</style>

<div class="container-fluid px-4 py-4">
    <!-- Operations Header -->
    <div class="fm-inventory-header d-flex align-items-center justify-content-between">
        <div>
            <div class="fm-caption fw-bold text-primary mb-1 text-uppercase ls-wide">Global Fulfillment Network</div>
            <h1 class="fm-h1 mb-0">Inventory Ledger</h1>
        </div>
        <div class="d-flex gap-3">
            <a href="${pageContext.request.contextPath}/staff/import-lot" class="fm-btn fm-btn-primary px-4 py-2 shadow-sm d-flex align-items-center gap-2">
                <i class="bi bi-plus-lg"></i> <span>Import Batch</span>
            </a>
        </div>
    </div>

    <!-- Quick Insights (KPIs) -->
    <div class="row g-4 mb-6">
        <div class="col-xl-3 col-sm-6">
            <div class="fm-kpi-card shadow-sm">
                <div class="fm-kpi-icon bg-primary-subtle text-primary">
                    <i class="bi bi-box-seam"></i>
                </div>
                <div>
                    <div class="fm-caption fw-bold text-slate-400">Total Batches</div>
                    <div class="fm-h3 mb-0">${fn:length(filteredLots)}</div>
                </div>
            </div>
        </div>
        <div class="col-xl-3 col-sm-6">
            <div class="fm-kpi-card shadow-sm">
                <div class="fm-kpi-icon bg-success-subtle text-success">
                    <i class="bi bi-check-circle"></i>
                </div>
                <div>
                    <div class="fm-caption fw-bold text-slate-400">Optimized/Ready</div>
                    <div class="fm-h3 mb-0 text-success">
                        <c:set var="availCount" value="0"/>
                        <c:forEach items="${filteredLots}" var="l">
                            <c:if test="${l.qtyLeft > 0 && l.expiryDate ge today && (l.expiryDate.toEpochDay() - today.toEpochDay()) > 7}"><c:set var="availCount" value="${availCount + 1}"/></c:if>
                        </c:forEach>
                        ${availCount}
                    </div>
                </div>
            </div>
        </div>
        <div class="col-xl-3 col-sm-6">
            <div class="fm-kpi-card shadow-sm">
                <div class="fm-kpi-icon bg-warning-subtle text-warning">
                    <i class="bi bi-alarm"></i>
                </div>
                <div>
                    <div class="fm-caption fw-bold text-slate-400">Critical Expiry</div>
                    <div class="fm-h3 mb-0 text-warning">
                        <c:set var="critCount" value="0"/>
                        <c:forEach items="${filteredLots}" var="l">
                            <c:if test="${l.qtyLeft > 0 && l.expiryDate ge today && (l.expiryDate.toEpochDay() - today.toEpochDay()) <= 7}"><c:set var="critCount" value="${critCount + 1}"/></c:if>
                        </c:forEach>
                        ${critCount}
                    </div>
                </div>
            </div>
        </div>
        <div class="col-xl-3 col-sm-6">
            <div class="fm-kpi-card shadow-sm">
                <div class="fm-kpi-icon bg-danger-subtle text-danger">
                    <i class="bi bi-shield-x"></i>
                </div>
                <div>
                    <div class="fm-caption fw-bold text-slate-400">Void/Expired</div>
                    <div class="fm-h3 mb-0 text-danger">
                        <c:set var="expCount" value="0"/>
                        <c:forEach items="${filteredLots}" var="l">
                            <c:if test="${l.expiryDate lt today}"><c:set var="expCount" value="${expCount + 1}"/></c:if>
                        </c:forEach>
                        ${expCount}
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- Integrated Filter Strip -->
    <div class="fm-filter-strip shadow-sm border">
        <form method="get" action="${pageContext.request.contextPath}/staff/inventory" class="row g-3 align-items-end">
            <div class="col-lg-3 col-md-4">
                <label class="fm-label text-slate-500 mb-2">Product Master</label>
                <div class="position-relative">
                    <i class="bi bi-search position-absolute top-50 start-0 translate-middle-y ms-3 text-slate-400 opacity-50"></i>
                    <select class="form-select fm-form-control ps-10 border-slate-200 shadow-none" name="productId">
                        <option value="">All Products</option>
                        <c:forEach items="${products}" var="p">
                            <option value="${p.id}" ${filter.productId == p.id ? 'selected' : ''}><c:out value="${p.name}"/></option>
                        </c:forEach>
                    </select>
                </div>
            </div>
            <div class="col-lg-3 col-md-4">
                <label class="fm-label text-slate-500 mb-2">Supplier</label>
                <select class="form-select fm-form-control border-slate-200 shadow-none" name="supplierId">
                    <option value="">All Suppliers</option>
                    <c:forEach items="${suppliers}" var="s">
                        <option value="${s.id}" ${filter.supplierId == s.id ? 'selected' : ''}><c:out value="${s.name}"/></option>
                    </c:forEach>
                </select>
            </div>
            <div class="col-lg-2 col-md-4">
                <label class="fm-label text-slate-500 mb-2">Compliance</label>
                <select class="form-select fm-form-control border-slate-200 shadow-none" name="status">
                    <option value="">Any Compliance</option>
                    <option value="AVAILABLE" ${filter.status == 'AVAILABLE' ? 'selected' : ''}>Available</option>
                    <option value="EXPIRING" ${filter.status == 'EXPIRING' ? 'selected' : ''}>Expiring Soon</option>
                    <option value="EXPIRED" ${filter.status == 'EXPIRED' ? 'selected' : ''}>Expired</option>
                </select>
            </div>
            <div class="col-lg-2 col-md-6">
                <label class="fm-label text-slate-500 mb-2">Min Qty</label>
                <input type="number" class="fm-form-control border-slate-200 shadow-none" name="minQtyLeft" value="${filter.minQtyLeft}" placeholder="Qty >=">
            </div>
            <div class="col-lg-2 col-md-6">
                <div class="d-flex gap-2">
                    <button type="submit" class="fm-btn fm-btn-primary flex-grow-1 border-0 shadow-none py-2">Search</button>
                    <a href="${pageContext.request.contextPath}/staff/inventory" class="btn btn-white border-slate-200 text-slate-400 hvr-soft px-3 shadow-none py-2"><i class="bi bi-arrow-clockwise"></i></a>
                </div>
            </div>
        </form>
    </div>

    <!-- Ledger Table -->
    <div class="fm-surface overflow-hidden shadow-sm border-0">
        <table class="table table-hover fm-inventory-table mb-0">
            <thead>
                <tr>
                    <th style="width: 140px;">Lot reference</th>
                    <th>Product details</th>
                    <th>Source hub</th>
                    <th>Lifecycle</th>
                    <th class="text-end">Balance</th>
                    <th style="width: 180px;">Compliance status</th>
                    <th class="text-end" style="width: 80px;"></th>
                </tr>
            </thead>
            <tbody>
                <c:choose>
                    <c:when test="${empty filteredLots}">
                        <tr>
                            <td colspan="7" class="text-center py-12">
                                <div class="mb-4">
                                    <div class="bg-slate-50 rounded-circle d-inline-flex p-5 text-slate-200">
                                        <i class="bi bi-database-dash fs-1"></i>
                                    </div>
                                </div>
                                <h5 class="fw-extrabold text-slate-900 mb-2">No Records Found</h5>
                                <p class="text-slate-400 mb-4 mx-auto" style="max-width: 320px;">We couldn't find any batches matching those exact parameters.</p>
                                <a href="${pageContext.request.contextPath}/staff/inventory" class="fm-btn btn-light border-0 px-8 py-2 text-slate-500 hvr-soft">Clear All Filters</a>
                            </td>
                        </tr>
                    </c:when>
                    <c:otherwise>
                        <c:forEach items="${filteredLots}" var="lot">
                            <c:set var="isExp" value="${lot.expiryDate lt today}"/>
                            <c:set var="isCrit" value="${lot.qtyLeft > 0 && lot.expiryDate ge today && (lot.expiryDate.toEpochDay() - today.toEpochDay()) <= 7}"/>
                            <c:set var="isCons" value="${lot.qtyLeft == 0}"/>
                            <c:set var="isLow" value="${lot.qtyLeft > 0 && lot.qtyLeft <= lot.qtyIn * 0.15}"/>

                            <tr>
                                <td><span class="fm-batch-id">#ID-${lot.id}</span></td>
                                <td>
                                    <div class="fw-extrabold text-slate-900 mb-0"><c:out value="${lot.product.name}"/></div>
                                    <div class="fm-caption text-slate-400 font-monospace" style="font-size: 0.65rem;">CAT: ${lot.product.category}</div>
                                </td>
                                <td>
                                    <div class="small fw-bold text-slate-600"><c:out value="${lot.supplier != null ? lot.supplier.name : 'Unknown Hub'}"/></div>
                                    <div class="fm-caption text-slate-400" style="font-size: 0.65rem;">In: ${lot.importDate}</div>
                                </td>
                                <td>
                                    <div class="d-flex align-items-center gap-2">
                                        <div class="small fw-extrabold ${isExp ? 'text-danger' : (isCrit ? 'text-warning' : 'text-slate-900')}">
                                            <fmt:parseDate value="${lot.expiryDate}" pattern="yyyy-MM-dd" var="parsedDate"/>
                                            <fmt:formatDate value="${parsedDate}" pattern="MMM dd, yyyy"/>
                                        </div>
                                        <c:if test="${isCrit}"><span class="badge bg-warning-subtle text-warning border-0 p-1" title="Critical FEFO alert"><i class="bi bi-patch-exclamation"></i></span></c:if>
                                    </div>
                                    <div class="fm-caption text-slate-400" style="font-size: 0.65rem;">FEFO Lifespan</div>
                                </td>
                                <td class="text-end">
                                    <div class="fw-extrabold text-slate-900 fs-5">
                                        <fmt:formatNumber value="${lot.qtyLeft}" type="number"/>
                                    </div>
                                    <div class="fm-caption text-slate-400" style="font-size: 0.65rem;">Available / ${lot.qtyIn} ${lot.product.unit}s</div>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${isExp}">
                                            <span class="fm-status-badge expired border-0 px-3 shadow-none"><i class="status-dot expired"></i> VOID/EXPIRED</span>
                                        </c:when>
                                        <c:when test="${isCons}">
                                            <span class="fm-status-badge border-0 px-3 shadow-none" style="background:var(--fm-slate-100); color:var(--fm-slate-500);"><i class="status-dot consumed"></i> CONSUMED</span>
                                        </c:when>
                                        <c:when test="${isCrit}">
                                            <span class="fm-status-badge expiring border-0 px-3 shadow-none"><i class="status-dot expiring"></i> CRITICAL FEFO</span>
                                        </c:when>
                                        <c:when test="${isLow}">
                                            <span class="fm-status-badge border-0 px-3 shadow-none" style="background:#fff7ed; color:#c2410c;"><i class="status-dot" style="background:#f97316;"></i> LOW STOCK</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="fm-status-badge available border-0 px-3 shadow-none"><i class="status-dot available"></i> OPTIMIZED</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td class="text-end">
                                    <div class="dropdown">
                                        <button class="btn btn-white border-0 p-1 shadow-none hvr-soft transition" data-bs-toggle="dropdown"><i class="bi bi-three-dots-vertical fs-5 text-slate-400"></i></button>
                                        <ul class="dropdown-menu dropdown-menu-end border-0 shadow-lg py-2">
                                            <li><a class="dropdown-item py-2 fw-medium" href="${pageContext.request.contextPath}/staff/import-lot?id=${lot.id}"><i class="bi bi-pencil-square me-3 text-slate-400"></i> Edit Audit Logs</a></li>
                                            <c:if test="${isExp && lot.qtyLeft > 0}">
                                                <li><hr class="dropdown-divider opacity-50"></li>
                                                <li>
                                                    <form action="${pageContext.request.contextPath}/staff/delete-lot" method="post" onsubmit="return confirm('Confirm removal of expired stock?');">
                                                        <input type="hidden" name="lotId" value="${lot.id}">
                                                        <input type="hidden" name="csrf_token" value="${sessionScope.CSRF_TOKEN}">
                                                        <button type="submit" class="dropdown-item py-2 fw-medium text-danger"><i class="bi bi-trash3 me-3 opacity-70"></i> Mark for Disposal</button>
                                                    </form>
                                                </li>
                                            </c:if>
                                        </ul>
                                    </div>
                                </td>
                            </tr>
                        </c:forEach>
                    </c:otherwise>
                </c:choose>
            </tbody>
        </table>
    </div>

    <!-- Statistical Footer Detail (Visible when filtered) -->
    <c:if test="${selectedProduct != null}">
        <div class="fm-surface mt-6 p-6 border-0 shadow-sm">
            <h5 class="fm-h3 mb-4 d-flex align-items-center gap-2"><i class="bi bi-graph-up text-primary"></i> Detailed Lifecycle Summary</h5>
            <div class="row g-4">
                <div class="col-md-4">
                    <div class="p-4 bg-slate-50 rounded-4">
                        <div class="fm-caption fw-bold text-slate-400 mb-2">Aggregate Intake</div>
                        <div class="fm-h2 text-slate-900 mb-0">${stockSummary.totalIn} <span class="fs-6 fw-normal">${selectedProduct.unit}s</span></div>
                    </div>
                </div>
                <div class="col-md-4">
                    <div class="p-4 bg-primary-subtle rounded-4">
                        <div class="fm-caption fw-bold text-primary mb-2">Optimized Bio-Balance</div>
                        <div class="fm-h2 text-primary mb-0">${stockSummary.availableQty} <span class="fs-6 fw-normal">${selectedProduct.unit}s</span></div>
                    </div>
                </div>
                <div class="col-md-4">
                    <div class="p-4 bg-danger-subtle rounded-4">
                        <div class="fm-caption fw-bold text-danger mb-2">Total Waste Factor</div>
                        <div class="fm-h2 text-danger mb-0">${stockSummary.expiredQty} <span class="fs-6 fw-normal">${selectedProduct.unit}s</span></div>
                    </div>
                </div>
            </div>
        </div>
    </c:if>
</div>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>