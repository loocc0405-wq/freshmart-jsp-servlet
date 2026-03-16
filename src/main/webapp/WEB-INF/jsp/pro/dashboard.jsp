<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:set var="pageTitle" value="PRO Intelligence Hub | FreshMart Enterprise"/>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"/>

<style>
    /* PRO Specific Dashboard Overrides */
    :root {
        --fm-pro-gradient: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%);
    }
    .fm-pro-card {
        background: #fff;
        border: 0;
        border-radius: 20px;
        box-shadow: 0 10px 25px rgba(0,0,0,0.05);
        transition: transform 0.3s ease, box-shadow 0.3s ease;
    }
    .fm-pro-card:hover {
        transform: translateY(-5px);
        box-shadow: 0 15px 35px rgba(0,0,0,0.1);
    }
    .fm-nav-pills .nav-link {
        border-radius: 12px;
        padding: 12px 24px;
        font-weight: 600;
        color: var(--fm-slate-500);
        transition: all 0.2s;
    }
    .fm-nav-pills .nav-link.active {
        background: var(--fm-primary-600);
        color: #fff;
        box-shadow: 0 4px 12px rgba(37, 99, 235, 0.2);
    }
    .fm-kpi-pro-icon {
        width: 48px;
        height: 48px;
        border-radius: 14px;
        display: flex;
        align-items: center;
        justify-content: center;
        background: var(--fm-slate-100);
        color: var(--fm-primary-600);
        font-size: 1.5rem;
    }
    .chart-container-pro {
        position: relative;
        height: 350px;
        width: 100%;
    }
</style>

