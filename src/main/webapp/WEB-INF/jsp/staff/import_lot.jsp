<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:set var="pageTitle" value="Inventory Logistics - Import Batch"/>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"/>

<c:set var="isEdit" value="${editingLot != null}" />

<div class="container py-5">
    <div class="row justify-content-center">
        <!-- Main Form Column -->
        <div class="col-lg-8 col-xl-7">
            <div class="fm-surface p-5 shadow-sm">
                <div class="mb-5 d-flex align-items-center justify-content-between">
                    <div>
                        <h1 class="fm-h1 mb-1">${isEdit ? 'Modify Batch Data' : 'Initialize New Batch'}</h1>
                        <p class="fm-text-secondary mb-0">Record fresh produce intake with precise FEFO attributes.</p>
                    </div>
                    <div class="bg-primary-subtle text-primary p-3 rounded-circle d-none d-sm-block">
                        <i class="bi bi-box-arrow-in-right fs-3"></i>
                    </div>
                </div>

                <c:if test="${not empty successMessage}"><div class="alert alert-success border-0 bg-success-subtle mb-4"><i class="bi bi-check-circle me-2"></i><c:out value="${successMessage}"/></div></c:if>
                <c:if test="${not empty errorMessage}"><div class="alert alert-danger border-0 bg-danger-subtle mb-4"><i class="bi bi-exclamation-octagon me-2"></i><c:out value="${errorMessage}"/></div></c:if>

                <form method="post" action="${pageContext.request.contextPath}/staff/import-lot">
                    <input type="hidden" name="csrf_token" value="${sessionScope.CSRF_TOKEN}" />
                    <c:if test="${isEdit}"><input type="hidden" name="lotId" value="${editingLot.id}" /></c:if>

                    <!-- Section: Product Identity -->
                    <div class="mb-5 border rounded-3 p-4 bg-light-subtle">
                        <h6 class="fw-bold text-uppercase small opacity-50 mb-4 ls-wide">Product Attribution</h6>
                        <div class="mb-4">
                            <label class="form-label fw-bold small text-uppercase">SKU / Product Master *</label>
                            <c:choose>
                                <c:when test="${isEdit && editingLot.qtyIn != editingLot.qtyLeft}">
                                    <input type="hidden" name="productId" value="${editingLot.product.id}"/>
                                    <select class="fm-form-control bg-light" disabled>
                                        <c:forEach items="${products}" var="p">
                                            <option value="${p.id}" ${editingLot.product.id == p.id ? 'selected' : ''}><c:out value="${p.name}"/></option>
                                        </c:forEach>
                                    </select>
                                    <div class="fm-caption mt-2 text-warning fw-semibold"><i class="bi bi-lock-fill"></i> Product mapping locked as batch has active shipments.</div>
                                </c:when>
                                <c:otherwise>
                                    <select class="fm-form-control" name="productId" required>
                                        <option value="">Select Target Product...</option>
                                        <c:forEach items="${products}" var="p">
                                            <option value="${p.id}" ${isEdit ? (editingLot.product.id == p.id ? 'selected' : '') : (formProductId == p.id ? 'selected' : '')}>
                                                <c:out value="${p.name}"/> ${!p.active ? '[Inactive]' : ''}
                                            </option>
                                        </c:forEach>
                                    </select>
                                </c:otherwise>
                            </c:choose>
                        </div>
                        <div>
                            <label class="form-label fw-bold small text-uppercase">Source Supplier</label>
                            <select class="fm-form-control" name="supplierId">
                                <option value="">Unknown / N/A</option>
                                <c:forEach items="${suppliers}" var="s">
                                    <option value="${s.id}" ${isEdit && editingLot.supplier != null && editingLot.supplier.id == s.id ? 'selected' : ''}>
                                        <c:out value="${s.name}"/>
                                    </option>
                                </c:forEach>
                            </select>
                        </div>
                    </div>

                    <!-- Section: Logistics & Expiry -->
                    <div class="mb-5">
                        <h6 class="fw-bold text-uppercase small opacity-50 mb-4 ls-wide">FEFO Logistics</h6>
                        <div class="row g-4">
                            <div class="col-md-6">
                                <label class="form-label fw-bold small text-uppercase">Intake Date *</label>
                                <input type="date" class="fm-form-control" name="importDate" value="${isEdit ? editingLot.importDate : formImportDate}" required>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label fw-bold small text-uppercase text-danger">Expiry Deadline *</label>
                                <input type="date" class="fm-form-control border-danger-subtle" name="expiryDate" value="${isEdit ? editingLot.expiryDate : formExpiryDate}" required>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label fw-bold small text-uppercase">Intake Quantity *</label>
                                <div class="input-group">
                                    <input type="number" class="fm-form-control" name="quantity" min="1" value="${isEdit ? editingLot.qtyIn : formQuantity}" required>
                                    <span class="input-group-text bg-white small text-muted border-start-0" style="border-radius: 0 var(--fm-radius-control) var(--fm-radius-control) 0;">Units</span>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label fw-bold small text-uppercase">Unit Acquisition Cost</label>
                                <div class="input-group">
                                    <span class="input-group-text bg-white small text-muted border-end-0" style="border-radius: var(--fm-radius-control) 0 0 var(--fm-radius-control);">$</span>
                                    <input type="number" step="0.01" class="fm-form-control" name="importPrice" min="0" value="${isEdit ? editingLot.importPrice : formImportPrice}">
                                </div>
                            </div>
                        </div>
                    </div>

                    <c:if test="${isEdit && editingLot.qtyIn != editingLot.qtyLeft}">
                        <div class="alert alert-info border-0 bg-info-subtle mb-5 small">
                            <i class="bi bi-info-circle-fill me-2"></i>Note: Adjusting total intake will preserve current consumption levels. Qty Left will be recalculated.
                        </div>
                    </c:if>

                    <div class="d-grid gap-2">
                        <button type="submit" class="fm-btn fm-btn-primary py-3 mb-2">
                            ${isEdit ? 'Commit Updates' : 'Commit New Batch'} <i class="bi bi-chevron-right ms-2"></i>
                        </button>
                        <a href="${pageContext.request.contextPath}/staff" class="fm-btn btn-light border py-3 text-muted">Discard & Return</a>
                    </div>
                </form>
            </div>
        </div>

        <!-- Sidebar / Context Column -->
        <div class="col-lg-4 d-none d-lg-block">
            <div class="fm-surface p-4 mb-4">
                <h6 class="fw-bold text-uppercase small ls-wide mb-3">FEFO Protocol</h6>
                <p class="small text-muted mb-4">First-Expired, First-Out ensures maximum product freshness and mitigates waste in high-turnover grocery environments.</p>
                
                <div class="d-flex gap-3 mb-3">
                    <div class="text-primary"><i class="bi bi-award fs-5"></i></div>
                    <div class="small fw-semibold">Quality Validation</div>
                </div>
                <div class="d-flex gap-3 mb-3">
                    <div class="text-primary"><i class="bi bi-calendar-check fs-5"></i></div>
                    <div class="small fw-semibold">Expiry Tracking</div>
                </div>
                <div class="d-flex gap-3 mb-0">
                    <div class="text-primary"><i class="bi bi-shield-check fs-5"></i></div>
                    <div class="small fw-semibold">Audit Visibility</div>
                </div>
            </div>

            <div class="fm-card bg-primary-subtle border-0">
                <h6 class="fw-bold mb-3">Need Assistance?</h6>
                <p class="small mb-3">Contact the warehouse logistics desk if you encounter SKU mismatch errors or supplier identification issues.</p>
                <div class="d-flex align-items-center gap-2 small fw-bold">
                    <i class="bi bi-telephone"></i> Ext. 405
                </div>
            </div>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
