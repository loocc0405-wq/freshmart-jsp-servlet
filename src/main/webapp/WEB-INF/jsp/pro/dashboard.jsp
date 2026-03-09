<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:set var="pageTitle" value="PRO Dashboard" />
<jsp:include page="/WEB-INF/jsp/common/header.jsp" />

<style>
    .pro-tabs {
        display: flex;
        gap: 10px;
        margin-bottom: 20px;
        flex-wrap: wrap;
    }

    .pro-tab {
        text-decoration: none;
        padding: 10px 14px;
        border-radius: 8px;
        border: 1px solid #d0d7de;
        background: #fff;
        color: #333;
        font-weight: 600;
    }

    .pro-tab.active {
        background: #0d6efd;
        color: #fff;
        border-color: #0d6efd;
    }

    .kpi-grid {
        display: grid;
        grid-template-columns: repeat(4, 1fr);
        gap: 16px;
        margin-bottom: 20px;
    }

    .kpi-card {
        background: #fff;
        border: 1px solid #e5e7eb;
        border-radius: 12px;
        padding: 16px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, .05);
    }

    .kpi-card h6 {
        margin: 0 0 8px;
        color: #555;
        font-size: 14px;
    }

    .kpi-card .value {
        font-size: 24px;
        font-weight: 700;
    }

    .panel-card {
        background: #fff;
        border: 1px solid #e5e7eb;
        border-radius: 12px;
        padding: 18px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, .05);
        margin-bottom: 20px;
    }

    .chart-box {
        min-height: 320px;
    }

    .insight {
        background: #f8f9fa;
        border-left: 4px solid #0d6efd;
        padding: 12px 14px;
        border-radius: 8px;
        margin-top: 12px;
    }

    .table-wrap {
        overflow-x: auto;
    }

    @media (max-width: 992px) {
        .kpi-grid {
            grid-template-columns: repeat(2, 1fr);
        }
    }

    @media (max-width: 576px) {
        .kpi-grid {
            grid-template-columns: 1fr;
        }
    }
</style>

<h3 class="mb-3">PRO Dashboard - Module 9</h3>

<div class="pro-tabs">
    <a class="pro-tab ${tab == 'forecast' ? 'active' : ''}"
       href="${pageContext.request.contextPath}/pro/dashboard?tab=forecast&method=${method}&history=${history}&horizon=${horizon}">
        9.1 Forecast
    </a>
    <a class="pro-tab ${tab == 'seasonality' ? 'active' : ''}"
       href="${pageContext.request.contextPath}/pro/dashboard?tab=seasonality&seasonalityHistory=${seasonalityHistory}&rollingWindow=${rollingWindow}&zThreshold=${zThreshold}">
        9.2 Seasonality
    </a>
    <a class="pro-tab ${tab == 'replenishment' ? 'active' : ''}"
       href="${pageContext.request.contextPath}/pro/dashboard?tab=replenishment&replenishmentHistory=${replenishmentHistory}&leadTimeDays=${leadTimeDays}&bufferDays=${bufferDays}&safetyDays=${safetyDays}">
        9.3 Replenishment
    </a>
</div>