<div class="container-fluid px-4 py-4">
    <!-- Header Strategy -->
    <div class="fm-page-header mb-5 border-bottom pb-4">
        <div class="d-flex align-items-center gap-3">
            <div class="bg-indigo-600 text-white rounded-4 p-3 shadow-lg">
                <i class="bi bi-cpu fs-2"></i>
            </div>
            <div>
                <div class="fm-caption fw-bold text-primary mb-1 text-uppercase ls-wide">Executive Intelligence</div>
                <h1 class="fm-page-title">PRO Intelligence Hub</h1>
                <p class="fm-page-subtitle">Algorithmic forecasting, seasonality auditing, and automated replenishment logic.</p>
            </div>
        </div>
    </div>

    <!-- Tier Navigation -->
    <ul class="nav fm-nav-pills gap-2 mb-5">
        <li class="nav-item">
            <a class="nav-link ${tab == 'forecast' or empty tab ? 'active' : ''}" href="${pageContext.request.contextPath}/pro/dashboard?tab=forecast">
                <i class="bi bi-graph-up-arrow me-2"></i> 9.1 Forecast Engine
            </a>
        </li>
        <li class="nav-item">
            <a class="nav-link ${tab == 'seasonality' ? 'active' : ''}" href="${pageContext.request.contextPath}/pro/dashboard?tab=seasonality">
                <i class="bi bi-calendar3-range me-2"></i> 9.2 Seasonality Audit
            </a>
        </li>
        <li class="nav-item">
            <a class="nav-link ${tab == 'replenishment' ? 'active' : ''}" href="${pageContext.request.contextPath}/pro/dashboard?tab=replenishment">
                <i class="bi bi-repeat me-2"></i> 9.3 Replenishment Logic
            </a>
        </li>
    </ul>

    <c:choose>
        <%-- FORECAST ENGINE --%>
        <c:when test="${tab == 'forecast' or empty tab}">
            <div class="row g-4 mb-5">
                <!-- Forecast KPIs -->
                <div class="col-md-3">
                    <div class="fm-pro-card p-4 h-100">
                        <div class="d-flex justify-content-between mb-3">
                            <div class="fm-kpi-pro-icon bg-primary-subtle text-primary"><i class="bi bi-lightning-charge"></i></div>
                            <span class="badge bg-slate-100 text-slate-500 rounded-pill h-100 py-2 px-3 fw-bold">${method}</span>
                        </div>
                        <h6 class="fm-caption fw-bold text-muted mb-1">Target Horizon</h6>
                        <div class="fm-h2 mb-0">${horizon} <span class="fs-6 opacity-50">Periods</span></div>
                    </div>
                </div>
                <div class="col-md-3">
                    <div class="fm-pro-card p-4 h-100">
                        <div class="d-flex justify-content-between mb-3">
                            <div class="fm-kpi-pro-icon bg-success-subtle text-success"><i class="bi bi-currency-dollar"></i></div>
                            <span class="badge bg-success-subtle text-success rounded-pill h-100 py-2 px-3 fw-bold">Actual</span>
                        </div>
                        <h6 class="fm-caption fw-bold text-muted mb-1">Latest Actual</h6>
                        <div class="fm-h2 mb-0"><fmt:formatNumber value="${latestActual}" type="number"/></div>
                    </div>
                </div>
                <div class="col-md-3">
                    <div class="fm-pro-card p-4 h-100">
                        <div class="d-flex justify-content-between mb-3">
                            <div class="fm-kpi-pro-icon bg-indigo-subtle text-indigo-600"><i class="bi bi-magic"></i></div>
                            <span class="badge bg-indigo-subtle text-indigo-600 rounded-pill h-100 py-2 px-3 fw-bold">Forecast</span>
                        </div>
                        <h6 class="fm-caption fw-bold text-muted mb-1">Confidence Target</h6>
                        <div class="fm-h2 mb-0"><fmt:formatNumber value="${latestForecast}" type="number"/></div>
                    </div>
                </div>
                <div class="col-md-3">
                    <div class="fm-pro-card p-4 h-100">
                        <div class="d-flex justify-content-between mb-3">
                            <div class="fm-kpi-pro-icon bg-amber-subtle text-amber-600"><i class="bi bi-clock-history"></i></div>
                            <span class="badge bg-slate-100 text-slate-500 rounded-pill h-100 py-2 px-3 fw-bold">Audit</span>
                        </div>
                        <h6 class="fm-caption fw-bold text-muted mb-1">History Depth</h6>
                        <div class="fm-h2 mb-0">${history} <span class="fs-6 opacity-50">Periods</span></div>
                    </div>
                </div>
            </div>

            <div class="row g-4">
                <div class="col-lg-8">
                    <!-- Main Visualization -->
                    <div class="fm-surface p-4 shadow-sm border-0 mb-4">
                        <div class="d-flex justify-content-between align-items-center mb-4">
                            <h5 class="fm-h3 mb-0">Forecast Visualization</h5>
                            <a class="fm-btn btn-light border btn-sm" href="?tab=forecast&export=csv">
                                <i class="bi bi-download me-1"></i> Export Dataset
                            </a>
                        </div>
                        <div class="chart-container-pro">
                            <canvas id="forecastChart"></canvas>
                        </div>
                    </div>

                    <!-- Data Ledger -->
                    <div class="fm-surface p-0 shadow-sm border-0 overflow-hidden">
                        <div class="p-4 border-bottom bg-light">
                            <h5 class="fm-h3 mb-0">Forecast Audit Ledger</h5>
                        </div>
                        <div class="table-responsive" style="max-height: 400px;">
                            <table class="table fm-data-table align-middle mb-0">
                                <thead class="bg-white sticky-top">
                                    <tr>
                                        <th class="ps-4">Period Identification</th>
                                        <th class="text-end">Actual Revenue</th>
                                        <th class="text-end pe-4">Forecast Projection</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="b" items="${forecastBuckets}">
                                        <tr class="${b.forecast != null && b.actual == null ? 'bg-indigo-50 fw-bold' : ''}">
                                            <td class="ps-4">${b.label}</td>
                                            <td class="text-end font-monospace">
                                                <fmt:formatNumber value="${b.actual}" type="number"/>
                                            </td>
                                            <td class="text-end pe-4 font-monospace text-primary">
                                                <fmt:formatNumber value="${b.forecast}" type="number"/>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>

                <div class="col-lg-4">
                    <!-- Control Panel -->
                    <div class="fm-surface p-4 shadow-sm border-0 sticky-top" style="top: 1rem;">
                        <h5 class="fm-h3 mb-4 text-dark"><i class="bi bi-sliders me-2 text-primary"></i> Algorithm Parametrics</h5>
                        <form method="get" class="vstack gap-4">
                            <input type="hidden" name="tab" value="forecast"/>
                            
                            <div>
                                <label class="fm-caption fw-bold text-muted d-block mb-2">Temporal Granularity</label>
                                <select class="fm-form-control" name="granularity">
                                    <option value="day" ${granularity == 'day' ? 'selected' : ''}>Day</option>
                                    <option value="month" ${granularity == 'month' ? 'selected' : ''}>Month</option>
                                </select>
                            </div>

                            <div>
                                <label class="fm-caption fw-bold text-muted d-block mb-2">Mathematical Model</label>
                                <select class="fm-form-control" name="method">
                                    <option value="ma" ${method == 'ma' ? 'selected' : ''}>Moving Average (Stabilized)</option>
                                    <option value="es" ${method == 'es' ? 'selected' : ''}>Exponential Smoothing (Responsive)</option>
                                </select>
                            </div>

                            <div class="row g-3">
                                <div class="col-6">
                                    <label class="fm-caption fw-bold text-muted d-block mb-2">History</label>
                                    <input class="fm-form-control" type="number" name="history" value="${history}" min="3"/>
                                </div>
                                <div class="col-6">
                                    <label class="fm-caption fw-bold text-muted d-block mb-2">Horizon</label>
                                    <input class="fm-form-control" type="number" name="horizon" value="${horizon}" min="1"/>
                                </div>
                            </div>

                            <button class="fm-btn fm-btn-primary w-100 py-3 fw-bold" type="submit">Recalculate Projections</button>
                        </form>

                        <div class="p-3 bg-light rounded-4 mt-5 border-0">
                            <div class="fm-caption fw-bold text-primary mb-2 text-uppercase ls-wide">Logic Trace</div>
                            <p class="small text-muted mb-0">Source: <code>revenue_daily</code> aggregated by COMPLETED fulfillments.</p>
                        </div>
                    </div>
                </div>
            </div>
        </c:when>

        <%-- SEASONALITY AUDIT --%>
        <c:when test="${tab == 'seasonality'}">
            <div class="row g-4 mb-5">
                <div class="col-md-3">
                    <div class="fm-pro-card p-4 h-100">
                        <div class="fm-kpi-pro-icon bg-info-subtle text-info mb-3"><i class="bi bi-clock-history"></i></div>
                        <h6 class="fm-caption fw-bold text-muted mb-1">Audit Depth</h6>
                        <div class="fm-h2 mb-0">${seasonalityHistory} <span class="fs-6 opacity-50">Days</span></div>
                    </div>
                </div>
                <div class="col-md-3">
                    <div class="fm-pro-card p-4 h-100">
                        <div class="fm-kpi-pro-icon bg-success-subtle text-success mb-3"><i class="bi bi-graph-up"></i></div>
                        <h6 class="fm-caption fw-bold text-muted mb-1">Peak Signals</h6>
                        <div class="fm-h2 mb-0">${peakCount} <span class="fs-6 opacity-50">Detected</span></div>
                    </div>
                </div>
                <div class="col-md-3">
                    <div class="fm-pro-card p-4 h-100">
                        <div class="fm-kpi-pro-icon bg-danger-subtle text-danger mb-3"><i class="bi bi-graph-down"></i></div>
                        <h6 class="fm-caption fw-bold text-muted mb-1">Dip Signals</h6>
                        <div class="fm-h2 mb-0">${dipCount} <span class="fs-6 opacity-50">Detected</span></div>
                    </div>
                </div>
                <div class="col-md-3">
                    <div class="fm-pro-card p-4 h-100">
                        <div class="fm-kpi-pro-icon bg-indigo-subtle text-indigo-600 mb-3"><i class="bi bi-sliders"></i></div>
                        <h6 class="fm-caption fw-bold text-muted mb-1">Rolling Window</h6>
                        <div class="fm-h2 mb-0">${rollingWindow} <span class="fs-6 opacity-50">Periods</span></div>
                    </div>
                </div>
            </div>

            <div class="row g-4">
                <div class="col-lg-4">
                    <div class="fm-surface p-4 shadow-sm border-0 sticky-top" style="top: 1rem;">
                        <h5 class="fm-h3 mb-4 text-dark"><i class="bi bi-funnel me-2 text-primary"></i> Analytical Constraints</h5>
                        <form method="get" class="vstack gap-4">
                            <input type="hidden" name="tab" value="seasonality"/>
                            <div>
                                <label class="fm-caption fw-bold text-muted d-block mb-2">Observation History (Days)</label>
                                <input class="fm-form-control" type="number" name="seasonalityHistory" value="${seasonalityHistory}" min="7" max="730"/>
                            </div>
                            <div>
                                <label class="fm-caption fw-bold text-muted d-block mb-2">Rolling Mean Window</label>
                                <input class="fm-form-control" type="number" name="rollingWindow" value="${rollingWindow}" min="3" max="30"/>
                            </div>
                            <div>
                                <label class="fm-caption fw-bold text-muted d-block mb-2">Anomaly Z-Threshold</label>
                                <input class="fm-form-control" type="number" step="0.1" name="zThreshold" value="${zThreshold}" min="0.5" max="5"/>
                            </div>
                            <button class="fm-btn fm-btn-primary w-100 py-3 fw-bold" type="submit">Run Seasonality Sync</button>
                        </form>
                        <div class="insight bg-light p-3 rounded-4 mt-5">
                            <p class="small text-muted mb-0"><strong>Peak Detection:</strong> Revenue > rolling mean + offset.<br><strong>Dip Detection:</strong> Revenue < rolling mean - offset.</p>
                        </div>
                    </div>
                </div>
                <div class="col-lg-8">
                    <div class="fm-surface p-4 shadow-sm border-0 mb-4">
                        <h5 class="fm-h3 mb-4">Seasonality Trend Analysis</h5>
                        <div class="chart-container-pro">
                            <canvas id="seasonalityChart"></canvas>
                        </div>
                    </div>
                    <div class="fm-surface p-4 shadow-sm border-0">
                        <h5 class="fm-h3 mb-4">Cyclical Monthly Performance</h5>
                        <div class="chart-container-pro" style="height: 250px;">
                            <canvas id="monthChart"></canvas>
                        </div>
                    </div>
                </div>
            </div>
        </c:when>

        <%-- REPLENISHMENT LOGIC --%>
        <c:when test="${tab == 'replenishment'}">
            <div class="row g-4 mb-5">
                <div class="col-md-3">
                    <div class="fm-pro-card p-4 h-100 border-start border-4 border-warning">
                        <h6 class="fm-caption fw-bold text-muted mb-1">Restock Signals</h6>
                        <div class="fm-h2 mb-0 text-warning">${restockCount} <span class="fs-6 opacity-50">SKUs</span></div>
                    </div>
                </div>
                <div class="col-md-3">
                    <div class="fm-pro-card p-4 h-100">
                        <h6 class="fm-caption fw-bold text-muted mb-1">Gross Suggested Qty</h6>
                        <div class="fm-h2 mb-0">${totalSuggestedQty}</div>
                    </div>
                </div>
                <div class="col-md-3">
                    <div class="fm-pro-card p-4 h-100">
                        <h6 class="fm-caption fw-bold text-muted mb-1">Total Expiring Qty</h6>
                        <div class="fm-h2 mb-0 text-danger">${totalExpiringQty}</div>
                    </div>
                </div>
                <div class="col-md-3">
                    <div class="fm-pro-card p-4 h-100 bg-slate-900 text-white">
                        <h6 class="fm-caption fw-bold text-white-50 mb-1">L/B/S Coefficients</h6>
                        <div class="fm-h2 mb-0">${leadTimeDays}/${bufferDays}/${safetyDays}</div>
                    </div>
                </div>
            </div>

            <div class="fm-surface p-0 shadow-sm border-0 overflow-hidden mb-4">
                <div class="p-4 border-bottom d-flex justify-content-between align-items-center bg-light">
                    <h5 class="fm-h3 mb-0">Algorithmic Replenishment Ledger</h5>
                    <a class="fm-btn fm-btn-primary btn-sm" href="${pageContext.request.contextPath}/pro/replenishment">
                        <i class="bi bi-fullscreen me-1"></i> Full Analysis
                    </a>
                </div>
                <div class="table-responsive">
                    <table class="table fm-data-table align-middle mb-0">
                        <thead>
                            <tr>
                                <th class="ps-4">Operational SKU</th>
                                <th class="text-end">Season Factor</th>
                                <th class="text-center">Current Stock</th>
                                <th class="text-center">Suggested</th>
                                <th class="text-end pe-4">Risk Exposure</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="r" items="${replenishmentRows}">
                                <tr>
                                    <td class="ps-4">
                                        <div class="fw-bold text-dark">${r.productName}</div>
                                        <div class="small opacity-50">Fcst: ${r.forecastPerDay}/day</div>
                                    </td>
                                    <td class="text-end font-monospace">
                                        <fmt:formatNumber value="${r.seasonFactor}" type="number" minFractionDigits="2"/>
                                    </td>
                                    <td class="text-center">
                                        <span class="badge bg-slate-100 text-slate-700 border-0 px-3">${r.stock} Units</span>
                                    </td>
                                    <td class="text-center">
                                        <c:choose>
                                            <c:when test="${r.suggestedQty > 0}">
                                                <span class="badge bg-warning text-dark border-0 px-3 fw-bold">+${r.suggestedQty}</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge bg-success-subtle text-success border-0 px-3">OPTIMIZED</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td class="text-end pe-4 text-danger small fw-bold">
                                        <c:if test="${r.expiringQty > 0}">
                                            ${r.expiringQty} Expiring
                                        </c:if>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </div>
        </c:when>
    </c:choose>
