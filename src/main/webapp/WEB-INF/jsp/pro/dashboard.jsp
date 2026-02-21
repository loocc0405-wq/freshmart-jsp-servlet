<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="PRO Dashboard"/>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"/>

<h3>PRO Dashboard - Dự báo doanh thu (No AI)</h3>

<div class="row mb-3">
    <div class="col-lg-8">
        <canvas id="chart" height="120"></canvas>
    </div>
    <div class="col-lg-4">
        <div class="card">
            <div class="card-body">
                <form method="get" action="${pageContext.request.contextPath}/pro/dashboard" class="vstack gap-2">
                    <label class="form-label mb-0">Method</label>
                    <select class="form-select" name="method">
                        <option value="ma" <c:if test="${method == 'ma'}">selected</c:if>>Moving Average</option>
                        <option value="es" <c:if test="${method == 'es'}">selected</c:if>>Exponential Smoothing</option>
                    </select>

                    <div class="row g-2">
                        <div class="col-6">
                            <label class="form-label mb-0">History (days)</label>
                            <input class="form-control" type="number" name="history" value="${history}" min="30" max="365"/>
                        </div>
                        <div class="col-6">
                            <label class="form-label mb-0">Horizon (days)</label>
                            <input class="form-control" type="number" name="horizon" value="${horizon}" min="7" max="60"/>
                        </div>
                    </div>

                    <c:choose>
                        <c:when test="${method == 'es'}">
                            <label class="form-label mb-0">Alpha (0..1)</label>
                            <input class="form-control" type="number" step="0.05" name="alpha" value="${alpha}" min="0.05" max="0.95"/>
                        </c:when>
                        <c:otherwise>
                            <label class="form-label mb-0">Window</label>
                            <input class="form-control" type="number" name="window" value="${window}" min="3" max="30"/>
                        </c:otherwise>
                    </c:choose>

                    <button class="btn btn-primary" type="submit">Run forecast</button>
                </form>

                <hr/>
                <p class="text-muted mb-0">
                    Data source: <code>revenue_daily(revenue_date, total_revenue)</code>.<br/>
                    Orders COMPLETED sẽ tự động cộng doanh thu theo ngày.
                </p>
            </div>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.3/dist/chart.umd.min.js"></script>
<script>
    const labels = <c:out value="${labelsJson}" escapeXml="false"/>;
    const actual = <c:out value="${actualJson}" escapeXml="false"/>;
    const forecast = <c:out value="${forecastJson}" escapeXml="false"/>;

    const ctx = document.getElementById('chart');

    new Chart(ctx, {
        type: 'line',
        data: {
            labels,
            datasets: [
                {
                    label: 'Actual',
                    data: actual,
                    spanGaps: true
                },
                {
                    label: 'Forecast',
                    data: forecast,
                    spanGaps: true
                }
            ]
        },
        options: {
            responsive: true,
            interaction: {
                mode: 'index',
                intersect: false,
            },
            plugins: {
                legend: { position: 'top' },
                tooltip: { enabled: true }
            },
            scales: {
                y: {
                    beginAtZero: true
                }
            }
        }
    });
</script>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
