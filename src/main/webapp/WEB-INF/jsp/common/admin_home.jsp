<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<c:set var="pageTitle" value="System Administration | FreshMart Enterprise"/>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"/>

<div class="container-fluid px-4 py-4">
    <div class="fm-page-header mb-5 border-bottom pb-4">
        <div>
            <div class="fm-caption fw-bold text-primary mb-1 text-uppercase ls-wide">Central Command</div>
            <h1 class="fm-page-title">Administration & Strategy Cockpit</h1>
            <p class="fm-page-subtitle">Global oversight of platform operations, user management, and service configurations.</p>
        </div>

        <div class="d-flex flex-wrap gap-2">
            <button class="fm-btn btn-light border"><i class="bi bi-cloud-download me-1"></i> System Export</button>
            <button class="fm-btn btn-light border"><i class="bi bi-gear-fill me-1"></i> Config</button>
        </div>
    </div>

    <!-- System Vital Metrics -->
    <div class="row g-4 mb-5">
        <div class="col-12 col-md-6 col-xl-3">
            <div class="fm-surface p-4 h-100 border-0 shadow-sm hover-lift">
                <div class="d-flex align-items-center gap-3 mb-3">
                    <div class="bg-primary-subtle text-primary p-3 rounded-3">
                        <i class="bi bi-people-fill fs-4"></i>
                    </div>
                    <div>
                        <div class="fm-caption fw-bold opacity-50 text-uppercase">USER BASE</div>
                        <h2 class="mb-0 fw-bold">Audit Required</h2>
                    </div>
                </div>
                <div class="progress" style="height: 4px;">
                    <div class="progress-bar" role="progressbar" style="width: 70%;"></div>
                </div>
            </div>
        </div>
        <div class="col-12 col-md-6 col-xl-3">
            <div class="fm-surface p-4 h-100 border-0 shadow-sm hover-lift">
                <div class="d-flex align-items-center gap-3 mb-3">
                    <div class="bg-success-subtle text-success p-3 rounded-3">
                        <i class="bi bi-box-seam-fill fs-4"></i>
                    </div>
                    <div>
                        <div class="fm-caption fw-bold opacity-50 text-uppercase">GLOBAL SKU COUNT</div>
                        <h2 class="mb-0 fw-bold">Managed</h2>
                    </div>
                </div>
                <div class="progress" style="height: 4px;">
                    <div class="progress-bar bg-success" role="progressbar" style="width: 85%;"></div>
                </div>
            </div>
        </div>
        <div class="col-12 col-md-6 col-xl-3">
            <div class="fm-surface p-4 h-100 border-0 shadow-sm hover-lift">
                <div class="d-flex align-items-center gap-3 mb-3">
                    <div class="bg-warning-subtle text-warning p-3 rounded-3">
                        <i class="bi bi-shield-check fs-4"></i>
                    </div>
                    <div>
                        <div class="fm-caption fw-bold opacity-50 text-uppercase">SUBSCRIPTIONS</div>
                        <h2 class="mb-0 fw-bold">Active</h2>
                    </div>
                </div>
                <div class="progress" style="height: 4px;">
                    <div class="progress-bar bg-warning" role="progressbar" style="width: 45%;"></div>
                </div>
            </div>
        </div>
        <div class="col-12 col-md-6 col-xl-3">
            <div class="fm-surface p-4 h-100 border-0 shadow-sm bg-primary text-white">
                <div class="d-flex align-items-center gap-3 mb-3">
                    <div class="bg-white bg-opacity-20 text-white p-3 rounded-3">
                        <i class="bi bi-graph-up-arrow fs-4"></i>
                    </div>
                    <div>
                        <div class="fm-caption fw-bold opacity-75 text-uppercase">SYSTEM VELOCITY</div>
                        <h2 class="mb-0 fw-bold text-white">EXPONENTIAL</h2>
                    </div>
                </div>
                <div class="progress" style="height: 4px; background: rgba(255,255,255,0.2);">
                    <div class="progress-bar bg-white" role="progressbar" style="width: 100%;"></div>
                </div>
            </div>
        </div>
    </div>

    <div class="row g-4">
        <!-- Module Navigation Grid -->
        <div class="col-xl-8">
            <div class="fm-surface p-4 border-0 shadow-sm mb-4">
                <h2 class="fm-h3 mb-4">Strategic Management Modules</h2>
                <div class="row g-4">
                    <div class="col-md-6">
                        <a href="${pageContext.request.contextPath}/admin/products" class="fm-card p-4 d-flex align-items-start gap-3 hover-lift text-decoration-none text-dark">
                            <div class="bg-light p-3 rounded-circle text-primary"><i class="bi bi-boxes fs-4"></i></div>
                            <div>
                                <h4 class="fw-bold mb-1">SKU & Inventory Master</h4>
                                <p class="small text-muted mb-0">Global catalog management, pricing strategy, and inactive product audits.</p>
                            </div>
                        </a>
                    </div>
                    <div class="col-md-6">
                        <a href="${pageContext.request.contextPath}/admin/sellers" class="fm-card p-4 d-flex align-items-start gap-3 hover-lift text-decoration-none text-dark">
                            <div class="bg-light p-3 rounded-circle text-primary"><i class="bi bi-shop fs-4"></i></div>
                            <div>
                                <h4 class="fw-bold mb-1">Merchant Hub Management</h4>
                                <p class="small text-muted mb-0">Partner onboarding, commission logic, and merchant performance ledger.</p>
                            </div>
                        </a>
                    </div>
                    <div class="col-md-6">
                        <a href="${pageContext.request.contextPath}/admin/subscriptions" class="fm-card p-4 d-flex align-items-start gap-3 hover-lift text-decoration-none text-dark">
                            <div class="bg-light p-3 rounded-circle text-primary"><i class="bi bi-card-checklist fs-4"></i></div>
                            <div>
                                <h4 class="fw-bold mb-1">Service Subscriptions</h4>
                                <p class="small text-muted mb-0">Tiered plan accessibility, recurring billing cycles, and system entitlement audit.</p>
                            </div>
                        </a>
                    </div>
                    <div class="col-md-6">
                        <a href="${pageContext.request.contextPath}/pro/dashboard" class="fm-card p-4 d-flex align-items-start gap-3 hover-lift text-decoration-none text-dark">
                            <div class="bg-light p-3 rounded-circle text-primary"><i class="bi bi-gem fs-4"></i></div>
                            <div>
                                <h4 class="fw-bold mb-1">PRO Analytics Console</h4>
                                <p class="small text-muted mb-0">High-level insights designed for executive decision-making and performance audit.</p>
                            </div>
                        </a>
                    </div>
                </div>
            </div>
            
            <div class="fm-surface p-4 border-0 shadow-sm">
                <h2 class="fm-h3 mb-3">Operations Log Summary</h2>
                <div class="p-5 text-center bg-light rounded-4">
                    <i class="bi bi-journal-text fs-1 opacity-10 mb-2 d-block"></i>
                    <p class="text-muted small mb-0">Administrative audit logging is active. No critical breaches detected in the last 24h cycle.</p>
                </div>
            </div>
        </div>

        <!-- Sidebar / Alerts / Quick Actions -->
        <div class="col-xl-4 text-center">
            <div class="fm-surface p-4 border-0 shadow-sm h-100 bg-slate-50">
                <div class="mb-5">
                    <div class="bg-white rounded-circle d-inline-flex align-items-center justify-content-center mb-3 shadow-sm" style="width: 72px; height: 72px;">
                        <i class="bi bi-shield-lock-fill text-primary fs-3"></i>
                    </div>
                    <h3 class="fm-h3">Administrative Security</h3>
                    <p class="small text-muted">Current session is verified. Role: <span class="badge bg-primary px-2">SYSTEM_ADMIN</span></p>
                </div>

                <div class="text-start mb-5">
                    <h5 class="fm-caption fw-bold opacity-50 mb-3 text-uppercase ls-wide">Pending Critical Approvals</h5>
                    <div class="fm-card p-3 border-0 bg-white mb-3">
                        <div class="d-flex align-items-center justify-content-between">
                            <span class="small fw-bold">New Merchant: Hub_A</span>
                            <button class="btn btn-sm btn-primary py-0 px-2 fw-bold">Review</button>
                        </div>
                    </div>
                    <div class="fm-card p-3 border-0 bg-white mb-0">
                        <div class="d-flex align-items-center justify-content-between">
                            <span class="small fw-bold">Price Index Threshold</span>
                            <button class="btn btn-sm btn-primary py-0 px-2 fw-bold">Audit</button>
                        </div>
                    </div>
                </div>

                <div class="p-4 bg-white rounded-4 border-0 shadow-xs mt-auto">
                    <h5 class="fm-caption fw-bold text-primary mb-3">System Health</h5>
                    <div class="d-flex justify-content-between align-items-center">
                        <span class="small text-muted">Java Stack</span>
                        <span class="badge bg-success-subtle text-success border-0 px-3">OPTIMIZED</span>
                    </div>
                    <hr class="fm-divider my-2">
                    <div class="d-flex justify-content-between align-items-center">
                        <span class="small text-muted">Tomcat Cycle</span>
                        <span class="badge bg-success-subtle text-success border-0 px-3">STABLE</span>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
