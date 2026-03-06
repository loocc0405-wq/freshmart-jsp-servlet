<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="isEdit" value="${product != null && product.id != null}" />
<c:set var="pageTitle" value="${isEdit ? 'Edit Product' : 'Add Product'}" />
<jsp:include page="/WEB-INF/jsp/common/header.jsp"/>

<div class="d-flex justify-content-between align-items-center mb-3">
  <div>
    <h3 class="mb-0">${isEdit ? "Edit Product" : "Add Product"}</h3>
    <div class="text-muted">Enter product information below</div>
  </div>

  <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/staff/products">
    Back
  </a>
</div>

<div class="card">
  <div class="card-body">

    <!-- Error Messages -->
    <c:if test="${not empty errors}">
      <div class="alert alert-danger alert-dismissible fade show" role="alert">
        <i class="bi bi-exclamation-triangle me-2"></i>
        <strong>Validation Errors:</strong><br>
        ${errors}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
      </div>
    </c:if>

    <form action="${pageContext.request.contextPath}/staff/products"
          method="post"
          class="row g-3">

      <!-- Hidden ID (chỉ có khi edit) -->
      <c:if test="${isEdit}">
        <input type="hidden" name="id" value="${product.id}" />
      </c:if>

      <div class="col-md-6">
        <label class="form-label">Product Name <span class="text-danger">*</span></label>
        <input type="text"
               class="form-control"
               name="name"
               value="${product.name}"
               required
               autofocus>
      </div>

      <div class="col-md-6">
        <label class="form-label">Category</label>
        <input type="text"
               class="form-control"
               name="category"
               value="${product.category}"
               placeholder="e.g. Rau, Thịt, Cá">
      </div>

      <div class="col-md-4">
        <label class="form-label">Unit</label>
        <input type="text"
               class="form-control"
               name="unit"
               value="${product.unit}"
               placeholder="kg, bó, hộp...">
      </div>

      <div class="col-md-4">
        <label class="form-label">Sell Price <span class="text-danger">*</span></label>
        <input type="number"
               step="0.01"
               min="0"
               class="form-control"
               name="sellPrice"
               value="${product.sellPrice}"
               required>
        <div class="form-text">Example: 180000 or 180000.00</div>
      </div>

      <div class="col-md-4">
        <label class="form-label">Image URL</label>
        <input type="text"
               class="form-control"
               name="imageUrl"
               value="${product.imageUrl}"
               placeholder="https://example.com/image.jpg">
      </div>

      <!-- Preview ảnh (nếu có imageUrl) -->
      <c:if test="${not empty product.imageUrl}">
        <div class="col-12">
          <div class="border rounded-3 p-2 bg-white">
            <div class="small text-muted mb-2">Image preview</div>
            <img src="${product.imageUrl}" alt="preview"
                 style="max-height:180px; max-width:100%; object-fit:contain;">
          </div>
        </div>
      </c:if>

      <div class="col-12">
        <label class="form-label">Description</label>
        <textarea class="form-control"
                  name="description"
                  rows="3"
                  placeholder="Product description...">${product.description}</textarea>
      </div>

      <div class="col-12 d-flex gap-2 mt-2">
        <button type="submit" class="btn btn-primary">
          ${isEdit ? "Update Product" : "Create Product"}
        </button>

        <a href="${pageContext.request.contextPath}/staff/products"
           class="btn btn-outline-secondary">
          Cancel
        </a>
      </div>

    </form>
  </div>
</div>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>