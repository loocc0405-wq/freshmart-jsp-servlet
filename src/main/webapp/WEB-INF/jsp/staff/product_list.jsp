<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<c:set var="pageTitle" value="Products" />
<jsp:include page="/WEB-INF/jsp/common/header.jsp"/>

<div class="d-flex justify-content-between align-items-center mb-3">
  <div>
    <h3 class="mb-0">Products</h3>
    <div class="text-muted">Search, add, edit and manage products</div>
  </div>
  <!-- FIX: action=add -->
  <a class="btn btn-primary" href="${pageContext.request.contextPath}/staff/products?action=add">
    + Add Product
  </a>
</div>

<!-- Search card -->
<div class="card mb-3">
  <div class="card-body">
    <form class="row g-2 align-items-end"
          action="${pageContext.request.contextPath}/staff/products"
          method="get">

      <div class="col-md-5">
        <label class="form-label mb-1">Name</label>
        <input class="form-control" type="text" name="keyword"
               value="${keyword}" placeholder="Type keyword...">
      </div>

      <div class="col-md-4">
        <label class="form-label mb-1">Category</label>
        <input class="form-control" type="text" name="category"
               value="${category}" placeholder="e.g. Rau, Thịt, Cá...">
      </div>

      <div class="col-md-2">
        <div class="form-check mt-4">
          <input class="form-check-input" type="checkbox" name="showInactive" id="showInactive"
                 <c:if test="${showInactive}">checked</c:if> />
          <label class="form-check-label" for="showInactive">Include inactive</label>
        </div>
      </div>
      <div class="col-md-1 d-grid">
        <button class="btn btn-outline-primary" type="submit">Search</button>
      </div>
    </form>
  </div>
</div>

<!-- Pagination info -->
<c:if test="${totalItems > 0}">
  <div class="mb-2 text-muted">
    Showing ${(currentPage - 1) * pageSize + 1} to ${currentPage * pageSize > totalItems ? totalItems : currentPage * pageSize} of ${totalItems} products
  </div>
</c:if>

<!-- Product Health Guide -->
<div class="fm-health-guide mb-3">
  <div class="fm-health-guide__title">
    <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" fill="currentColor" viewBox="0 0 16 16" class="me-1" style="vertical-align:-1px">
      <path d="M8 15A7 7 0 1 1 8 1a7 7 0 0 1 0 14zm0 1A8 8 0 1 0 8 0a8 8 0 0 0 0 16z"/>
      <path d="m8.93 6.588-2.29.287-.082.38.45.083c.294.07.352.176.288.469l-.738 3.468c-.194.897.105 1.319.808 1.319.545 0 1.178-.252 1.465-.598l.088-.416c-.2.176-.492.246-.686.246-.275 0-.375-.193-.304-.533L8.93 6.588zM9 4.5a1 1 0 1 1-2 0 1 1 0 0 1 2 0z"/>
    </svg>
    Product Health Guide
  </div>
  <div class="fm-health-guide__items">
    <div class="fm-health-guide__item">
      <span class="badge bg-success fm-health-badge">Healthy</span>
      <span class="fm-health-guide__desc">Good stock, no near-expiry lots</span>
    </div>
    <div class="fm-health-guide__item">
      <span class="badge bg-info text-dark fm-health-badge">Low</span>
      <span class="fm-health-guide__desc">Stock available but limited (&le;10)</span>
    </div>
    <div class="fm-health-guide__item">
      <span class="badge bg-warning text-dark fm-health-badge">Expiry Risk</span>
      <span class="fm-health-guide__desc">Has lots expiring within 3 days</span>
    </div>
    <div class="fm-health-guide__item">
      <span class="badge bg-danger fm-health-badge">Out</span>
      <span class="fm-health-guide__desc">No usable stock remaining</span>
    </div>
  </div>
</div>

