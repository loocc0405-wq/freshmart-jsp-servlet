<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:set var="pageTitle" value="Gợi ý nhập hàng (Rule-based)"/>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"/>

<c:if test="${empty bufferDays}">
  <c:set var="bufferDays" value="0"/>
</c:if>

<div class="container my-4">
  <div class="d-flex justify-content-between align-items-center mb-2">
    <h3>Gợi ý nhập hàng (Rule-based)</h3>
  </div>

  <div class="alert alert-info">
    <div><b>Công thức:</b></div>
    <div>
      <b>forecastPerDay</b> = <b>avg7</b> × <b>trend</b> × <b>seasonFactor</b><br/>
      <b>expectedDemand</b> = <b>forecastPerDay</b> × (<b>leadTimeDays</b> + <b>bufferDays</b>)<br/>
      <b>safetyStock</b> = <b>forecastPerDay</b> × <b>safetyDays</b><br/>
      <b>reorderPoint</b> = <b>expectedDemand</b> + <b>safetyStock</b><br/>
      <b>suggest</b> = max(0, ceil(<b>reorderPoint</b> − <b>stock</b>))
    </div>
    <hr class="my-2"/>
    <div class="small">
      daysHistory=<c:out value="${daysHistory}"/>,
      leadTimeDays=<c:out value="${leadTimeDays}"/>,
      bufferDays=<c:out value="${bufferDays}"/>,
      safetyDays=<c:out value="${safetyDays}"/>
    </div>
  </div>

  <c:choose>
    <c:when test="${empty rows}">
      <div class="alert alert-warning">Chưa có dữ liệu gợi ý. Hãy đảm bảo có sản phẩm + đơn COMPLETED + tồn kho.</div>
    </c:when>
    <c:otherwise>
      <div class="table-responsive">
        <table class="table table-bordered bg-white align-middle">
          <thead class="table-light">
          <tr>
            <th>Product</th>
            <th class="text-end">Avg7<br/><span class="small text-muted">(qty/day)</span></th>
            <th class="text-end">Avg<c:out value="${daysHistory}"/><br/><span class="small text-muted">(qty/day)</span></th>
            <th class="text-end">SeasonFactor</th>
            <th class="text-end">Forecast/day</th>
            <th class="text-end">Stock</th>
            <th class="text-end">ExpectedDemand</th>
            <th class="text-end">SafetyStock</th>
            <th class="text-end">ReorderPoint</th>
            <th class="text-center">Suggested Import</th>
            <th>Best Supplier</th>
            <th class="text-end">Lead Time</th>
            <th class="text-end">Avg Price</th>
            <th>Last Import</th>
            <th>Action</th>
          </tr>
          </thead>

          <tbody>
          <c:forEach var="r" items="${rows}">
            <tr>
              <td><c:out value="${r.productName}"/></td>

              <td class="text-end">
                <fmt:formatNumber value="${r.avg7}" minFractionDigits="2" maxFractionDigits="2"/>
              </td>

              <td class="text-end">
                <fmt:formatNumber value="${r.avg30}" minFractionDigits="2" maxFractionDigits="2"/>
              </td>

              <td class="text-end">
                <fmt:formatNumber value="${r.seasonFactor}" minFractionDigits="2" maxFractionDigits="2"/>
              </td>

              <td class="text-end">
                <fmt:formatNumber value="${r.forecastPerDay}" minFractionDigits="2" maxFractionDigits="2"/>
              </td>

              <td class="text-end"><c:out value="${r.stock}"/></td>

              <td class="text-end">
                <fmt:formatNumber value="${r.expectedDemand}" minFractionDigits="2" maxFractionDigits="2"/>
              </td>

              <td class="text-end">
                <fmt:formatNumber value="${r.safetyStock}" minFractionDigits="2" maxFractionDigits="2"/>
              </td>

              <td class="text-end">
                <fmt:formatNumber value="${r.reorderPoint}" minFractionDigits="2" maxFractionDigits="2"/>
              </td>

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

              <td>
                <c:choose>
                  <c:when test="${not empty r.recommendedSupplierName}">
                    <c:out value="${r.recommendedSupplierName}"/>
                  </c:when>
                  <c:otherwise>
                    <span class="text-muted small">-</span>
                  </c:otherwise>
                </c:choose>
              </td>

              <td class="text-end">
                <c:choose>
                  <c:when test="${not empty r.recommendedSupplierLeadTimeDays}">
                    <c:out value="${r.recommendedSupplierLeadTimeDays}"/>d
                  </c:when>
                  <c:otherwise>
                    <span class="text-muted small">-</span>
                  </c:otherwise>
                </c:choose>
              </td>

              <td class="text-end">
                <c:choose>
                  <c:when test="${not empty r.recommendedSupplierAvgImportPrice}">
                    <fmt:formatNumber value="${r.recommendedSupplierAvgImportPrice}" minFractionDigits="0" maxFractionDigits="0"/>
                  </c:when>
                  <c:otherwise>
                    <span class="text-muted small">-</span>
                  </c:otherwise>
                </c:choose>
              </td>

              <td>
                <c:choose>
                  <c:when test="${not empty r.recommendedSupplierLastImportDate}">
                    <c:out value="${r.recommendedSupplierLastImportDate}"/>
                  </c:when>
                  <c:otherwise>
                    <span class="text-muted small">-</span>
                  </c:otherwise>
                </c:choose>
              </td>

              <td>
                <c:choose>
                  <c:when test="${r.suggestedQty > 0 and not empty r.recommendedSupplierId}">
                    <a href="${pageContext.request.contextPath}/staff/import-lot?productId=${r.productId}&supplierId=${r.recommendedSupplierId}" 
                       class="btn btn-sm btn-primary">Nhập lô với NCC này</a>
                  </c:when>
                  <c:otherwise>
                    <span class="text-muted small">-</span>
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