<c:choose>

    <c:when test="${tab == 'seasonality'}">
        <div class="kpi-grid">
            <div class="kpi-card">
                <h6>Days History</h6>
                <div class="value">${seasonalityHistory}</div>
            </div>
            <div class="kpi-card">
                <h6>Rolling Window</h6>
                <div class="value">${rollingWindow}</div>
            </div>
            <div class="kpi-card">
                <h6>Peak Signals</h6>
                <div class="value">${peakCount}</div>
            </div>
            <div class="kpi-card">
                <h6>Dip Signals</h6>
                <div class="value">${dipCount}</div>
            </div>
        </div>

        <div class="row g-3 mb-3">
            <div class="col-lg-4">
                <div class="panel-card">
                    <form method="get" action="${pageContext.request.contextPath}/pro/dashboard" class="vstack gap-2">
                        <input type="hidden" name="tab" value="seasonality" />
                        <label class="form-label mb-0">History (days)</label>
                        <input class="form-control" type="number" name="seasonalityHistory"
                               value="${seasonalityHistory}" min="30" max="730" />

                        <label class="form-label mb-0">Rolling window</label>
                        <input class="form-control" type="number" name="rollingWindow"
                               value="${rollingWindow}" min="3" max="30" />

                        <label class="form-label mb-0">Z-threshold</label>
                        <input class="form-control" type="number" step="0.1" name="zThreshold"
                               value="${zThreshold}" min="0.5" max="5" />

                        <button class="btn btn-primary" type="submit">Run seasonality</button>
                    </form>

                    <div class="insight">
                        Peak = doanh thu cao hơn rolling mean vượt ngưỡng z-score.
                        Dip = doanh thu thấp hơn rolling mean vượt ngưỡng âm.
                    </div>
                </div>
            </div>

            <div class="col-lg-8">
                <div class="panel-card">
                    <h5>Seasonality Trend</h5>
                    <div class="chart-box">
                        <canvas id="seasonalityChart"></canvas>
                    </div>
                </div>
            </div>
        </div>

        <div class="panel-card">
            <h5>Monthly Summary</h5>
            <div class="chart-box mb-3">
                <canvas id="monthChart"></canvas>
            </div>

            <div class="table-wrap">
                <table class="table table-striped">
                    <thead>
                    <tr>
                        <th>Month</th>
                        <th>Average</th>
                        <th>Min</th>
                        <th>Max</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="m" items="${monthStats}">
                        <tr>
                            <td>${m.label}</td>
                            <td>
                                <fmt:formatNumber value="${m.avgDemand}" type="number"
                                                  minFractionDigits="0" maxFractionDigits="2" />
                            </td>
                            <td>
                                <fmt:formatNumber value="${m.minDemand}" type="number"
                                                  minFractionDigits="0" maxFractionDigits="2" />
                            </td>
                            <td>
                                <fmt:formatNumber value="${m.maxDemand}" type="number"
                                                  minFractionDigits="0" maxFractionDigits="2" />
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>
    </c:when>

    <c:when test="${tab == 'replenishment'}">
        <div class="kpi-grid">
            <div class="kpi-card">
                <h6>Products Need Restock</h6>
                <div class="value">${restockCount}</div>
            </div>
            <div class="kpi-card">
                <h6>Total Suggested Qty</h6>
                <div class="value">${totalSuggestedQty}</div>
            </div>
            <div class="kpi-card">
                <h6>Total Expiring Qty</h6>
                <div class="value">${totalExpiringQty}</div>
            </div>
            <div class="kpi-card">
                <h6>Lead / Buffer / Safety</h6>
                <div class="value">${leadTimeDays}/${bufferDays}/${safetyDays}</div>
            </div>
        </div>

        <div class="row g-3 mb-3">
            <div class="col-lg-4">
                <div class="panel-card">
                    <form method="get" action="${pageContext.request.contextPath}/pro/dashboard" class="vstack gap-2">
                        <input type="hidden" name="tab" value="replenishment" />
                        <label class="form-label mb-0">History (days)</label>
                        <input class="form-control" type="number" name="replenishmentHistory"
                               value="${replenishmentHistory}" min="7" max="90" />

                        <label class="form-label mb-0">Lead time days</label>
                        <input class="form-control" type="number" name="leadTimeDays"
                               value="${leadTimeDays}" min="1" max="30" />

                        <label class="form-label mb-0">Buffer days</label>
                        <input class="form-control" type="number" name="bufferDays" value="${bufferDays}"
                               min="0" max="30" />

                        <label class="form-label mb-0">Safety days</label>
                        <input class="form-control" type="number" name="safetyDays" value="${safetyDays}"
                               min="0" max="30" />

                        <button class="btn btn-primary" type="submit">Run replenishment</button>
                    </form>

                    <div class="insight">
                        Gợi ý nhập hàng được tính theo forecast/day, stock hiện có, lead time, buffer và
                        safety stock.
                    </div>
                </div>
            </div>

            <div class="col-lg-8">
                <div class="panel-card">
                    <h5>Top Replenishment Suggestions</h5>
                    <div class="table-wrap">
                        <table class="table table-striped">
                            <thead>
                            <tr>
                                <th>Product</th>
                                <th>Avg 7</th>
                                <th>Avg History</th>
                                <th>Season Factor</th>
                                <th>Forecast/Day</th>
                                <th>Stock</th>
                                <th>Suggested</th>
                                <th>Expiring Qty</th>
                                <th>Expiring Lots</th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:forEach var="r" items="${replenishmentRows}">
                                <tr>
                                    <td>${r.productName}</td>
                                    <td>
                                        <fmt:formatNumber value="${r.avg7}" type="number"
                                                          minFractionDigits="0" maxFractionDigits="2" />
                                    </td>
                                    <td>
                                        <fmt:formatNumber value="${r.avg30}" type="number"
                                                          minFractionDigits="0" maxFractionDigits="2" />
                                    </td>
                                    <td>
                                        <fmt:formatNumber value="${r.seasonFactor}" type="number"
                                                          minFractionDigits="0" maxFractionDigits="2" />
                                    </td>
                                    <td>
                                        <fmt:formatNumber value="${r.forecastPerDay}" type="number"
                                                          minFractionDigits="0" maxFractionDigits="2" />
                                    </td>
                                    <td>${r.stock}</td>
                                    <td>${r.suggestedQty}</td>
                                    <td>${r.expiringQty}</td>
                                    <td>${r.expiringLots}</td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </div>

                    <div class="insight mt-3">
                        Các sản phẩm có nhiều lô sắp hết hạn sẽ được ưu tiên clearance trước, từ đó
                        suggestion có thể bị giảm.
                    </div>
                </div>
            </div>
        </div>
    </c:when>

    <c:otherwise>
        <div class="kpi-grid">
            <div class="kpi-card">
                <h6>Method</h6>
                <div class="value">${method}</div>
            </div>
            <div class="kpi-card">
                <h6>History</h6>
                <div class="value">${history}</div>
            </div>
            <div class="kpi-card">
                <h6>Latest Actual</h6>
                <div class="value">
                    <fmt:formatNumber value="${latestActual}" type="number" minFractionDigits="0"
                                      maxFractionDigits="2" />
                </div>
            </div>
            <div class="kpi-card">
                <h6>Latest Forecast</h6>
                <div class="value">
                    <fmt:formatNumber value="${latestForecast}" type="number" minFractionDigits="0"
                                      maxFractionDigits="2" />
                </div>
            </div>
        </div>

        <div class="row mb-3">
            <div class="col-lg-8">
                <div class="panel-card">
                    <div class="d-flex justify-content-between align-items-center mb-2">
                        <h5 class="mb-0">Forecast Chart</h5>
                        <a class="btn btn-sm btn-outline-success"
                           href="${pageContext.request.contextPath}/pro/dashboard?tab=forecast&export=csv&granularity=${granularity}&method=${method}&history=${history}&horizon=${horizon}&window=${window}&alpha=${alpha}">
                            <i class="bi bi-download me-1"></i>Export CSV
                        </a>
                    </div>
                    <div class="chart-box">
                        <canvas id="forecastChart"></canvas>
                    </div>
                </div>

                <div class="panel-card">
                    <h6>Forecast Data Table</h6>
                    <div class="table-wrap">
                        <table class="table table-sm table-hover">
                            <thead>
                            <tr>
                                <th>Period</th>
                                <th class="text-end">Actual Revenue</th>
                                <th class="text-end">Forecast Revenue</th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:forEach var="b" items="${forecastBuckets}">
                                <tr class="${b.forecast != null && b.actual == null ? 'table-info' : ''}">
                                    <td>
                                        <c:out value="${b.label}" />
                                    </td>
                                    <td class="text-end">
                                        <c:if test="${b.actual != null}">
                                            <fmt:formatNumber value="${b.actual}" type="number" groupingUsed="true" />
                                        </c:if>
                                    </td>
                                    <td class="text-end">
                                        <c:if test="${b.forecast != null}">
                                            <fmt:formatNumber value="${b.forecast}" type="number" groupingUsed="true" />
                                        </c:if>
                                    </td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>

            <div class="col-lg-4">
                <div class="panel-card">
                    <form method="get" action="${pageContext.request.contextPath}/pro/dashboard" class="vstack gap-2">
                        <input type="hidden" name="tab" value="forecast" />

                        <label class="form-label mb-0">Granularity</label>
                        <select class="form-select" name="granularity">
                            <option value="day" <c:if test="${granularity == 'day'}">selected</c:if>>Day</option>
                            <option value="month" <c:if test="${granularity == 'month'}">selected</c:if>>Month</option>
                            <option value="quarter" <c:if test="${granularity == 'quarter'}">selected</c:if>>Quarter</option>
                            <option value="year" <c:if test="${granularity == 'year'}">selected</c:if>>Year</option>
                        </select>

                        <label class="form-label mb-0">Method</label>
                        <select class="form-select" name="method">
                            <option value="ma" <c:if test="${method == 'ma'}">selected</c:if>>Moving Average</option>
                            <option value="es" <c:if test="${method == 'es'}">selected</c:if>>Exponential Smoothing</option>
                        </select>

                        <div class="row g-2">
                            <div class="col-6">
                                <label class="form-label mb-0">History (periods)</label>
                                <input class="form-control" type="number" name="history" value="${history}"
                                       min="3" max="730" />
                            </div>
                            <div class="col-6">
                                <label class="form-label mb-0">Horizon (periods)</label>
                                <input class="form-control" type="number" name="horizon" value="${horizon}"
                                       min="1" max="60" />
                            </div>
                        </div>

                        <c:choose>
                            <c:when test="${method == 'es'}">
                                <label class="form-label mb-0">Alpha (0..1)</label>
                                <input class="form-control" type="number" step="0.05" name="alpha"
                                       value="${alpha}" min="0.05" max="0.95" />
                            </c:when>
                            <c:otherwise>
                                <label class="form-label mb-0">Window</label>
                                <input class="form-control" type="number" name="window" value="${window}"
                                       min="3" max="30" />
                            </c:otherwise>
                        </c:choose>

                        <button class="btn btn-primary" type="submit">Run forecast</button>
                    </form>

                    <div class="insight">
                        Data source: <code>revenue_daily(revenue_date, total_revenue)</code>.
                        Orders COMPLETED sẽ tự động cộng doanh thu theo ngày.
                        Chọn granularity để xem dữ liệu tổng hợp theo tháng/quý/năm.
                    </div>
                </div>
            </div>
        </div>
    </c:otherwise>

