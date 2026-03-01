<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Gợi ý nhập hàng (Rule-based)"/>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"/>

<div class="container my-4">
  <div class="d-flex justify-content-between align-items-center mb-2">
    <h3>Gợi ý nhập hàng (Rule-based)</h3>
  </div>

  <div class="alert alert-info">
    Công thức:
    <b>forecastPerDay = avg7 * trend * seasonFactor</b>,
    <b>suggest = ceil(forecastPerDay*(leadTime+safety)) - stock</b>.
    <br/>
    daysHistory=${daysHistory}, leadTimeDays=${leadTimeDays}, safetyDays=${safetyDays}
  </div>

  <table class="table table-bordered bg-white">
    <thead>
    <tr>
      <th>Product</th>
      <th>Avg7 (qty/day)</th>
      <th>Avg${daysHistory} (qty/day)</th>
      <th>SeasonFactor</th>
      <th>Forecast/day</th>
      <th>Stock</th>
      <th>Suggested Import</th>
    </tr>
    </thead>
    <tbody>
    <c:forEach var="r" items="${rows}">
      <tr>
        <td><c:out value="${r.productName}"/></td>
        <td><c:out value="${r.avg7}"/></td>
        <td><c:out value="${r.avg30}"/></td>
        <td><c:out value="${r.seasonFactor}"/></td>
        <td><c:out value="${r.forecastPerDay}"/></td>
        <td><c:out value="${r.stock}"/></td>
        <td>
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

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>