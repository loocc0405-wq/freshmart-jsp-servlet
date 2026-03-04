<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:set var="pageTitle" value="PRO - Restock Recommendation"/>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"/>

<div class="container my-4">
  <div class="d-flex justify-content-between align-items-center mb-2">
    <h3>PRO - Gợi ý nhập hàng (Restock)</h3>
  </div>

  <div class="row g-3 mb-3">
    <div class="col-lg-4">
      <div class="card">
        <div class="card-body">
          <form method="get" action="${pageContext.request.contextPath}/pro/replenishment" class="vstack gap-2">
            <label class="form-label mb-0">History (days)</label>
            <input class="form-control" type="number" name="history" min="7" max="365" value="${daysHistory}"/>

            <label class="form-label mb-0">Lead time (days)</label>
            <input class="form-control" type="number" name="lead" min="0" max="60" value="${leadTimeDays}"/>

            <label class="form-label mb-0">Buffer (days)</label>
            <input class="form-control" type="number" name="buffer" min="0" max="60" value="${bufferDays}"/>

            <label class="form-label mb-0">Safety (days)</label>
            <input class="form-control" type="number" name="safety" min="0" max="60" value="${safetyDays}"/>

            <button class="btn btn-primary mt-2" type="submit">Calculate</button>
          </form>
        </div>
      </div>
    </div>

    <div class="col-lg-8">
      <div class="alert alert-info mb-0">
        <div><b>Công thức nghiệp vụ:</b></div>
        <div>
          <b>expectedDemand</b> = <b>forecastPerDay</b> × (<b>leadTimeDays</b> + <b>bufferDays</b>)<br/>
          <b>safetyStock</b> = <b>forecastPerDay</b> × <b>safetyDays</b><br/>
          <b>reorderPoint</b> = <b>expectedDemand</b> + <b>safetyStock</b><br/>
          <b>suggestQty</b> = max(0, ceil(<b>reorderPoint</b> − <b>stock</b>))
        </div>
        <hr class="my-2"/>
        <div class="small">
          daysHistory=<c:out value="${daysHistory}"/>,
          leadTimeDays=<c:out value="${leadTimeDays}"/>,
          bufferDays=<c:out value="${bufferDays}"/>,
          safetyDays=<c:out value="${safetyDays}"/>
        </div>
      </div>
    </div>
  </div>

  <c:choose>
    <c:when test="${empty rows}">
      <div class="alert alert-warning">Chưa có dữ liệu để gợi ý nhập hàng.</div>
    </c:when>
    <c:otherwise>
      <div class="table-responsive">
        <table class="table table-bordered bg-white align-middle">
          <thead class="table-light">
          <tr>
            <th>Product</th>
            <th class="text-end">Avg7</th>
            <th class="text-end">Avg<c:out value="${daysHistory}"/></th>
            <th class="text-end">SeasonFactor</th>
            <th class="text-end">Forecast/day</th>
            <th class="text-end">Stock</th>
            <th class="text-end">ExpectedDemand</th>
            <th class="text-end">SafetyStock</th>
            <th class="text-end">ReorderPoint</th>
            <th class="text-center">Suggested</th>
          </tr>
          </thead>

          <tbody>
          <c:forEach var="r" items="${rows}">
            <c:set var="expectedDemand" value="${r.forecastPerDay * (leadTimeDays + bufferDays)}"/>
            <c:set var="safetyStock" value="${r.forecastPerDay * safetyDays}"/>
            <c:set var="reorderPoint" value="${expectedDemand + safetyStock}"/>

            <tr>
              <td><c:out value="${r.productName}"/></td>

              <td class="text-end"><fmt:formatNumber value="${r.avg7}" minFractionDigits="2" maxFractionDigits="2"/></td>
              <td class="text-end"><fmt:formatNumber value="${r.avg30}" minFractionDigits="2" maxFractionDigits="2"/></td>

              <td class="text-end"><fmt:formatNumber value="${r.seasonFactor}" minFractionDigits="2" maxFractionDigits="2"/></td>
              <td class="text-end"><fmt:formatNumber value="${r.forecastPerDay}" minFractionDigits="2" maxFractionDigits="2"/></td>

              <td class="text-end"><c:out value="${r.stock}"/></td>

              <td class="text-end"><fmt:formatNumber value="${expectedDemand}" minFractionDigits="2" maxFractionDigits="2"/></td>
              <td class="text-end"><fmt:formatNumber value="${safetyStock}" minFractionDigits="2" maxFractionDigits="2"/></td>
              <td class="text-end"><fmt:formatNumber value="${reorderPoint}" minFractionDigits="2" maxFractionDigits="2"/></td>

              <td class="text-center">
                <c:choose>
                  <c:when test="${r.suggestedQty > 0}">
                    <span class="badge text-bg-warning"><c:out value="${r.suggestedQty}"/></span>
                  </c:when>
                  <c:otherwise>
                    <span class="badge text-bg-success">OK</span>
                  </c:otherwise>
                </c:choose>
              </td>
            </tr>
          </c:forEach>
          </tbody>
        </table>
      </div>
    </c:otherwise>
  </c:choose>
</div>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>