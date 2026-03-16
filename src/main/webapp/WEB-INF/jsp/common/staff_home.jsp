<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="pageTitle" value="Operations Control Center"/>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"/>

<div class="container-fluid px-4 py-4">
    <!-- Page Header -->
    <div class="d-flex align-items-center justify-content-between mb-4">
        <div>
            <h1 class="fm-h1 mb-1">Operations Control Center</h1>
            <p class="fm-text-secondary mb-0">Real-time inventory monitoring and warehouse management hub.</p>
        </div>
        <div class="d-flex gap-2">
            <button class="fm-btn btn-light border small"><i class="bi bi-download me-2"></i>Export Report</button>
            <a href="${pageContext.request.contextPath}/staff/import-lot" class="fm-btn fm-btn-primary small">
                <i class="bi bi-plus-lg me-2"></i>New Inventory Import
            </a>
        </div>
    </div>

    <!-- Active Statistics / KPIs -->
    <div class="row g-4 mb-5">
        <div class="col-md-6 col-xl-3">
            <div class="fm-card d-flex align-items-center gap-3">
                <div class="bg-primary-subtle text-primary rounded-3 d-flex align-items-center justify-content-center" style="width: 48px; height: 48px;">
                    <i class="bi bi-box-seam fs-4"></i>
                </div>
                <div>
                    <div class="fm-caption text-uppercase fw-bold opacity-75">Active Batches</div>
                    <div class="fm-h2 mb-0">124</div>
                </div>
            </div>
        </div>
        <div class="col-md-6 col-xl-3">
            <div class="fm-card d-flex align-items-center gap-3">
                <div class="bg-warning-subtle text-warning rounded-3 d-flex align-items-center justify-content-center" style="width: 48px; height: 48px;">
                    <i class="bi bi-exclamation-triangle fs-4"></i>
                </div>
                <div>
                    <div class="fm-caption text-uppercase fw-bold opacity-75">Expiring Soon</div>
                    <div class="fm-h2 mb-0 text-warning">18</div>
                </div>
            </div>
        </div>
        <div class="col-md-6 col-xl-3">
            <div class="fm-card d-flex align-items-center gap-3">
                <div class="bg-danger-subtle text-danger rounded-3 d-flex align-items-center justify-content-center" style="width: 48px; height: 48px;">
                    <i class="bi bi-x-circle fs-4"></i>
                </div>
                <div>
                    <div class="fm-caption text-uppercase fw-bold opacity-75">Critical / Expired</div>
                    <div class="fm-h2 mb-0 text-danger">4</div>
                </div>
            </div>
        </div>
        <div class="col-md-6 col-xl-3">
            <div class="fm-card d-flex align-items-center gap-3">
                <div class="bg-info-subtle text-info rounded-3 d-flex align-items-center justify-content-center" style="width: 48px; height: 48px;">
                    <i class="bi bi-truck fs-4"></i>
                </div>
                <div>
                    <div class="fm-caption text-uppercase fw-bold opacity-75">Fulfillment Rate</div>
                    <div class="fm-h2 mb-0">98.2%</div>
                </div>
            </div>
        </div>
    </div>

    <!-- Operation Modules -->
    <div class="row g-4 mb-4">
        <!-- Inventory Module -->
        <div class="col-lg-8">
            <div class="fm-surface p-4 h-100">
                <div class="d-flex justify-content-between align-items-center mb-4">
                    <h3 class="fm-h3 mb-0">Inventory & FEFO Management</h3>
                    <a href="${pageContext.request.contextPath}/staff/inventory" class="small fw-bold">View Detailed Ledger <i class="bi bi-arrow-right"></i></a>
                </div>
                <div class="row g-3">
                    <div class="col-md-6">
                        <div class="p-3 border rounded-3 bg-light-subtle">
                            <h6 class="fw-bold small mb-2"><i class="bi bi-lightning-charge me-2 text-primary"></i>FEFO Optimization</h6>
                            <p class="small text-muted mb-3">System automatically selects nearest-expiry batches to minimize fresh produce waste.</p>
                            <div class="d-flex gap-2">
                                <a href="${pageContext.request.contextPath}/staff/inventory" class="btn btn-sm btn-outline-dark">Check Stock</a>
                                <a href="${pageContext.request.contextPath}/staff/forecast" class="btn btn-sm btn-outline-dark">Forecasting</a>
                            </div>
                        </div>
                    </div>
                    <div class="col-md-6">
                        <div class="p-3 border rounded-3 bg-light-subtle">
                            <h6 class="fw-bold small mb-2"><i class="bi bi-graph-up me-2 text-primary"></i>Operational Insights</h6>
                            <p class="small text-muted mb-3">Analyze supplier performance and cold-chain efficiency reports.</p>
                            <div class="d-flex gap-2">
                                <a href="${pageContext.request.contextPath}/staff/inventory-report" class="btn btn-sm btn-outline-dark">Analytics Hub</a>
                            </div>
                        </div>
                    </div>
                </div>
                
                <!-- Quick Look: Critical Items -->
                <div class="mt-4 pt-4 border-top">
                    <h6 class="fw-bold text-uppercase small opacity-50 mb-3">Operational Alerts</h6>
                    <div class="alert alert-warning border-0 bg-warning-subtle d-flex align-items-center gap-3">
                        <i class="bi bi-calendar-x fs-4"></i>
                        <div>
                            <div class="fw-bold">Expiring Batches Detected</div>
                            <div class="small">5 batches of "Organic Salmon Steak" are expiring in less than 48 hours. Recommend POS discount injection.</div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- Sales & POS Sidebar -->
        <div class="col-lg-4">
            <div class="fm-surface p-4 h-100">
                <h3 class="fm-h3 mb-4">Channel Fulfillment</h3>
                <div class="d-grid gap-3">
                    <a href="${pageContext.request.contextPath}/seller/pos" class="fm-btn fm-btn-primary py-3">
                        <i class="bi bi-shop fs-5 me-2"></i> Launch Smart POS
                    </a>
                    <a href="${pageContext.request.contextPath}/staff/orders" class="fm-btn btn-light border py-3">
                        <i class="bi bi-list-check fs-5 me-2"></i> Order Management (OMS)
                    </a>
                </div>
                
                <div class="mt-5">
                    <h6 class="fw-bold text-uppercase small opacity-50 mb-3">Quick Metrics</h6>
                    <div class="d-flex justify-content-between mb-2">
                        <span class="small text-muted">Daily Revenue</span>
                        <span class="small fw-bold">$12,450.00</span>
                    </div>
                    <div class="progress mb-4" style="height: 6px;">
                        <div class="progress-bar bg-success" style="width: 75%"></div>
                    </div>
                    
                    <div class="d-flex justify-content-between mb-2">
                        <span class="small text-muted">Inventory Turnover</span>
                        <span class="small fw-bold">4.2x</span>
                    </div>
                    <div class="progress mb-4" style="height: 6px;">
                        <div class="progress-bar bg-primary" style="width: 60%"></div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
