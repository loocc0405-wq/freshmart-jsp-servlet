<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:set var="pageTitle" value="Account Security & Identity | FreshMart Enterprise"/>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"/>

<c:set var="form" value="${empty formData ? profileUser : formData}" />

<div class="container py-5">
    <div class="row justify-content-center">
        <div class="col-xl-9">
            <div class="fm-page-header mb-5">
                <div>
                    <div class="fm-caption fw-bold text-primary mb-1 text-uppercase">Account Management</div>
                    <h1 class="fm-page-title">Identity & Profile Security</h1>
                    <p class="fm-page-subtitle">Manage your personal attributes and authentication metadata.</p>
                </div>
            </div>

            <c:if test="${not empty successMessage}">
                <div class="alert alert-success fm-surface border-0 shadow-sm mb-4"><i class="bi bi-check-circle-fill me-2"></i> ${fn:escapeXml(successMessage)}</div>
            </c:if>

            <c:if test="${not empty errorMessage}">
                <div class="alert alert-danger fm-surface border-0 shadow-sm mb-4"><i class="bi bi-exclamation-octagon-fill me-2"></i> ${fn:escapeXml(errorMessage)}</div>
            </c:if>

            <div class="fm-surface p-0 overflow-hidden shadow-sm border-0">
                <div class="row g-0">
                    <!-- Left Sidebar Info -->
                    <div class="col-lg-4 bg-light border-end p-4">
                        <div class="text-center mb-4">
                            <div class="bg-primary text-white rounded-circle d-inline-flex align-items-center justify-content-center mb-3 shadow-sm" style="width: 80px; height: 80px;">
                                <i class="bi bi-person fs-1"></i>
                            </div>
                            <h5 class="fw-bold mb-1">${fn:escapeXml(profileUser.username)}</h5>
                            <div class="small text-muted font-monospace">${fn:escapeXml(profileUser.email)}</div>
                        </div>
                        
                        <div class="mt-5">
                            <div class="fm-caption fw-bold opacity-50 mb-3 text-uppercase">Security Attributes</div>
                            <div class="d-flex align-items-center gap-2 mb-3">
                                <i class="bi bi-shield-lock text-success"></i>
                                <span class="small fw-semibold">Encrypted Storage</span>
                            </div>
                            <div class="d-flex align-items-center gap-2 mb-0">
                                <i class="bi bi-patch-check text-primary"></i>
                                <span class="small fw-semibold">Identity Verified</span>
                            </div>
                        </div>
                    </div>

                    <!-- Right Form Area -->
                    <div class="col-lg-8 p-4 p-md-5">
                        <form method="post" action="${pageContext.request.contextPath}/customer/profile">
                            <input type="hidden" name="csrf_token" value="${sessionScope.CSRF_TOKEN}" />

                            <div class="mb-5 pb-4 border-bottom">
                                <h3 class="fm-h3 mb-4">Identity Information</h3>
                                <div class="row g-4">
                                    <div class="col-12">
                                        <label class="fm-caption fw-bold d-block mb-2">Display Full Name</label>
                                        <input type="text" name="fullName" class="fm-form-control" value="${fn:escapeXml(form.fullName)}" placeholder="Enterprise Legal Name">
                                    </div>
                                    <div class="col-md-6">
                                        <label class="fm-caption fw-bold d-block mb-2">Gender Category</label>
                                        <select name="gender" class="fm-form-control">
                                            <option value="">-- Classified --</option>
                                            <option value="MALE"   ${form.gender != null && form.gender == 'MALE'   ? 'selected' : ''}>Male</option>
                                            <option value="FEMALE" ${form.gender != null && form.gender == 'FEMALE' ? 'selected' : ''}>Female</option>
                                            <option value="OTHER"  ${form.gender != null && form.gender == 'OTHER'  ? 'selected' : ''}>Other</option>
                                        </select>
                                    </div>
                                    <div class="col-md-6">
                                        <label class="fm-caption fw-bold d-block mb-2">Date of Birth</label>
                                        <input type="date" name="dob" class="fm-form-control" value="${fn:escapeXml(form.dob)}">
                                    </div>
                                </div>
                            </div>

                            <div class="mb-5">
                                <h3 class="fm-h3 mb-4">Communication Details</h3>
                                <div class="row g-4">
                                    <div class="col-12">
                                        <label class="fm-caption fw-bold d-block mb-2">Primary Hub Phone</label>
                                        <input type="text" name="phone" class="fm-form-control" value="${fn:escapeXml(form.phone)}" placeholder="+84 XXX XXX XXX">
                                    </div>
                                    <div class="col-12">
                                        <label class="fm-caption fw-bold d-block mb-2">Delivery Ledger Address</label>
                                        <textarea name="address" class="fm-form-control" style="min-height: 120px;" placeholder="Full logistic destination address">${fn:escapeXml(form.address)}</textarea>
                                    </div>
                                </div>
                            </div>

                            <div class="d-flex align-items-center justify-content-between pt-4">
                                <div class="text-muted small">
                                    <i class="bi bi-info-circle me-1"></i> Updates apply globally to logistics.
                                </div>
                                <button type="submit" class="fm-btn fm-btn-primary px-5 py-3 shadow-sm">Commit Profile Changes</button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>