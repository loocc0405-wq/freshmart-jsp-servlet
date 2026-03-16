<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="pageTitle" value="Inventory Performance Analytics"/>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"/>

<div class="container-fluid px-4 py-4">
    <!-- Header -->
    <div class="d-flex align-items-center justify-content-between mb-4">
        <div>
            <h1 class="fm-h1 mb-1">Inventory Performance</h1>
            <p class="fm-text-secondary mb-0">Comprehensive auditing and financial valuation of active warehouse stock.</p>
        </div>
        <div class="d-flex gap-2">
            <button class="fm-btn btn-light border small"><i class="bi bi-printer me-2"></i>Print Ledger</button>
            <button class="fm-btn fm-btn-primary small"><i class="bi bi-file-earmark-excel me-2"></i>Export CSV</button>
        </div>
    </div>

    <!-- Multi-metric KPI Grid -->
    <div class="row g-4 mb-4">
        <div class="col-md-6 col-xl-3">
            <div class="fm-card border-start border-4 border-primary p-4 h-100">
                <div class="fm-caption text-uppercase fw-bold opacity-75 mb-1">Stock Valuation</div>
                <div class="fm-h2 mb-2 text-primary">${totalInventoryValue}</div>
                <div class="small text-muted"><i class="bi bi-graph-up me-1 text-success"></i> +2.4% vs last week</div>
            </div>
        </div>
        <div class="col-md-6 col-xl-3">
            <div class="fm-card border-start border-4 border-info p-4 h-100">
                <div class="fm-caption text-uppercase fw-bold opacity-75 mb-1">Operational Batches</div>
                <div class="fm-h2 mb-2 text-info">${totalActiveLots}</div>
                <div class="small text-muted">Across ${allProductsOverview.size()} Product SKU Types</div>
            </div>
        </div>
        <div class="col-md-6 col-xl-3">
            <div class="fm-card border-start border-4 border-warning p-4 h-100">
                <div class="fm-caption text-uppercase fw-bold opacity-75 mb-1">Expiry Risk Stock</div>
                <div class="fm-h2 mb-2 text-warning">${upcomingExpiryCount}</div>
                <div class="small text-muted"><i class="bi bi-clock-history me-1"></i> Threshold: 7 Days Remaining</div>
            </div>
        </div>
        <div class="col-md-6 col-xl-3">
            <div class="fm-card border-start border-4 border-danger p-4 h-100">
                <div class="fm-caption text-uppercase fw-bold opacity-75 mb-1">Void Stock (Expired)</div>
                <div class="fm-h2 mb-2 text-danger">${expiredLotsCount}</div>
                <div class="small text-muted">Awaiting Disposal Protocol</div>
            </div>
        </div>
    </div>

    <!-- Advanced Filter Bar -->
    <div class="fm-surface p-3 mb-4">
        <form method="get" action="${pageContext.request.contextPath}/staff/inventory-report" class="row g-2 align-items-end">
            <div class="col-md-3">
                <label class="fm-caption fw-bold mb-1 d-block">SKU FILTER</label>
                <select class="fm-form-control py-2" name="productId" style="font-size: 0.85rem;">
                    <option value="">All Active Products</option>
                    <c:forEach items="${products}" var="p">
                        <option value="${p.id}" ${filter.productId == p.id ? 'selected' : ''}><c:out value="${p.name}"/></option>
                    </c:forEach>
                </select>
            </div>
            <div class="col-md-2">
                <label class="fm-caption fw-bold mb-1 d-block">AUDIT STATUS</label>
                <select class="fm-form-control py-2" name="status" style="font-size: 0.85rem;">
                    <option value="">Full Audit</option>
                    <option value="AVAILABLE" ${filter.status == 'AVAILABLE' ? 'selected' : ''}>Available</option>
                    <option value="EXPIRING" ${filter.status == 'EXPIRING' ? 'selected' : ''}>Expiring</option>
                </select>
            </div>
            <div class="col-md-2">
                <label class="fm-caption fw-bold mb-1 d-block">MIN QTY</label>
                <input type="number" name="minQtyLeft" class="fm-form-control py-2" style="font-size: 0.85rem;" value="${filter.minQtyLeft}">
            </div>
            <div class="col-md-3 d-flex gap-2">
                <button type="submit" class="fm-btn fm-btn-primary py-2 px-3 flex-grow-1" style="font-size: 0.85rem;">Run Audit</button>
                <a href="${pageContext.request.contextPath}/staff/inventory-report" class="btn btn-light border py-2 px-3" style="font-size: 0.85rem;"><i class="bi bi-arrow-clockwise"></i></a>
            </div>
        </form>
    </div>

    <!-- Data Presentation Tabs -->
    <div class="fm-surface overflow-hidden">
        <ul class="nav nav-tabs border-bottom-0 bg-light-subtle px-3 pt-3 gap-1" id="reportTabs">
            <li class="nav-item">
                <button class="nav-link active fm-caption fw-bold border-0 py-3 px-4" data-bs-toggle="tab" data-bs-target="#tab-summary">Operational Ledger</button>
            </li>
            <li class="nav-item">
                <button class="nav-link fm-caption fw-bold border-0 py-3 px-4" data-bs-toggle="tab" data-bs-target="#tab-lowstock">Replenishment required</button>
            </li>
            <li class="nav-item text-danger">
                <button class="nav-link fm-caption fw-bold border-0 py-3 px-4 text-danger" data-bs-toggle="tab" data-bs-target="#tab-expired">Disposal Ledger</button>
            </li>
        </ul>
        
        <div class="tab-content">
            <!-- TAB: ALL PRODUCTS OVERVIEW -->
            <div class="tab-pane fade show active" id="tab-summary">
                <table class="fm-data-table">
                    <thead class="bg-light">
                        <tr>
                            <th>SKU Master Name</th>
                            <th class="text-end">Lifecycle Intake</th>
                            <th class="text-end">Bio-Available</th>
                            <th class="text-end">Void (Expired)</th>
                            <th class="text-end">Fulfillment Rate</th>
                            <th>Next Expiry Event</th>
                            <th class="text-end">Inventory Value</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach items="${allProductsOverview}" var="o">
                            <c:set var="fillPercent" value="${o.totalQtyIn > 0 ? (o.totalQtyConsumed / o.totalQtyIn) * 100 : 0}"/>
                            <tr>
                                <td class="fw-bold text-primary"><c:out value="${o.productName}"/></td>
                                <td class="text-end small">${o.totalQtyIn}</td>
                                <td class="text-end fw-bold text-success">${o.availableQty}</td>
                                <td class="text-end fw-bold text-danger">${o.expiredQty}</td>
                                <td class="text-end">
                                    <div class="d-flex align-items-center justify-content-end gap-2">
                                        <div class="progress me-1" style="width: 50px; height: 4px;">
                                            <div class="progress-bar bg-info" style="width: ${fillPercent}%"></div>
                                        </div>
                                        <span class="small fw-semibold">${fn:substringBefore(fillPercent, ".")} %</span>
                                    </div>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${o.nearestExpiry != null}">
                                            <span class="small opacity-75"><i class="bi bi-clock me-1"></i> ${o.nearestExpiry}</span>
                                        </c:when>
                                        <c:otherwise><span class="text-muted opacity-50">-</span></c:otherwise>
                                    </c:choose>
                                </td>
                                <td class="text-end fw-bold">${o.availableValue}</td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>

            <!-- TAB: LOW STOCK -->
            <div class="tab-pane fade" id="tab-lowstock">
                <div class="p-5 text-center ${not empty lowStockProducts ? 'd-none' : ''}">
                    <i class="bi bi-check-circle fs-1 text-success opacity-25 mb-3"></i>
                    <h5 class="fw-bold">Inventory Levels Optimal</h5>
                    <p class="text-muted small">No SKUs currently fall below the replenishment threshold.</p>
                </div>
                <c:if test="${not empty lowStockProducts}">
                    <table class="fm-data-table">
                        <thead>
                            <tr>
                                <th>Restock Priority SKU</th>
                                <th>Available Bio-Stock</th>
                                <th>Threshold Delta</th>
                                <th class="text-end">Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach items="${lowStockProducts}" var="o">
                                <tr class="bg-warning-subtle">
                                    <td class="fw-bold"><c:out value="${o.productName}"/></td>
                                    <td class="fw-bold text-danger">${o.availableQty} Units</td>
                                    <td class="small">Below safe threshold</td>
                                    <td class="text-end">
                                        <a href="${pageContext.request.contextPath}/staff/import-lot?productId=${o.productId}" class="fm-btn fm-btn-primary py-1 px-3 small" style="font-size: 0.75rem;">Initialize Replenishment</a>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </c:if>
            </div>

            <!-- TAB: EXPIRED LEDGER -->
            <div class="tab-pane fade" id="tab-expired">
                <c:if test="${empty expiredLots}">
                    <div class="p-5 text-center">
                        <i class="bi bi-shield-check fs-1 text-success opacity-25 mb-3"></i>
                        <h5 class="fw-bold">Zero Void Inventory</h5>
                        <p class="text-muted small">No expired batches currently detected in active storage.</p>
                    </div>
                </c:if>
                <c:if test="${not empty expiredLots}">
                    <table class="fm-data-table">
                        <thead>
                            <tr>
                                <th>Void Batch SKU</th>
                                <th>Target ID</th>
                                <th>Expiry Event Date</th>
                                <th>Volume remaining</th>
                                <th class="text-end">Disposal Audit</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach items="${expiredLots}" var="lot">
                                <tr>
                                    <td><c:out value="${lot.product.name}"/></td>
                                    <td><span class="font-monospace text-muted small">#LOT-${lot.id}</span></td>
                                    <td class="fw-bold text-danger">${lot.expiryDate}</td>
                                    <td class="fw-bold">${lot.qtyLeft}</td>
                                    <td class="text-end">
                                        <form action="${pageContext.request.contextPath}/staff/delete-lot" method="post" onsubmit="return confirm('Authorize physical disposal of batch #${lot.id}?');">
                                            <input type="hidden" name="lotId" value="${lot.id}">
                                            <input type="hidden" name="csrf_token" value="${sessionScope.CSRF_TOKEN}">
                                            <button type="submit" class="fm-btn btn-danger py-1 px-3 small" style="font-size: 0.75rem;">Authorize Disposal</button>
                                        </form>
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

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
