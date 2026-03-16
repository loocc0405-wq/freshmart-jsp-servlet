<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="pageTitle" value="Initialize SKU | FreshMart Enterprise"/>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"/>

<div class="container py-5">
    <div class="row justify-content-center">
        <div class="col-xl-9">
            <div class="fm-page-header mb-5 border-bottom pb-4">
                <div>
                    <div class="fm-caption fw-bold text-primary mb-1 text-uppercase ls-wide">Catalog Growth</div>
                    <h1 class="fm-page-title">Initialize New Service SKU</h1>
                    <p class="fm-page-subtitle">Registering metadata and logistics attributes for a new system-wide product offering.</p>
                </div>
                <a href="${pageContext.request.contextPath}/admin/products" class="fm-btn btn-light border h-100">
                    <i class="bi bi-arrow-left me-1"></i> Return to SKU Master
                </a>
            </div>

            <c:if test="${error != null}">
                <div class="alert alert-danger fm-surface border-0 shadow-sm mb-5 p-3"><i class="bi bi-exclamation-octagon-fill me-2"></i> ${error}</div>
            </c:if>

            <div class="fm-surface p-0 overflow-hidden shadow-sm border-0">
                <form method="post" class="p-4 p-md-5">
                    <input type="hidden" name="csrf_token" value="${sessionScope.CSRF_TOKEN}" />
                    
                    <div class="row g-5">
                        <div class="col-lg-7">
                            <h3 class="fm-h3 mb-4 border-bottom pb-2">Core Product Metadata</h3>
                            <div class="mb-4">
                                <label class="fm-caption fw-bold d-block mb-2">Display Registry Name</label>
                                <input type="text" name="name" class="fm-form-control py-3" required placeholder="e.g. Premium Norwegian Salmon Fillet">
                            </div>

                            <div class="row g-4 mb-4">
                                <div class="col-md-6">
                                    <label class="fm-caption fw-bold d-block mb-2">Operational Department</label>
                                    <input type="text" name="category" class="fm-form-control" placeholder="Seafood, Produce, etc.">
                                </div>
                                <div class="col-md-6">
                                    <label class="fm-caption fw-bold d-block mb-2">Logistics Unit</label>
                                    <input type="text" name="unit" class="fm-form-control" placeholder="kg, pack, unit">
                                </div>
                            </div>

                            <div class="mb-4">
                                <label class="fm-caption fw-bold d-block mb-2">Global Price Index (₫)</label>
                                <input type="number" step="0.01" name="sellPrice" class="fm-form-control py-3 fw-bold text-primary" required placeholder="0.00">
                            </div>

                            <div class="mb-0">
                                <label class="fm-caption fw-bold d-block mb-2">Operational Narrative / Description</label>
                                <textarea name="description" class="fm-form-control" style="min-height: 150px;" placeholder="Detailed specification and quality attributes..."></textarea>
                            </div>
                        </div>

                        <div class="col-lg-5 bg-light p-4 p-md-5">
                            <h3 class="fm-h3 mb-4 border-bottom pb-2 text-dark">Logistics Media</h3>
                            <div class="mb-5">
                                <label class="fm-caption fw-bold d-block mb-2">Media Attribution URL</label>
                                <input type="text" name="imageUrl" class="fm-form-control" placeholder="https://cloud.cdn/image.jpg">
                                <p class="small text-muted mt-2">Specify a high-resolution endpoint for the procurement catalog.</p>
                            </div>

                            <div class="p-4 bg-white rounded-4 border-0 shadow-xs mb-5">
                                <h5 class="fm-caption fw-bold text-primary mb-3">System Compliance</h5>
                                <div class="d-flex align-items-center gap-2 mb-2 small fw-medium">
                                    <i class="bi bi-shield-check text-success"></i> Unique SKU Identification
                                </div>
                                <div class="d-flex align-items-center gap-2 mb-0 small fw-medium">
                                    <i class="bi bi-shield-check text-success"></i> Search Engine Optimized
                                </div>
                            </div>

                            <button type="submit" class="fm-btn fm-btn-primary w-100 py-3 shadow-sm fs-5">Commit SKU Entry</button>
                        </div>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>