</c:choose>

<script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.3/dist/chart.umd.min.js"></script>
<script>
    const activeTab = '${tab}';

    if (activeTab === 'forecast') {
        const labels = <c:out value="${labelsJson}" escapeXml="false" />;
        const actual = <c:out value="${actualJson}" escapeXml="false" />;
        const forecast = <c:out value="${forecastJson}" escapeXml="false" />;

        new Chart(document.getElementById('forecastChart'), {
            type: 'line',
            data: {
                labels,
                datasets: [
                    { label: 'Actual', data: actual, spanGaps: true },
                    { label: 'Forecast', data: forecast, spanGaps: true }
                ]
            },
            options: {
                responsive: true,
                interaction: { mode: 'index', intersect: false },
                plugins: { legend: { position: 'top' } },
                scales: { y: { beginAtZero: true } }
            }
        });
    }

    if (activeTab === 'seasonality') {
        const labels = <c:out value="${seasonalityLabelsJson}" escapeXml="false" />;
        const actual = <c:out value="${seasonalityActualJson}" escapeXml="false" />;
        const rolling = <c:out value="${seasonalityRollingJson}" escapeXml="false" />;
        const z = <c:out value="${seasonalityZJson}" escapeXml="false" />;

        new Chart(document.getElementById('seasonalityChart'), {
            type: 'line',
            data: {
                labels,
                datasets: [
                    { label: 'Actual', data: actual, spanGaps: true },
                    { label: 'Rolling Mean', data: rolling, spanGaps: true },
                    { label: 'Z-Score', data: z, spanGaps: true }
                ]
            },
            options: {
                responsive: true,
                interaction: { mode: 'index', intersect: false }
            }
        });

        const monthNames = <c:out value="${monthNamesJson}" escapeXml="false" />;
        const monthAvg = <c:out value="${monthAvgJson}" escapeXml="false" />;
        const monthMin = <c:out value="${monthMinJson}" escapeXml="false" />;
        const monthMax = <c:out value="${monthMaxJson}" escapeXml="false" />;

        new Chart(document.getElementById('monthChart'), {
            type: 'bar',
            data: {
                labels: monthNames,
                datasets: [
                    { label: 'Avg', data: monthAvg },
                    { label: 'Min', data: monthMin },
                    { label: 'Max', data: monthMax }
                ]
            },
            options: {
                responsive: true
            }
        });
    }
</script>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp" />