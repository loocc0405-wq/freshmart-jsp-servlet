<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="PRO Seasonality"/>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"/>

<h3>PRO - Seasonality (Rolling Mean + Z-score)</h3>

<div class="row mb-3">
  <div class="col-lg-8">
    <canvas id="chart1" height="120"></canvas>
    <div class="mt-3">
      <canvas id="chart2" height="90"></canvas>
    </div>
  </div>

  <div class="col-lg-4">
    <div class="card">
      <div class="card-body">
        <form method="get" action="${pageContext.request.contextPath}/pro/seasonality" class="vstack gap-2">
          <label class="form-label mb-0">History (days)</label>
          <input class="form-control" type="number" name="history" value="${history}" min="30" max="365"/>

          <label class="form-label mb-0">Window (rolling)</label>
          <input class="form-control" type="number" name="window" value="${window}" min="3" max="30"/>

          <label class="form-label mb-0">Z threshold</label>
          <input class="form-control" type="number" step="0.1" name="z" value="${z}" min="0.5" max="5"/>

          <button class="btn btn-primary" type="submit">Analyze</button>
        </form>

        <hr/>
        <p class="text-muted mb-0">
          Data source: <code>revenue_daily(revenue_date, total_revenue)</code><br/>
          PEAK nếu z ≥ threshold, DIP nếu z ≤ -threshold.
        </p>
      </div>
    </div>
  </div>
</div>

<c:if test="${not empty flagged}">
  <h5>Detected Peaks / Dips</h5>
  <div class="table-responsive">
    <table class="table table-bordered bg-white align-middle">
      <thead class="table-light">
      <tr>
        <th>Date</th>
        <th class="text-end">Revenue</th>
        <th class="text-end">Rolling Mean</th>
        <th class="text-end">Z-score</th>
        <th>Signal</th>
      </tr>
      </thead>
      <tbody>
      <c:forEach var="p" items="${flagged}">
        <tr>
          <td><c:out value="${p.date}"/></td>
          <td class="text-end"><fmt:formatNumber value="${p.actual}" type="number" minFractionDigits="2" maxFractionDigits="2"/></td>
          <td class="text-end"><fmt:formatNumber value="${p.rollingMean}" type="number" minFractionDigits="2" maxFractionDigits="2"/></td>
          <td class="text-end"><c:out value="${p.zScore}"/></td>
          <td>
            <c:choose>
              <c:when test="${p.signal == 'PEAK'}"><span class="badge text-bg-danger">PEAK</span></c:when>
              <c:when test="${p.signal == 'DIP'}"><span class="badge text-bg-info">DIP</span></c:when>
              <c:otherwise>-</c:otherwise>
            </c:choose>
          </td>
        </tr>
      </c:forEach>
      </tbody>
    </table>
  </div>
</c:if>

<script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.3/dist/chart.umd.min.js"></script>
<script>
  const labels = <c:out value="${labelsJson}" escapeXml="false"/>;
  const actual = <c:out value="${actualJson}" escapeXml="false"/>;
  const mean = <c:out value="${meanJson}" escapeXml="false"/>;
  const z = <c:out value="${zJson}" escapeXml="false"/>;

  new Chart(document.getElementById('chart1'), {
    type: 'line',
    data: {
      labels,
      datasets: [
        { label: 'Actual Revenue', data: actual, spanGaps: true },
        { label: 'Rolling Mean', data: mean, spanGaps: true }
      ]
    },
    options: {
      responsive: true,
      interaction: { mode: 'index', intersect: false },
      plugins: { legend: { position: 'top' } },
      scales: { y: { beginAtZero: true } }
    }
  });

  new Chart(document.getElementById('chart2'), {
    type: 'line',
    data: {
      labels,
      datasets: [
        { label: 'Z-score', data: z, spanGaps: true }
      ]
    },
    options: {
      responsive: true,
      plugins: { legend: { position: 'top' } },
      scales: { y: { } }
    }
  });
</script>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>