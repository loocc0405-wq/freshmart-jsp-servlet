<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<c:set var="pageTitle" value="Replenishment Ops | FreshMart Enterprise"/>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"/>

<div class="container-fluid px-4 py-4">
    <!-- Header Strategy -->
    <div class="fm-page-header mb-5 border-bottom pb-4">
        <div class="d-flex align-items-center gap-3">
            <div class="bg-amber-500 text-white rounded-4 p-3 shadow-lg">
                <i class="bi bi-repeat fs-2"></i>
            </div>
            <div>
                <div class="fm-caption fw-bold text-primary mb-1 text-uppercase ls-wide">Supply Chain Resilience</div>
                <h1 class="fm-page-title">Algorithmic Replenishment Analyzer</h1>
                <p class="fm-page-subtitle">Advanced calculations for reorder points, safety stock, and lead-time optimization.</p>
            </div>
        </div>
    </div>

    <!-- Algorithm Tuning & Theory -->
    <div class="row g-4 mb-5">
        <div class="col-xl-4">
            <div class="fm-surface p-4 shadow-sm border-0 h-100">
                <h5 class="fm-h3 mb-4 text-dark"><i class="bi bi-sliders me-2 text-primary"></i> Logistics Parametrics</h5>
                <form method="get" action="${pageContext.request.contextPath}/pro/replenishment" class="vstack gap-4">
                    <div>
                        <label class="fm-caption fw-bold text-muted d-block mb-2">Observation Window (Days)</label>
                        <input class="fm-form-control" type="number" name="history" min="7" max="365" value="${daysHistory}"/>
                    </div>
                    <div class="row g-3">
                        <div class="col-6">
                            <label class="fm-caption fw-bold text-muted d-block mb-2">Lead Time</label>
                            <input class="fm-form-control" type="number" name="lead" min="0" max="60" value="${leadTimeDays}"/>
                        </div>
                        <div class="col-6">
                            <label class="fm-caption fw-bold text-muted d-block mb-2">Buffer Days</label>
                            <input class="fm-form-control" type="number" name="buffer" min="0" max="60" value="${bufferDays}"/>
                        </div>
                    </div>
                    <div>
                        <label class="fm-caption fw-bold text-muted d-block mb-2">Safety Stock Coefficient</label>
                        <input class="fm-form-control" type="number" name="safety" min="0" max="60" value="${safetyDays}"/>
                    </div>
                    <button class="fm-btn fm-btn-primary w-100 py-3 fw-bold" type="submit">Recalculate Constraints</button>
                </form>
            </div>
        </div>

        <div class="col-xl-8">
            <div class="fm-surface p-4 shadow-sm border-0 bg-slate-900 text-white h-100">
                <h5 class="fm-h3 mb-4 text-white">Mathematical Fulfillment Model</h5>
                <div class="row g-4">
                    <div class="col-md-6">
                        <div class="p-3 rounded-4 bg-slate-800 border-0 mb-3">
                            <div class="fm-caption fw-bold text-primary mb-2 text-uppercase">Expected Demand</div>
                            <div class="font-monospace small opacity-75">forecastPerDay × (leadTimeDays + bufferDays)</div>
                        </div>
                        <div class="p-3 rounded-4 bg-slate-800 border-0">
                            <div class="fm-caption fw-bold text-primary mb-2 text-uppercase">Safety Stock</div>
                            <div class="font-monospace small opacity-75">forecastPerDay × safetyDays</div>
                        </div>
                    </div>
                    <div class="col-md-6">
                        <div class="p-3 rounded-4 bg-slate-800 border-0 mb-3">
                            <div class="fm-caption fw-bold text-indigo-400 mb-2 text-uppercase">Reorder Point</div>
                            <div class="font-monospace small opacity-75">expectedDemand + safetyStock</div>
                        </div>
                        <div class="p-3 rounded-4 bg-primary text-white border-0">
                            <div class="fm-caption fw-bold opacity-75 mb-2 text-uppercase text-white">Suggested Qty</div>
                            <div class="font-monospace small">max(0, ceil(reorderPoint − stock))</div>
                        </div>
                    </div>
                </div>
                <div class="mt-4 p-3 border border-white border-opacity-10 rounded-4">
                    <div class="d-flex align-items-center gap-2 mb-2"><i class="bi bi-info-circle text-primary"></i> <span class="fw-bold">Dynamic Lead Time:</span> System prioritizes supplier-specific historical data when available.</div>
                    <div class="d-flex align-items-center gap-2"><i class="bi bi-exclamation-triangle text-amber-500"></i> <span class="fw-bold">Risk Management:</span> Suggestions are throttled for products with significant near-expiry lots (≤ 3d).</div>
                </div>
            </div>
        </div>
    </div>

    <!-- Detailed Ledger -->
    <div class="fm-surface p-0 shadow-sm border-0 overflow-hidden">
        <div class="p-4 border-bottom d-flex justify-content-between align-items-center bg-light">
            <h5 class="fm-h3 mb-0 text-dark">Replenishment Audit Ledger</h5>
        </div>
        <div class="table-responsive">
            <table class="table fm-data-table align-middle mb-0">
                <thead class="bg-white">
                    <tr>
                        <th class="ps-4">SKU Identity</th>
                        <th class="text-end">Fcst/Day</th>
                        <th class="text-end">Stock</th>
                        <th class="text-end">ROP</th>
                        <th class="text-center">Suggest</th>
                        <th>Preferred Partner</th>
                        <th class="text-end pe-4">Thao tác</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="r" items="${rows}">
                        <tr class="${r.suggestedQty > 0 ? 'bg-amber-50' : ''}">
                            <td class="ps-4">
                                <div class="fw-bold text-dark">${r.productName}</div>
                                <div class="small text-muted">Season Factor: <fmt:formatNumber value="${r.seasonFactor}" minFractionDigits="2"/></div>
                            </td>
                            <td class="text-end font-monospace"><fmt:formatNumber value="${r.forecastPerDay}" minFractionDigits="2"/></td>
                            <td class="text-end font-monospace">${r.stock}</td>
                            <td class="text-end font-monospace text-indigo-600 fw-bold"><fmt:formatNumber value="${r.reorderPoint}" minFractionDigits="1"/></td>
                            <td class="text-center">
                                <c:choose>
                                    <c:when test="${r.suggestedQty > 0}">
                                        <span class="badge bg-warning text-dark border-0 px-3 fw-bold">+${r.suggestedQty} Units</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="badge bg-success-subtle text-success border-0 px-3">STABLE</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td>
                                <c:if test="${not empty r.recommendedSupplierName}">
                                    <div class="fw-medium">${r.recommendedSupplierName}</div>
                                    <div class="small opacity-50">LT: ${r.recommendedSupplierLeadTimeDays}d | ₫<fmt:formatNumber value="${r.recommendedSupplierAvgImportPrice}" groupingUsed="true"/></div>
                                </c:if>
                            </td>
                            <td class="text-end pe-4">
                                <c:if test="${r.suggestedQty > 0 and not empty r.recommendedSupplierId}">
                                    <a class="fm-btn fm-btn-primary btn-sm px-3" href="${pageContext.request.contextPath}/staff/import-lot?productId=${r.productId}&supplierId=${r.recommendedSupplierId}">
                                        Initialize Import
                                    </a>
                                </c:if>
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty rows}">
                        <tr>
                            <td colspan="7" class="p-5 text-center text-muted">
                                <i class="bi bi-clipboard2-minus fs-1 opacity-10 mb-2 d-block"></i> No replenishment signals generated for the current window.
                            </td>
                        </tr>
                    </c:if>
                </tbody>
            </table>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>