</div>

<!-- Chart.js Logic (Consolidated) -->
<script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.3/dist/chart.umd.min.js"></script>
<script>
    const activeTab = '${tab}';

    if (activeTab === 'forecast') {
        const labels = <c:out value="${labelsJson}" escapeXml="false" />;
        const actualData = <c:out value="${actualJson}" escapeXml="false" />;
        const forecastData = <c:out value="${forecastJson}" escapeXml="false" />;
        
        new Chart(ctx, {
            type: 'line',
            data: {
                labels: labels,
                datasets: [
                    {
                        label: 'Operational Actuals',
                        data: actualData,
                        borderColor: '#2563eb',
                        backgroundColor: 'rgba(37, 99, 235, 0.1)',
                        borderWidth: 3,
                        tension: 0.4,
                        fill: true,
                        pointRadius: 4,
                        pointHoverRadius: 6
                    },
                    {
                        label: 'Algorithmic Forecast',
                        data: forecastData,
                        borderColor: '#7c3aed',
                        backgroundColor: 'transparent',
                        borderWidth: 3,
                        borderDash: [5, 5],
                        tension: 0.4,
                        pointRadius: 4,
                        pointHoverRadius: 6
                    }
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { position: 'top', labels: { usePointStyle: true, padding: 20, font: { weight: '600' } } },
                    tooltip: { padding: 12, backgroundColor: '#0f172a' }
                },
                scales: {
                    y: { beginAtZero: true, grid: { color: 'rgba(0,0,0,0.05)' } },
                    x: { grid: { display: false } }
                }
            }
        });
    }

    if (activeTab === 'seasonality') {
        const labels = <c:out value="${seasonalityLabelsJson}" escapeXml="false" />;
        const actual = <c:out value="${seasonalityActualJson}" escapeXml="false" />;
        const rolling = <c:out value="${seasonalityRollingJson}" escapeXml="false" />;
        
        new Chart(document.getElementById('seasonalityChart'), {
            type: 'line',
            data: {
                labels,
                datasets: [
                    { label: 'Actual Revenue', data: actual, borderColor: '#2563eb', tension: 0.4 },
                    { label: 'Rolling Mean', data: rolling, borderColor: '#64748b', borderDash: [5, 5], tension: 0.4 }
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: { legend: { position: 'top' } }
            }
        });

        const monthNames = <c:out value="${monthNamesJson}" escapeXml="false" />;
        const monthAvg = <c:out value="${monthAvgJson}" escapeXml="false" />;
        
        new Chart(document.getElementById('monthChart'), {
            type: 'bar',
            data: {
                labels: monthNames,
                datasets: [
                    { label: 'Avg Monthly Demand', data: monthAvg, backgroundColor: '#4f46e5', borderRadius: 8 }
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: { legend: { display: false } }
            }
        });
    }
</script>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>