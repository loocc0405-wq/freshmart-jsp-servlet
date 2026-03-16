<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:set var="pageTitle" value="Subscription Engine | FreshMart Enterprise"/>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"/>

<div class="container-fluid px-4 py-4">
    <!-- Header Section -->
    <div class="fm-page-header mb-5 border-bottom pb-4">
        <div>
            <div class="fm-caption fw-bold text-primary mb-1 text-uppercase ls-wide">System Governance</div>
            <h1 class="fm-page-title">Enterprise Subscription Engine</h1>
            <p class="fm-page-subtitle">Mission-critical interface for tier management, billing audits, and global system configurations.</p>
        </div>
    </div>

    <!-- Alert Messaging -->
    <c:if test="${not empty successMessage}">
        <div class="alert alert-success fm-surface border-0 shadow-sm mb-4 p-3 anim-fade-in">
            <i class="bi bi-check-circle-fill me-2"></i> <c:out value="${successMessage}"/>
        </div>
    </c:if>
    <c:if test="${not empty errorMessage}">
        <div class="alert alert-danger fm-surface border-0 shadow-sm mb-4 p-3 anim-fade-in">
            <i class="bi bi-exclamation-octagon-fill me-2"></i> <c:out value="${errorMessage}"/>
        </div>
    </c:if>

    <div class="row g-4">
        <!-- Configuration & Grant Panels -->
        <div class="col-xl-5">
            <div class="vstack gap-4">
                <!-- Tier Authorization -->
                <div class="fm-surface p-4 shadow-sm border-0">
                    <h5 class="fm-h3 mb-4 text-dark"><i class="bi bi-shield-lock me-2 text-primary"></i> Tier Authorization</h5>
                    <form method="post" action="${pageContext.request.contextPath}/admin/subscriptions">
                        <input type="hidden" name="csrf_token" value="${sessionScope.CSRF_TOKEN}"/>
                        <input type="hidden" name="action" value="grantPro"/>

                        <div class="mb-4">
                            <label class="fm-caption fw-bold d-block mb-2">Target Customer Entity</label>
                            <select class="fm-form-control py-2" name="userId" required>
                                <option value="">-- Audit User Identity --</option>
                                <c:forEach items="${users}" var="u">
                                    <option value="${u.id}">
                                        <c:out value="${u.username}"/> | <c:out value="${u.fullName}"/> [${u.tier}]
                                    </option>
                                </c:forEach>
                            </select>
                        </div>

                        <div class="row g-3 mb-4">
                            <div class="col-md-6">
                                <label class="fm-caption fw-bold d-block mb-2">Extended Duration (Days)</label>
                                <input class="fm-form-control" type="number" name="days" min="1" value="30" required/>
                            </div>
                            <div class="col-md-6">
                                <label class="fm-caption fw-bold d-block mb-2">Operational Template</label>
                                <select class="fm-form-control" onchange="this.form.days.value=this.value;">
                                    <c:forEach items="${planPrices}" var="plan">
                                        <option value="${plan.key}">${plan.key} Days Core</option>
                                    </c:forEach>
                                </select>
                            </div>
                        </div>

                        <div class="mb-4">
                            <label class="fm-caption fw-bold d-block mb-2">Strategic Authorization Note</label>
                            <textarea class="fm-form-control" name="note" rows="2" placeholder="Audit trail entry for this grant..."></textarea>
                        </div>

                        <button class="fm-btn fm-btn-primary w-100 py-3 fw-bold" type="submit">Initialize PRO Grant</button>
                    </form>

                    <hr class="my-5 border-secondary opacity-10">

                    <!-- Revocation Engine -->
                    <h5 class="fm-h3 mb-4 text-dark"><i class="bi bi-trash3 me-2 text-danger"></i> Access Revocation</h5>
                    <form method="post" action="${pageContext.request.contextPath}/admin/subscriptions">
                        <input type="hidden" name="csrf_token" value="${sessionScope.CSRF_TOKEN}"/>
                        <input type="hidden" name="action" value="revokePro"/>
                        
                        <div class="mb-4">
                            <label class="fm-caption fw-bold d-block mb-2">Subject for De-escalation</label>
                            <select class="fm-form-control border-danger-subtle py-2" name="userId" required>
                                <option value="">-- Identify PRO Subject --</option>
                                <c:forEach items="${users}" var="u">
                                    <c:if test="${u.tier.toString() eq 'PRO'}">
                                        <option value="${u.id}">
                                            <c:out value="${u.username}"/> | Exp: <c:out value="${u.expiredDate}"/>
                                        </option>
                                    </c:if>
                                </c:forEach>
                            </select>
                        </div>
                        <button class="fm-btn btn-danger w-100 py-3 fw-bold" type="submit" onclick="return confirm('Confirm permanent PRO access termination?');">
                            Terminate Access
                        </button>
                    </form>
                </div>

                <!-- Global Logic Constants -->
                <div class="fm-surface p-4 shadow-sm border-0 bg-slate-900 text-white">
                    <h5 class="fm-h3 mb-4 text-white"><i class="bi bi-gear-wide-connected me-2 text-primary"></i> Business Logic Tuner</h5>
                    <form method="post" action="${pageContext.request.contextPath}/admin/subscriptions">
                        <input type="hidden" name="csrf_token" value="${sessionScope.CSRF_TOKEN}"/>
                        <input type="hidden" name="action" value="saveSettings"/>

                        <div class="row g-3">
                            <div class="col-md-6">
                                <label class="fm-caption fw-bold text-white-50 d-block mb-2">Low Stock Threshold</label>
                                <input class="fm-form-control border-0 bg-slate-800 text-white" type="number" name="lowStockThreshold" value="${settings['low_stock_threshold']}" required/>
                            </div>
                            <div class="col-md-6">
                                <label class="fm-caption fw-bold text-white-50 d-block mb-2">Expiry Window (Days)</label>
                                <input class="fm-form-control border-0 bg-slate-800 text-white" type="number" name="upcomingExpiryDays" value="${settings['upcoming_expiry_days']}" required/>
                            </div>
                            <div class="col-md-6">
                                <label class="fm-caption fw-bold text-white-50 d-block mb-2">Grace Period (Days)</label>
                                <input class="fm-form-control border-0 bg-slate-800 text-white" type="number" name="subGracePeriodDays" value="${settings['subscription_grace_period_days']}" required/>
                            </div>
                            <div class="col-md-6">
                                <label class="fm-caption fw-bold text-white-50 d-block mb-2">Notification Lead</label>
                                <input class="fm-form-control border-0 bg-slate-800 text-white" type="number" name="subNotifyDays" value="${settings['subscription_notify_days']}" required/>
                            </div>
                        </div>

                        <div class="mt-5">
                            <button class="fm-btn fm-btn-primary w-100 py-3 fw-bold" type="submit">Commit Engine Settings</button>
                        </div>
                    </form>
                </div>
            </div>
        </div>

        <!-- System Ledgers -->
        <div class="col-xl-7">
            <div class="vstack gap-4">
                <!-- User Entitlements Ledger -->
                <div class="fm-surface p-0 shadow-sm border-0 overflow-hidden">
                    <div class="p-4 border-bottom d-flex justify-content-between align-items-center bg-light">
                        <h5 class="fm-h3 mb-0 text-dark">Entitlements Ledger</h5>
                        <input type="text" id="userSearchInput" class="fm-form-control py-1 px-3 w-Auto" style="min-width: 250px;" placeholder="Filter Identities..." onkeyup="filterUserTable()"/>
                    </div>
                    <div class="table-responsive">
                        <table class="table fm-data-table align-middle mb-0" id="userTable">
                            <thead class="bg-white">
                                <tr>
                                    <th class="ps-4">Identity</th>
                                    <th>Strategic Tier</th>
                                    <th class="text-center">Lifecycle Status</th>
                                    <th class="text-end pe-4">Expiry</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach items="${users}" var="u">
                                    <c:set var="uStatus" value="${statusMap[u.id]}"/>
                                    <tr>
                                        <td class="ps-4">
                                            <div class="fw-bold">${u.username}</div>
                                            <div class="small opacity-50">${u.fullName}</div>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${u.tier eq 'PRO'}">
                                                    <span class="badge bg-indigo-100 text-indigo-600 border-0 px-3 py-1">PRO-TIER</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge bg-slate-100 text-slate-500 border-0 px-3 py-1">FREE-CORE</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td class="text-center">
                                            <c:choose>
                                                <c:when test="${uStatus.status eq 'PRO_ACTIVE'}">
                                                    <span class="fm-status-badge available px-3">ACTIVE</span>
                                                </c:when>
                                                <c:when test="${uStatus.status eq 'PRO_EXPIRING_SOON'}">
                                                    <span class="fm-status-badge partially-available px-3">CRITICAL-EXP</span>
                                                </c:when>
                                                <c:when test="${uStatus.status eq 'PRO_EXPIRED_IN_GRACE'}">
                                                    <span class="fm-status-badge soon px-3">GRACE-PERIOD</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="fm-status-badge disposed px-3 opacity-50">DORMANT</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td class="text-end pe-4 font-monospace small">
                                            <c:out value="${u.expiredDate != null ? u.expiredDate : '-'}"/>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </div>

                <!-- Strategic Pivot: Payment Audit -->
                <div class="fm-surface p-0 shadow-sm border-0 overflow-hidden">
                    <div class="p-4 border-bottom bg-slate-50">
                        <h5 class="fm-h3 mb-0 text-dark">Strategic Settlement Audit</h5>
                        <p class="small text-muted mb-0">Record of all financial transitions and tier conversions.</p>
                    </div>
                    <div class="table-responsive" style="max-height: 400px;">
                        <table class="table fm-data-table align-middle mb-0">
                            <thead class="bg-white sticky-top">
                                <tr>
                                    <th class="ps-4">Settlement ID</th>
                                    <th>Subject</th>
                                    <th>Value (₫)</th>
                                    <th>Type</th>
                                    <th class="text-end pe-4">Committed At</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach items="${payments}" var="p">
                                    <tr>
                                        <td class="ps-4">
                                            <div class="font-monospace fw-bold text-primary">${p.paymentCode}</div>
                                        </td>
                                        <td>
                                            <div class="fw-medium">${p.user.username}</div>
                                            <div class="small opacity-50">${p.planName}</div>
                                        </td>
                                        <td class="fw-bold">
                                            <fmt:formatNumber value="${p.amount}" type="number"/>
                                        </td>
                                        <td>
                                            <span class="badge ${p.paymentStatus eq 'SUCCESS' ? 'bg-success-subtle text-success' : 'bg-secondary-subtle text-secondary'} border-0">
                                                ${p.paymentStatus}
                                            </span>
                                        </td>
                                        <td class="text-end pe-4 small text-muted">
                                            ${p.createdAt}
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<script>
    function filterUserTable() {
        let input = document.getElementById('userSearchInput');
        let filter = input.value.toUpperCase();
        let table = document.getElementById('userTable');
        let tr = table.getElementsByTagName('tr');
        for (let i = 1; i < tr.length; i++) {
            let cells = tr[i].getElementsByTagName('td');
            let match = false;
            for(let j=0; j<cells.length; j++) {
                if (cells[j].textContent.toUpperCase().indexOf(filter) > -1) {
                    match = true; break;
                }
            }
            tr[i].style.display = match ? "" : "none";
        }
    }
</script>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>