<!-- Table card -->
<div class="card">
  <div class="card-body">
    <div class="table-responsive">
      <table class="table table-hover align-middle">
        <thead class="table-light">
          <tr>
            <th style="width:60px;">ID</th>
            <th>Name</th>
            <th>Category</th>
            <th>Unit</th>
            <th class="text-end">Sell price</th>
            <th>Status</th>
            <th class="text-end">Stock</th>
            <th>Near Expiry</th>
            <th class="text-end">Avg Import</th>
            <th class="text-end">Margin</th>
            <th>Best Supplier</th>
            <th>Restock</th>
            <th class="text-end" style="width:150px;">Action</th>
          </tr>
        </thead>

        <tbody>
        <c:forEach var="p" items="${products}">
          <c:set var="h" value="${productHealthMap[p.id]}" />
          <tr>
            <td class="text-muted">${p.id}</td>
            <td class="fw-semibold">${p.name}</td>

            <td>
              <c:choose>
                <c:when test="${not empty p.category}">
                  <span class="badge text-bg-light border">${p.category}</span>
                </c:when>
                <c:otherwise><span class="text-muted">-</span></c:otherwise>
              </c:choose>
            </td>

            <td>${empty p.unit ? "-" : p.unit}</td>

            <td class="text-end">
              <fmt:formatNumber value="${p.sellPrice}" type="number" minFractionDigits="0" maxFractionDigits="0"/>
            </td>

            <td>
              <c:choose>
                <c:when test="${p.active}">
                  <span class="badge bg-success">Active</span>
                </c:when>
                <c:otherwise>
                  <span class="badge bg-secondary">Inactive</span>
                </c:otherwise>
              </c:choose>
            </td>

            <%-- Stock column --%>
            <td class="text-end">
              <c:choose>
                <c:when test="${h == null}">
                  <span class="text-muted">-</span>
                </c:when>
                <c:otherwise>
                  <div class="lh-1">
                    <div class="fw-semibold">${h.stock}</div>
                    <span class="badge ${h.healthBadgeCssClass} fm-health-badge mt-1">${h.healthBadgeLabel}</span>
                  </div>
                </c:otherwise>
              </c:choose>
            </td>

            <%-- Near Expiry column --%>
            <td>
              <c:choose>
                <c:when test="${h == null || !h.hasNearExpiry()}">
                  <span class="text-muted">-</span>
                </c:when>
                <c:otherwise>
                  <span class="badge bg-warning text-dark" title="${h.expiringLots} ${h.expiringLots == 1 ? 'lot' : 'lots'} expiring within 3 days">
                    ${h.expiringQty} units / ${h.expiringLots} ${h.expiringLots == 1 ? 'lot' : 'lots'}
                  </span>
                </c:otherwise>
              </c:choose>
            </td>

            <%-- Avg Import Price column --%>
            <td class="text-end">
              <c:choose>
                <c:when test="${h == null || h.avgImportPrice == null}">
                  <span class="text-muted">-</span>
                </c:when>
                <c:otherwise>
                  <fmt:formatNumber value="${h.avgImportPrice}" type="number" minFractionDigits="0" maxFractionDigits="0"/>
                </c:otherwise>
              </c:choose>
            </td>

            <%-- Estimated Margin column --%>
            <td class="text-end">
              <c:choose>
                <c:when test="${h == null || h.estimatedMargin == null}">
                  <span class="text-muted">-</span>
                </c:when>
                <c:when test="${h.hasNegativeMargin()}">
                  <span class="badge bg-danger bg-opacity-75" title="Negative margin - check pricing">
                    <fmt:formatNumber value="${h.estimatedMargin}" type="number" minFractionDigits="0" maxFractionDigits="0"/>
                  </span>
                </c:when>
                <c:otherwise>
                  <span class="text-success fw-semibold">
                    +<fmt:formatNumber value="${h.estimatedMargin}" type="number" minFractionDigits="0" maxFractionDigits="0"/>
                  </span>
                </c:otherwise>
              </c:choose>
            </td>

            <%-- Best Supplier column --%>
            <td>
              <c:choose>
                <c:when test="${h == null || !h.hasSupplierRecommendation()}">
                  <span class="text-muted">-</span>
                </c:when>
                <c:otherwise>
                  <div class="small" title="${h.recommendationReason}">
                    <div class="fw-semibold">${h.recommendedSupplierName}</div>
                    <div class="text-muted">
                      <c:if test="${h.recommendedSupplierLeadTimeDays != null}">LT: ${h.recommendedSupplierLeadTimeDays}d</c:if>
                      <c:if test="${h.recommendedSupplierLeadTimeDays != null && h.recommendedSupplierAvgImportPrice != null}"> | </c:if>
                      <c:if test="${h.recommendedSupplierAvgImportPrice != null}">Avg: <fmt:formatNumber value="${h.recommendedSupplierAvgImportPrice}" type="number" minFractionDigits="0" maxFractionDigits="0"/></c:if>
                    </div>
                  </div>
                </c:otherwise>
              </c:choose>
            </td>

            <%-- Restock Action column --%>
            <td>
              <c:choose>
                <c:when test="${h != null && h.hasSupplierRecommendation()}">
                  <a class="btn btn-sm btn-outline-primary"
                     href="${pageContext.request.contextPath}/staff/import-lot?productId=${p.id}&supplierId=${h.recommendedSupplierId}">
                    Restock
                  </a>
                </c:when>
                <c:otherwise>
                  <a class="btn btn-sm btn-outline-secondary"
                     href="${pageContext.request.contextPath}/staff/import-lot?productId=${p.id}">
                    Restock
                  </a>
                </c:otherwise>
              </c:choose>
            </td>

            <td class="text-end">
              <a class="btn btn-sm btn-outline-secondary"
                 href="${pageContext.request.contextPath}/staff/products?action=edit&id=${p.id}">
                Edit
              </a>

              <!-- Delete POST: khớp servlet mới -->
              <form class="d-inline"
                    action="${pageContext.request.contextPath}/staff/products"
                    method="post"
                    onsubmit="return confirm('Delete this product?');">
                <input type="hidden" name="csrf_token" value="${sessionScope.CSRF_TOKEN}" />
                <input type="hidden" name="action" value="delete">
                <input type="hidden" name="id" value="${p.id}">
                <button class="btn btn-sm btn-outline-danger" type="submit">Delete</button>
              </form>
            </td>
          </tr>
        </c:forEach>

        <c:if test="${empty products}">
          <tr>
            <td colspan="13" class="text-center text-muted py-4">
              No products found.
            </td>
          </tr>
        </c:if>
        </tbody>
      </table>
    </div>
  </div>
