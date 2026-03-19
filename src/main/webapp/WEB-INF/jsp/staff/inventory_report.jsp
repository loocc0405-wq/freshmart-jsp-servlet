<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
            <%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

                <c:set var="pageTitle" value="Inventory Performance Analytics" />
                <jsp:include page="/WEB-INF/jsp/common/header.jsp" />

                <c:url var="exportCsvUrl" value="/staff/inventory-report">
                    <c:param name="export" value="csv" />
                    <c:if test="${filter.productId != null}">
                        <c:param name="productId" value="${filter.productId}" />
                    </c:if>
                    <c:if test="${filter.supplierId != null}">
                        <c:param name="supplierId" value="${filter.supplierId}" />
                    </c:if>
                    <c:if test="${not empty filter.status}">
                        <c:param name="status" value="${filter.status}" />
                    </c:if>
                    <c:if test="${filter.importFrom != null}">
                        <c:param name="importFrom" value="${filter.importFrom}" />
                    </c:if>
                    <c:if test="${filter.importTo != null}">
                        <c:param name="importTo" value="${filter.importTo}" />
                    </c:if>
                    <c:if test="${filter.expiryFrom != null}">
                        <c:param name="expiryFrom" value="${filter.expiryFrom}" />
                    </c:if>
                    <c:if test="${filter.expiryTo != null}">
                        <c:param name="expiryTo" value="${filter.expiryTo}" />
                    </c:if>
                    <c:if test="${filter.minQtyLeft != null}">
                        <c:param name="minQtyLeft" value="${filter.minQtyLeft}" />
                    </c:if>
                    <c:if test="${filter.maxQtyLeft != null}">
                        <c:param name="maxQtyLeft" value="${filter.maxQtyLeft}" />
                    </c:if>
                </c:url>

                <div class="container-fluid px-4 py-4">
                    <div class="d-flex align-items-center justify-content-between mb-4">
                        <div>
                            <h1 class="fm-h1 mb-1">Inventory Performance</h1>
                            <p class="fm-text-secondary mb-0">
                                Comprehensive auditing, stock movements and disposal history for warehouse lots.
                            </p>
                        </div>
                        <div class="d-flex gap-2">
                            <button type="button" class="fm-btn btn-light border small" onclick="window.print()">
                                <i class="bi bi-printer me-2"></i>Print Ledger
                            </button>
                            <a href="${exportCsvUrl}"
                                class="fm-btn fm-btn-primary small">
                                <i class="bi bi-file-earmark-excel me-2"></i>Export CSV
                            </a>
                        </div>
                    </div>

                    <c:if test="${not empty successMessage}">
                        <div class="alert alert-success border-0 shadow-sm mb-4">
                            <i class="bi bi-check-circle me-2"></i>
                            <c:out value="${successMessage}" />
                        </div>
                    </c:if>

                    <c:if test="${not empty errorMessage}">
                        <div class="alert alert-danger border-0 shadow-sm mb-4">
                            <i class="bi bi-exclamation-triangle me-2"></i>
                            <c:out value="${errorMessage}" />
                        </div>
                    </c:if>

                    <div class="row g-4 mb-4">
                        <div class="col-md-6 col-xl-3">
                            <div class="fm-card border-start border-4 border-primary p-4 h-100">
                                <div class="fm-caption text-uppercase fw-bold opacity-75 mb-1">Stock Valuation</div>
                                <div class="fm-h2 mb-2 text-primary">${totalInventoryValue}</div>
                                <div class="small text-muted">
                                    <i class="bi bi-graph-up me-1 text-success"></i>
                                    Current value of usable on-hand stock
                                </div>
                            </div>
                        </div>

                        <div class="col-md-6 col-xl-3">
                            <div class="fm-card border-start border-4 border-info p-4 h-100">
                                <div class="fm-caption text-uppercase fw-bold opacity-75 mb-1">Operational Batches</div>
                                <div class="fm-h2 mb-2 text-info">${totalActiveLots}</div>
                                <div class="small text-muted">Across ${fn:length(allProductsOverview)} Product SKU Types
                                </div>
                            </div>
                        </div>

                        <div class="col-md-6 col-xl-3">
                            <div class="fm-card border-start border-4 border-warning p-4 h-100">
                                <div class="fm-caption text-uppercase fw-bold opacity-75 mb-1">Expiry Risk Stock</div>
                                <div class="fm-h2 mb-2 text-warning">${upcomingExpiryCount}</div>
                                <div class="small text-muted">
                                    <i class="bi bi-clock-history me-1"></i>
                                    Threshold: ${upcomingExpiryDays} Days Remaining
                                </div>
                            </div>
                        </div>

                        <div class="col-md-6 col-xl-3">
                            <div class="fm-card border-start border-4 border-danger p-4 h-100">
                                <div class="fm-caption text-uppercase fw-bold opacity-75 mb-1">Pending Disposal Lots
                                </div>
                                <div class="fm-h2 mb-2 text-danger">${expiredLotsCount}</div>
                                <div class="small text-muted">Expired lots still waiting for disposal action</div>
                            </div>
                        </div>
                    </div>

                    <div class="row g-4 mb-4">
                        <div class="col-md-6 col-xl-4">
                            <div class="fm-card border-start border-4 border-warning p-4 h-100">
                                <div class="fm-caption text-uppercase fw-bold opacity-75 mb-1">Near-expiry Value</div>
                                <div class="fm-h2 mb-2 text-warning">
                                    <fmt:formatNumber value="${nearExpiryValue}" type="number" maxFractionDigits="2" />
                                </div>
                                <div class="small text-muted">Capital tied in lots expiring within ${upcomingExpiryDays}
                                    days</div>
                            </div>
                        </div>

                        <div class="col-md-6 col-xl-4">
                            <div class="fm-card border-start border-4 border-danger p-4 h-100">
                                <div class="fm-caption text-uppercase fw-bold opacity-75 mb-1">Expired Stock Value</div>
                                <div class="fm-h2 mb-2 text-danger">
                                    <fmt:formatNumber value="${expiredValue}" type="number" maxFractionDigits="2" />
                                </div>
                                <div class="small text-muted">Estimated waste value still sitting in expired batches
                                </div>
                            </div>
                        </div>

                        <div class="col-md-6 col-xl-4">
                            <div class="fm-card border-start border-4 border-secondary p-4 h-100">
                                <div class="fm-caption text-uppercase fw-bold opacity-75 mb-1">Stagnant Inventory</div>
                                <div class="fm-h2 mb-2 text-secondary">${stagnantLotsCount}</div>
                                <div class="small text-muted">Lots older than ${stagnantLotDays} days · value <strong>
                                        <fmt:formatNumber value="${stagnantValue}" type="number"
                                            maxFractionDigits="2" />
                                    </strong></div>
                            </div>
                        </div>
                    </div>

                    <div class="fm-surface p-3 mb-4">
                        <form method="get" action="${pageContext.request.contextPath}/staff/inventory-report"
                            class="row g-2 align-items-end">
                            <div class="col-md-3">
                                <label class="fm-caption fw-bold mb-1 d-block">SKU FILTER</label>
                                <select class="fm-form-control py-2" name="productId" style="font-size: 0.85rem;">
                                    <option value="">All Active Products</option>
                                    <c:forEach items="${products}" var="p">
                                        <option value="${p.id}" ${filter.productId==p.id ? 'selected' : '' }>
                                            <c:out value="${p.name}" />
                                        </option>
                                    </c:forEach>
                                </select>
                            </div>

                            <div class="col-md-2">
                                <label class="fm-caption fw-bold mb-1 d-block">AUDIT STATUS</label>
                                <select class="fm-form-control py-2" name="status" style="font-size: 0.85rem;">
                                    <option value="">Full Audit</option>
                                    <option value="AVAILABLE" ${filter.status=='AVAILABLE' ? 'selected' : '' }>Available
                                    </option>
                                    <option value="EXPIRING" ${filter.status=='EXPIRING' ? 'selected' : '' }>Expiring
                                    </option>
                                    <option value="EXPIRED" ${filter.status=='EXPIRED' ? 'selected' : '' }>Expired
                                    </option>
                                </select>
                            </div>

                            <div class="col-md-2">
                                <label class="fm-caption fw-bold mb-1 d-block">MIN QTY</label>
                                <input type="number" name="minQtyLeft" class="fm-form-control py-2"
                                    style="font-size: 0.85rem;" value="${filter.minQtyLeft}">
                            </div>

                            <div class="col-md-3 d-flex gap-2">
                                <button type="submit" class="fm-btn fm-btn-primary py-2 px-3 flex-grow-1"
                                    style="font-size: 0.85rem;">
                                    Run Audit
                                </button>
                                <a href="${pageContext.request.contextPath}/staff/inventory-report"
                                    class="btn btn-light border py-2 px-3" style="font-size: 0.85rem;">
                                    <i class="bi bi-arrow-clockwise"></i>
                                </a>
                            </div>
                        </form>
                    </div>

                    <div class="fm-surface overflow-hidden">
                        <ul class="nav nav-tabs border-bottom-0 bg-light-subtle px-3 pt-3 gap-1" id="reportTabs"
                            role="tablist">
                            <li class="nav-item" role="presentation">
                                <button class="nav-link active fm-caption fw-bold border-0 py-3 px-4"
                                    data-bs-toggle="tab" data-bs-target="#tab-summary" type="button" role="tab">
                                    Operational Ledger
                                </button>
                            </li>
                            <li class="nav-item" role="presentation">
                                <button class="nav-link fm-caption fw-bold border-0 py-3 px-4" data-bs-toggle="tab"
                                    data-bs-target="#tab-lowstock" type="button" role="tab">
                                    Replenishment Required
                                </button>
                            </li>
                            <li class="nav-item" role="presentation">
                                <button class="nav-link fm-caption fw-bold border-0 py-3 px-4" data-bs-toggle="tab"
                                    data-bs-target="#tab-transactions" type="button" role="tab">
                                    Transaction History
                                </button>
                            </li>
                            <li class="nav-item" role="presentation">
                                <button class="nav-link fm-caption fw-bold border-0 py-3 px-4" data-bs-toggle="tab"
                                    data-bs-target="#tab-disposals" type="button" role="tab">
                                    Disposal History
                                </button>
                            </li>
                            <li class="nav-item text-danger" role="presentation">
                                <button class="nav-link fm-caption fw-bold border-0 py-3 px-4 text-danger"
                                    data-bs-toggle="tab" data-bs-target="#tab-expired" type="button" role="tab">
                                    Pending Disposal
                                </button>
                            </li>
                        </ul>

                        <div class="tab-content">
                            <div class="tab-pane fade show active" id="tab-summary" role="tabpanel">
                                <table class="fm-data-table">
                                    <thead class="bg-light">
                                        <tr>
                                            <th>SKU Master Name</th>
                                            <th class="text-end">Lifecycle Intake</th>
                                            <th class="text-end">Available Qty</th>
                                            <th class="text-end">Expired Qty</th>
                                            <th class="text-end">Fulfillment Rate</th>
                                            <th>Next Expiry Event</th>
                                            <th class="text-end">Inventory Value</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach items="${allProductsOverview}" var="o">
                                            <c:set var="fillPercent"
                                                value="${o.totalQtyIn > 0 ? (o.totalQtyConsumed / o.totalQtyIn) * 100 : 0}" />
                                            <tr>
                                                <td class="fw-bold text-primary">
                                                    <c:out value="${o.productName}" />
                                                </td>
                                                <td class="text-end small">${o.totalQtyIn}</td>
                                                <td class="text-end fw-bold text-success">${o.availableQty}</td>
                                                <td class="text-end fw-bold text-danger">${o.expiredQty}</td>
                                                <td class="text-end">
                                                    <div class="d-flex align-items-center justify-content-end gap-2">
                                                        <fmt:formatNumber value="${fillPercent}" maxFractionDigits="0"
                                                            var="fmtFillPercent" />
                                                        <div class="progress me-1" style="width: 50px; height: 4px;">
                                                            <div class="progress-bar bg-info" <c:out
                                                                value="style='width: ${fmtFillPercent}%'"
                                                                escapeXml="false" />>
                                                        </div>
                                                    </div>
                                                    <span class="small fw-semibold">${fmtFillPercent} %</span>
                            </div>
                            </td>
                            <td>
                                <c:choose>
                                    <c:when test="${o.nearestExpiry != null}">
                                        <span class="small opacity-75">
                                            <i class="bi bi-clock me-1"></i>${o.nearestExpiry}
                                        </span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="text-muted opacity-50">-</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td class="text-end fw-bold">${o.availableValue}</td>
                            </tr>
                            </c:forEach>
                            </tbody>
                            </table>
                        </div>

                        <div class="tab-pane fade" id="tab-lowstock" role="tabpanel">
                            <div class="p-5 text-center ${not empty lowStockProducts ? 'd-none' : ''}">
                                <i class="bi bi-check-circle fs-1 text-success opacity-25 mb-3"></i>
                                <h5 class="fw-bold">Inventory Levels Optimal</h5>
                                <p class="text-muted small">No SKUs currently fall below the replenishment threshold.
                                </p>
                            </div>

                            <c:if test="${not empty lowStockProducts}">
                                <table class="fm-data-table">
                                    <thead>
                                        <tr>
                                            <th>Restock Priority SKU</th>
                                            <th>Available Stock</th>
                                            <th>Threshold Delta</th>
                                            <th class="text-end">Actions</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach items="${lowStockProducts}" var="o">
                                            <tr class="bg-warning-subtle">
                                                <td class="fw-bold">
                                                    <c:out value="${o.productName}" />
                                                </td>
                                                <td class="fw-bold text-danger">${o.availableQty} Units</td>
                                                <td class="small">Below safe threshold (${lowStockThreshold})</td>
                                                <td class="text-end">
                                                    <a href="${pageContext.request.contextPath}/staff/import-lot?productId=${o.productId}"
                                                        class="fm-btn fm-btn-primary py-1 px-3 small"
                                                        style="font-size: 0.75rem;">
                                                        Initialize Replenishment
                                                    </a>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </tbody>
                                </table>
                            </c:if>
                        </div>

                        <div class="tab-pane fade" id="tab-transactions" role="tabpanel">
                            <c:if test="${empty recentTransactions}">
                                <div class="p-5 text-center">
                                    <i class="bi bi-clock-history fs-1 text-muted opacity-25 mb-3"></i>
                                    <h5 class="fw-bold">No Inventory Transactions Yet</h5>
                                    <p class="text-muted small">
                                        Once lots are imported, sold, adjusted or disposed, the audit trail will appear
                                        here.
                                    </p>
                                </div>
                            </c:if>

                            <c:if test="${not empty recentTransactions}">
                                <table class="fm-data-table">
                                    <thead>
                                        <tr>
                                            <th>Time</th>
                                            <th>Type</th>
                                            <th>Product</th>
                                            <th>Lot</th>
                                            <th class="text-end">Qty</th>
                                            <th>Reference</th>
                                            <th>Actor</th>
                                            <th>Note</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach items="${recentTransactions}" var="tx">
                                            <tr>
                                                <td class="small text-nowrap">${tx.createdAt}</td>
                                                <td>
                                                    <span
                                                        class="badge ${tx.type == 'IMPORT' ? 'text-bg-primary' : tx.type == 'SALE' ? 'text-bg-success' : tx.type == 'ADJUST' ? 'text-bg-warning' : tx.type == 'DISPOSE' ? 'text-bg-danger' : 'text-bg-secondary'}">
                                                        ${tx.type}
                                                    </span>
                                                </td>
                                                <td>
                                                    <div class="fw-semibold">
                                                        <c:out value="${tx.productLot.product.name}" />
                                                    </div>
                                                    <div class="small text-muted">
                                                        <c:out
                                                            value="${tx.productLot.supplier != null ? tx.productLot.supplier.name : 'No supplier'}" />
                                                    </div>
                                                </td>
                                                <td class="small">#LOT-${tx.productLot.id}</td>
                                                <td
                                                    class="text-end fw-bold ${tx.quantity >= 0 ? 'text-success' : 'text-danger'}">
                                                    ${tx.quantity}
                                                </td>
                                                <td class="small">
                                                    <c:choose>
                                                        <c:when test="${not empty tx.referenceType}">
                                                            <span class="fw-semibold">
                                                                <c:out value="${tx.referenceType}" />
                                                            </span>
                                                            <c:if test="${tx.referenceId != null}">#${tx.referenceId}
                                                            </c:if>
                                                        </c:when>
                                                        <c:otherwise>-</c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td class="small">
                                                    <c:choose>
                                                        <c:when test="${tx.createdBy != null}">
                                                            <c:out
                                                                value="${not empty tx.createdBy.fullName ? tx.createdBy.fullName : tx.createdBy.username}" />
                                                        </c:when>
                                                        <c:otherwise>System</c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td class="small text-muted">
                                                    <c:out value="${tx.note}" />
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </tbody>
                                </table>
                            </c:if>
                        </div>

                        <div class="tab-pane fade" id="tab-disposals" role="tabpanel">
                            <c:if test="${empty recentDisposals}">
                                <div class="p-5 text-center">
                                    <i class="bi bi-trash3 fs-1 text-muted opacity-25 mb-3"></i>
                                    <h5 class="fw-bold">No Disposal Records Yet</h5>
                                    <p class="text-muted small">
                                        Disposed lots will remain visible here for audit instead of being hard deleted.
                                    </p>
                                </div>
                            </c:if>

                            <c:if test="${not empty recentDisposals}">
                                <table class="fm-data-table">
                                    <thead>
                                        <tr>
                                            <th>Disposed At</th>
                                            <th>Product</th>
                                            <th>Lot</th>
                                            <th class="text-end">Disposed Qty</th>
                                            <th>Reason</th>
                                            <th>Performed By</th>
                                            <th>Note</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach items="${recentDisposals}" var="d">
                                            <tr>
                                                <td class="small text-nowrap">${d.disposedAt}</td>
                                                <td>
                                                    <div class="fw-semibold">
                                                        <c:out value="${d.productLot.product.name}" />
                                                    </div>
                                                    <div class="small text-muted">#LOT-${d.productLot.id}</div>
                                                </td>
                                                <td class="small">Import ${d.productLot.importDate} · Exp
                                                    ${d.productLot.expiryDate}</td>
                                                <td class="text-end fw-bold text-danger">${d.disposedQty}</td>
                                                <td class="small">
                                                    <c:out value="${d.reason}" />
                                                </td>
                                                <td class="small">
                                                    <c:choose>
                                                        <c:when test="${d.disposedBy != null}">
                                                            <c:out
                                                                value="${not empty d.disposedBy.fullName ? d.disposedBy.fullName : d.disposedBy.username}" />
                                                        </c:when>
                                                        <c:otherwise>System</c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td class="small text-muted">
                                                    <c:out value="${d.note}" />
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </tbody>
                                </table>
                            </c:if>
                        </div>

                        <div class="tab-pane fade" id="tab-expired" role="tabpanel">
                            <c:if test="${empty expiredLots}">
                                <div class="p-5 text-center">
                                    <i class="bi bi-shield-check fs-1 text-success opacity-25 mb-3"></i>
                                    <h5 class="fw-bold">Zero Pending Disposal</h5>
                                    <p class="text-muted small">No expired batches currently detected in active storage.
                                    </p>
                                </div>
                            </c:if>

                            <c:if test="${not empty expiredLots}">
                                <table class="fm-data-table">
                                    <thead>
                                        <tr>
                                            <th>Expired Batch SKU</th>
                                            <th>Lot ID</th>
                                            <th>Expiry Event Date</th>
                                            <th>Qty Remaining</th>
                                            <th class="text-end">Disposal Action</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach items="${expiredLots}" var="lot">
                                            <tr>
                                                <td>
                                                    <c:out value="${lot.product.name}" />
                                                </td>
                                                <td><span class="font-monospace text-muted small">#LOT-${lot.id}</span>
                                                </td>
                                                <td class="fw-bold text-danger">${lot.expiryDate}</td>
                                                <td class="fw-bold">${lot.qtyLeft}</td>
                                                <td class="text-end">
                                                    <a href="${pageContext.request.contextPath}/staff/lot-disposals/new?lotId=${lot.id}&redirect=/staff/inventory-report"
                                                        class="fm-btn btn-danger py-1 px-3 small"
                                                        style="font-size: 0.75rem;">
                                                        Open Disposal Form
                                                    </a>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </tbody>
                                </table>
                            </c:if>
                        </div>
                    </div>
                </div>
                </div>

                <jsp:include page="/WEB-INF/jsp/common/footer.jsp" />