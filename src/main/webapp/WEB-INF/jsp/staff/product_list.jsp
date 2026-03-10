<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

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
        <!-- FIX: name=keyword, value lấy từ requestScope.keyword -->
        <input class="form-control" type="text" name="keyword"
               value="${keyword}" placeholder="Type keyword...">
      </div>

      <div class="col-md-5">
        <label class="form-label mb-1">Category</label>
        <input class="form-control" type="text" name="category"
               value="${category}" placeholder="e.g. Rau, Thịt, Cá...">
      </div>

      <div class="col-md-2">
        <div class="form-check">
          <input class="form-check-input" type="checkbox" name="showInactive" id="showInactive"
                 <c:if test="${showInactive}">checked</c:if> />
          <label class="form-check-label" for="showInactive">Include inactive</label>
        </div>
      </div>
      <div class="col-md-2 d-grid">
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

<!-- Table card -->
<div class="card">
  <div class="card-body">
    <div class="table-responsive">
      <table class="table table-hover align-middle">
        <thead class="table-light">
          <tr>
            <th style="width:70px;">ID</th>
            <th>Name</th>
            <th>Category</th>
            <th>Unit</th>
            <th class="text-end">Sell price</th>
            <th>Status</th>
            <th>Image</th>
            <th>Description</th>
            <th class="text-end" style="width:180px;">Action</th>
          </tr>
        </thead>

        <tbody>
        <c:forEach var="p" items="${products}">
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

            <td>
              <c:choose>
                <c:when test="${not empty p.imageUrl}">
                  <a href="${p.imageUrl}" target="_blank" class="text-decoration-none">View</a>
                </c:when>
                <c:otherwise><span class="text-muted">-</span></c:otherwise>
              </c:choose>
            </td>

            <td>
              <c:choose>
                <c:when test="${not empty p.description}">
                  <span>${p.description}</span>
                </c:when>
                <c:otherwise><span class="text-muted">-</span></c:otherwise>
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
            <td colspan="9" class="text-center text-muted py-4">
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