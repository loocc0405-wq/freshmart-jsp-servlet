<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="pageTitle" value="${supplier == null ? 'Add Supplier' : 'Edit Supplier'}" />
<jsp:include page="/WEB-INF/jsp/common/header.jsp"/>

<div class="d-flex justify-content-between align-items-center mb-3">
  <div>
    <h3 class="mb-0">${supplier == null ? "Add Supplier" : "Edit Supplier"}</h3>
    <div class="text-muted">Fill in supplier details</div>
  </div>
  <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/staff/suppliers">Back</a>
</div>

<div class="card">
  <div class="card-body">
    <form action="${pageContext.request.contextPath}/staff/suppliers" method="post" class="row g-3">

      <!-- CSRF TOKEN (thêm) -->
      <input type="hidden" name="csrf_token" value="${sessionScope.CSRF_TOKEN}" />

      <input type="hidden" name="id" value="${supplier.id}" />

      <div class="col-md-6">
        <label class="form-label">Name</label>
        <input class="form-control" type="text" name="name" value="${supplier.name}" required>
      </div>

      <div class="col-md-6">
        <label class="form-label">Phone</label>
        <input class="form-control" type="text" name="phone" value="${supplier.phone}" required>
      </div>

      <div class="col-md-6">
        <label class="form-label">Email</label>
        <input class="form-control" type="email" name="email" value="${supplier.email}" required>
      </div>

      <div class="col-md-6">
        <label class="form-label">Address</label>
        <input class="form-control" type="text" name="address" value="${supplier.address}" required>
      </div>

      <div class="col-md-6">
        <label class="form-label">Certificate</label>
        <input class="form-control" type="text" name="certificate" value="${supplier.certificate}">
      </div>

      <div class="col-md-6">
        <label class="form-label">Lead time (days)</label>
        <input class="form-control" type="number" min="1" name="leadTimeDays" value="${supplier.leadTimeDays}" required>
      </div>

      <div class="col-12">
        <label class="form-label">Note</label>
        <textarea class="form-control" name="note" rows="3">${supplier.note}</textarea>
      </div>

      <div class="col-12 d-flex gap-2">
        <button class="btn btn-primary" type="submit">Save</button>
        <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/staff/suppliers">Cancel</a>
      </div>

    </form>
  </div>
</div>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>