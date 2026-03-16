<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:set var="pageTitle" value="Procurement Successfully Initialized | FreshMart Enterprise"/>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"/>

<div class="container py-5 mt-5">
    <div class="row justify-content-center">
        <div class="col-xl-7 text-center">
            <!-- Animated Success Sequence -->
            <div class="mb-5">
                <div class="bg-success text-white rounded-circle d-inline-flex align-items-center justify-content-center shadow-lg hover-lift mb-4" style="width: 110px; height: 110px;">
                    <i class="bi bi-shield-check" style="font-size: 4rem;"></i>
                </div>
                <h1 class="display-5 fw-bold" style="font-family: 'Outfit', sans-serif;">Procurement Confirmed</h1>
                <p class="fs-5 text-muted">Your settlement has been authorized. The logistics sequence for your fulfillment is now active.</p>
            </div>

            <div class="fm-surface p-0 overflow-hidden shadow-sm text-start mb-5 border-0">
                <div class="bg-slate-900 text-white p-4">
                    <div class="row align-items-center">
                        <div class="col-md-7">
                            <div class="fm-caption fw-bold opacity-75 text-white mb-1 ls-wide">SYSTEM TRACKING ID</div>
                            <div class="fm-h3 mb-0 font-monospace text-primary">#ORD-${order.id}</div>
                        </div>
                        <div class="col-md-5 text-md-end mt-3 mt-md-0">
                            <span class="badge bg-success-subtle text-success border-0 rounded-pill px-4 py-2 fw-bold">AUTHORIZED</span>
                        </div>
                    </div>
                </div>

                <div class="p-4 p-md-5">
                    <div class="d-flex justify-content-between align-items-center mb-0">
                        <div>
                            <div class="fm-caption fw-bold opacity-50 mb-1 text-uppercase">Total Settlement Value</div>
                            <div class="fm-h1 mb-0 text-dark">
                                <fmt:formatNumber value="${order.totalAmount}" type="number"/> 
                                <span class="fs-4 opacity-50">₫</span>
                            </div>
                        </div>
                        <div class="text-end">
                            <div class="fm-caption fw-bold opacity-50 mb-1 text-uppercase">Hub Allocation</div>
                            <div class="small fw-bold">FEFO-Optimized</div>
                        </div>
                    </div>
                </div>
                
                <div class="bg-light p-3 border-top text-center">
                    <div class="small text-muted fw-bold"><i class="bi bi-info-circle me-1"></i> A detailed audit trail has been dispatched to your registered communique.</div>
                </div>
            </div>

            <!-- Strategic Actions -->
            <div class="d-flex flex-wrap justify-content-center gap-3 mb-5">
                <a class="fm-btn fm-btn-primary px-5 py-3 fs-5 shadow-sm" href="${pageContext.request.contextPath}/customer/orders">
                    <i class="bi bi-clipboard-data me-2"></i> Audit My Orders
                </a>
                <a class="fm-btn btn-light border px-5 py-3 fs-5 fw-bold" href="${pageContext.request.contextPath}/catalog">
                    <i class="bi bi-cart3 me-2"></i> Resume Procurement
                </a>
            </div>

            <div class="p-4 bg-primary-subtle rounded-4 d-inline-flex align-items-center gap-4 text-primary small">
                <div class="d-flex align-items-center gap-2"><i class="bi bi-check2-square border rounded-circle border-primary p-1"></i> Strategy Optimized</div>
                <div class="d-flex align-items-center gap-2"><i class="bi bi-check2-square border rounded-circle border-primary p-1"></i> Logistics Initialized</div>
                <div class="d-flex align-items-center gap-2"><i class="bi bi-check2-square border rounded-circle border-primary p-1"></i> Data Audited</div>
            </div>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>