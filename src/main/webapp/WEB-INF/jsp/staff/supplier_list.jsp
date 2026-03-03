<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="pageTitle" value="Suppliers" />
<%@ include file="_layout_top.jspf" %>

<div class="d-flex justify-content-between align-items-center mb-3">
  <div>
    <h3 class="mb-0">Suppliers</h3>
    <div class="text-muted">Manage supplier info</div>
  </div>
  <a class="btn btn-primary" href="${pageContext.request.contextPath}/staff/suppliers?action=new">
    + Add Supplier
  </a>
</div>

<div class="card">
  <div class="card-body">
    <div class="table-responsive">
      <table class="table table-hover align-middle">
        <thead class="table-light">
          <tr>
            <th>ID</th>
            <th>Name</th>
            <th>Phone</th>
            <th>Email</th>
            <th>Address</th>
            <th class="text-end">Action</th>
          </tr>
        </thead>
        <tbody>
        <c:forEach var="s" items="${suppliers}">
          <tr>
            <td class="text-muted">${s.id}</td>
            <td class="fw-semibold">${s.name}</td>
            <td>${empty s.phone ? "-" : s.phone}</td>
            <td>${empty s.email ? "-" : s.email}</td>
            <td>${empty s.address ? "-" : s.address}</td>
            <td class="text-end">
              <a class="btn btn-sm btn-outline-secondary"
                 href="${pageContext.request.contextPath}/staff/suppliers?action=edit&id=${s.id}">
                Edit
              </a>

              <!-- Delete bằng POST cho chuyên nghiệp -->
              <form class="d-inline"
                    action="${pageContext.request.contextPath}/staff/suppliers"
                    method="post"
                    onsubmit="return confirm('Delete this supplier?');">
                <input type="hidden" name="action" value="delete">
                <input type="hidden" name="id" value="${s.id}">
                <button class="btn btn-sm btn-outline-danger" type="submit">Delete</button>
              </form>
            </td>
          </tr>
        </c:forEach>

        <c:if test="${empty suppliers}">
          <tr>
            <td colspan="6" class="text-center text-muted py-4">No suppliers found.</td>
          </tr>
        </c:if>
        </tbody>
      </table>
    </div>
  </div>
</div>

<%@ include file="_layout_bottom.jspf" %>