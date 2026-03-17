<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="pageTitle" value="PRO Seasonality"/>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"/>

<div class="container-xl py-4">
  <h3>PRO - Seasonality (Rolling Mean + Z-score)</h3>
  
  <div class="row mb-4">
    <div class="col-lg-8">
      <div class="fm-surface p-3 mb-4">
        <canvas id="chart1" height="120"></canvas>
      </div>
      <div class="fm-surface p-3">
        <canvas id="chart2" height="90"></canvas>
      </div>
    </div>
  
    <div class="col-lg-4">
      <div class="fm-card">
        <div class="card-body p-0">
          <form method="get" action="${pageContext.request.contextPath}/pro/seasonality" class="vstack gap-3">
            <div>
              <label class="fm-label">History (days)</label>
              <input class="form-control" type="number" name="history" value="${history}" min="30" max="365"/>
            </div>
  
            <div>
              <label class="fm-label">Window (rolling)</label>
              <input class="form-control" type="number" name="window" value="${window}" min="3" max="30"/>
            </div>
  
            <div>
              <label class="fm-label">Z threshold</label>
              <input class="form-control" type="number" step="0.1" name="z" value="${z}" min="0.5" max="5"/>
            </div>
  
            <button class="fm-btn fm-btn-primary w-100" type="submit">
              <i class="bi bi-search me-2"></i>Analyze Data
            </button>
          </form>
  
          <div class="mt-4 pt-3 border-top">
            <p class="fm-caption mb-1">
              Data source: <code>revenue_daily</code>
            </p>
            <p class="fm-caption">
              PEAK if z ≥ threshold, DIP if z ≤ -threshold.
            </p>
          </div>
        </div>
      </div>
    </div>
  </div>
  
  <c:if test="${not empty flagged}">
    <h5 class="fm-h3 mb-3">Detected Peaks / Dips</h5>
    <div class="fm-surface overflow-hidden">
      <div class="table-responsive">
        <table class="fm-data-table">
          <thead>
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
              <td class="text-end fw-bold"><fmt:formatNumber value="${p.actual}" type="number" minFractionDigits="0" maxFractionDigits="0"/></td>
              <td class="text-end text-muted"><fmt:formatNumber value="${p.rollingMean}" type="number" minFractionDigits="0" maxFractionDigits="0"/></td>
              <td class="text-end"><span class="badge bg-light text-dark border">${p.zScore}</span></td>
              <td>
                <c:choose>
                  <c:when test="${p.signal == 'PEAK'}"><span class="fm-status-badge expired">PEAK</span></c:when>
                  <c:when test="${p.signal == 'DIP'}"><span class="fm-status-badge available">DIP</span></c:when>
                  <c:otherwise>-</c:otherwise>
                </c:choose>
              </td>
            </tr>
          </c:forEach>
          </tbody>
        </table>
      </div>
    </div>
  </c:if>
</div>

<script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.3/dist/chart.umd.min.js"></script>
<script>
  (function() {
    const labels = <c:out value="${labelsJson}" escapeXml="false"/> || [];
    const actual = <c:out value="${actualJson}" escapeXml="false"/> || [];
    const mean = <c:out value="${meanJson}" escapeXml="false"/> || [];
    const z = <c:out value="${zJson}" escapeXml="false"/> || [];

    if (!document.getElementById('chart1')) return;

    new Chart(document.getElementById('chart1'), {
      type: 'line',
      data: {
        labels,
        datasets: [
          { 
            label: 'Actual Revenue', 
            data: actual, 
            borderColor: '#22C55E',
            backgroundColor: 'rgba(34, 197, 94, 0.1)',
            fill: true,
            tension: 0.3,
            spanGaps: true 
          },
          { 
            label: 'Rolling Mean', 
            data: mean, 
            borderColor: '#64748B',
            borderDash: [5, 5],
            fill: false,
            tension: 0.3,
            spanGaps: true 
          }
        ]
      },
      options: {
        responsive: true,
        maintainAspectRatio: true,
        interaction: { mode: 'index', intersect: false },
        plugins: { 
          legend: { position: 'top', labels: { usePointStyle: true, padding: 20 } } 
        },
        scales: { 
          y: { beginAtZero: true, grid: { color: 'rgba(0,0,0,0.05)' } },
          x: { grid: { display: false } }
        }
      }
    });

    if (!document.getElementById('chart2')) return;

    new Chart(document.getElementById('chart2'), {
      type: 'line',
      data: {
        labels,
        datasets: [
          { 
            label: 'Z-score', 
            data: z, 
            borderColor: '#2563EB',
            backgroundColor: 'rgba(37, 99, 235, 0.1)',
            fill: true,
            tension: 0.3,
            spanGaps: true 
          }
        ]
      },
      options: {
        responsive: true,
        plugins: { 
          legend: { position: 'top', labels: { usePointStyle: true } } 
        },
        scales: { 
          y: { grid: { color: 'rgba(0,0,0,0.05)' } },
          x: { grid: { display: false } }
        }
      }
    });
  })();
</script>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>