</div>

<!-- Pagination controls -->
<c:if test="${totalPages > 1}">
  <nav aria-label="Product pagination" class="mt-3">
    <ul class="pagination justify-content-center">
      <!-- Previous button -->
      <li class="page-item ${currentPage == 1 ? 'disabled' : ''}">
        <a class="page-link" 
           href="${pageContext.request.contextPath}/staff/products?page=${currentPage - 1}&keyword=${keyword}&category=${category}&showInactive=${showInactive}">
          Previous
        </a>
      </li>

      <!-- Page numbers -->
      <c:choose>
        <c:when test="${totalPages <= 7}">
          <!-- Show all pages if 7 or fewer -->
          <c:forEach var="i" begin="1" end="${totalPages}">
            <li class="page-item ${i == currentPage ? 'active' : ''}">
              <a class="page-link" 
                 href="${pageContext.request.contextPath}/staff/products?page=${i}&keyword=${keyword}&category=${category}&showInactive=${showInactive}">
                ${i}
              </a>
            </li>
          </c:forEach>
        </c:when>
        <c:otherwise>
          <!-- Show first page -->
          <li class="page-item ${1 == currentPage ? 'active' : ''}">
            <a class="page-link" 
               href="${pageContext.request.contextPath}/staff/products?page=1&keyword=${keyword}&category=${category}&showInactive=${showInactive}">
              1
            </a>
          </li>

          <!-- Show ellipsis if needed -->
          <c:if test="${currentPage > 3}">
            <li class="page-item disabled"><span class="page-link">...</span></li>
          </c:if>

          <!-- Show pages around current page -->
          <c:forEach var="i" begin="${currentPage - 1}" end="${currentPage + 1}">
            <c:if test="${i > 1 && i < totalPages}">
              <li class="page-item ${i == currentPage ? 'active' : ''}">
                <a class="page-link" 
                   href="${pageContext.request.contextPath}/staff/products?page=${i}&keyword=${keyword}&category=${category}&showInactive=${showInactive}">
                  ${i}
                </a>
              </li>
            </c:if>
          </c:forEach>

          <!-- Show ellipsis if needed -->
          <c:if test="${currentPage < totalPages - 2}">
            <li class="page-item disabled"><span class="page-link">...</span></li>
          </c:if>

          <!-- Show last page -->
          <li class="page-item ${totalPages == currentPage ? 'active' : ''}">
            <a class="page-link" 
               href="${pageContext.request.contextPath}/staff/products?page=${totalPages}&keyword=${keyword}&category=${category}&showInactive=${showInactive}">
              ${totalPages}
            </a>
          </li>
        </c:otherwise>
      </c:choose>

      <!-- Next button -->
      <li class="page-item ${currentPage == totalPages ? 'disabled' : ''}">
        <a class="page-link" 
           href="${pageContext.request.contextPath}/staff/products?page=${currentPage + 1}&keyword=${keyword}&category=${category}&showInactive=${showInactive}">
          Next
        </a>
      </li>
    </ul>
  </nav>
</c:if>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>