<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:set var="pageTitle" value="Personal Cockpit | FreshMart Enterprise"/>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"/>

<div class="container-fluid px-4 py-4">
    <div class="fm-page-header mb-5">
        <div>
            <div class="fm-caption fw-bold text-primary mb-1 text-uppercase">Logistics Hub</div>
            <h1 class="fm-page-title">Personal Fulfillment Cockpit</h1>
            <p class="fm-page-subtitle">Unified view of your procurement activity and financial spending metrics.</p>
        </div>

        <div class="d-flex flex-wrap gap-2">
            <a class="fm-btn btn-light border" href="${pageContext.request.contextPath}/catalog">
                <i class="bi bi-cart3 me-1"></i> New Procurement
            </a>
            <a class="fm-btn fm-btn-primary" href="${pageContext.request.contextPath}/customer/orders">
                <i class="bi bi-receipt me-1"></i> Audit Orders
            </a>
        </div>
    </div>

    <c:if test="${not empty errorMessage}">
        <div class="alert alert-danger fm-surface mb-4 shadow-none border-0 bg-danger-subtle"><i class="bi bi-exclamation-octagon me-2"></i> <c:out value="${errorMessage}"/></div>
    </c:if>

    <c:if test="${not empty summary && summary.overSpendingThreshold}">
        <div class="alert alert-warning border-0 bg-warning-subtle d-flex align-items-center gap-3 mb-5 p-4 shadow-sm">
            <i class="bi bi-graph-up-arrow fs-3"></i>
            <div>
                <div class="fw-bold">Financial Alert: Spending Threshold Exceeded</div>
                <div class="small">
                    Your spending in the last 30 days is 
                    <span class="fw-bold"><fmt:formatNumber value="${summary.spentLast30Days}" type="number"/> ₫</span>, 
                    surpassing your limit of <span class="fw-bold"><fmt:formatNumber value="${summary.spendingAlertThreshold}" type="number"/> ₫</span>.
                </div>
            </div>
        </div>
    </c:if>

    <c:if test="${not empty summary}">
        <!-- Dynamic Metrics Ribbon -->
        <div class="row g-4 mb-5">
            <div class="col-6 col-md-4 col-xl-2">
                <div class="fm-surface p-4 text-center h-100 border-0 shadow-sm hover-lift">
                    <div class="fm-caption fw-bold opacity-50 mb-2">LIFETIME ORDERS</div>
                    <div class="fm-h1 mb-0">${summary.totalOrders}</div>
                </div>
            </div>
            <div class="col-6 col-md-4 col-xl-2">
                <div class="fm-surface p-4 text-center h-100 border-0 shadow-sm hover-lift">
                    <div class="fm-caption fw-bold opacity-50 mb-2">ACTIVE FULFILLMENT</div>
                    <div class="fm-h1 mb-0 text-primary">${summary.pendingOrders}</div>
                </div>
            </div>
            <div class="col-6 col-md-4 col-xl-2">
                <div class="fm-surface p-4 text-center h-100 border-0 shadow-sm hover-lift">
                    <div class="fm-caption fw-bold opacity-50 mb-3">COMPLETED</div>
                    <div class="fm-h1 mb-0 text-success">${summary.completedOrders}</div>
                </div>
            </div>
            <div class="col-12 col-md-6 col-xl-3">
                <div class="fm-surface p-4 h-100 border-0 shadow-sm bg-primary text-white">
                    <div class="fm-caption fw-bold opacity-75 mb-2 text-white">TOTAL EXPENDITURE</div>
                    <div class="fm-h1 mb-0">
                        <fmt:formatNumber value="${summary.totalSpent}" type="number" maxFractionDigits="0"/> 
                        <span class="fs-5 opacity-75">₫</span>
                    </div>
                </div>
            </div>
            <div class="col-12 col-md-6 col-xl-3">
                <div class="fm-surface p-4 h-100 border-0 shadow-sm">
                    <div class="fm-caption fw-bold opacity-50 mb-2">30-DAY VELOCITY</div>
                    <div class="fm-h1 mb-0">
                        <fmt:formatNumber value="${summary.spentLast30Days}" type="number" maxFractionDigits="0"/>
                        <span class="fs-5 opacity-50">₫</span>
                    </div>
                </div>
            </div>
        </div>

        <div class="row g-4">
            <!-- Recent Activity Ledger -->
            <div class="col-12 col-xl-8">
                <div class="fm-surface p-4 border-0 shadow-sm">
                    <div class="d-flex justify-content-between align-items-center mb-4">
                        <h2 class="fm-h3 mb-0">Recent Logistics Ledger</h2>
                        <a class="fm-btn btn-light border btn-sm small fw-bold" href="${pageContext.request.contextPath}/customer/orders">View Full History</a>
                    </div>

                    <div class="table-responsive">
                        <table class="table fm-data-table align-middle">
                            <thead class="bg-light">
                                <tr>
                                    <th>Service ID</th>
                                    <th>Status Phase</th>
                                    <th class="text-end">Value</th>
                                    <th>Timestamp</th>
                                    <th class="text-end">Action</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:choose>
                                    <c:when test="${not empty recentOrders}">
                                        <c:forEach var="o" items="${recentOrders}">
                                            <tr>
                                                <td><span class="font-monospace fw-bold text-primary">${o.orderCode}</span></td>
                                                <td><span class="fm-status-badge ${o.status == 'COMPLETED' ? 'available' : (o.status == 'CANCELED' ? 'disposed' : 'low-stock')} px-2 small">${o.status}</span></td>
                                                <td class="text-end fw-bold">
                                                    <fmt:formatNumber value="${o.totalAmount}" type="number" maxFractionDigits="0"/> ₫
                                                </td>
                                                <td class="small fm-text-secondary">${o.createdAt}</td>
                                                <td class="text-end">
                                                    <a class="btn btn-outline-primary btn-sm rounded-pill px-3 py-1 small" href="${pageContext.request.contextPath}/customer/order-detail?id=${o.id}">Inspect</a>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </c:when>
                                    <c:otherwise>
                                        <tr>
                                            <td colspan="5" class="p-5 text-center text-muted">No recent activity detected.</td>
                                        </tr>
                                    </c:otherwise>
                                </c:choose>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>

            <!-- Insights Component -->
            <div class="col-12 col-xl-4">
                <div class="fm-surface p-4 border-0 shadow-sm h-100">
                    <h2 class="fm-h3 mb-4">Account Analytics</h2>
                    
                    <c:choose>
                        <c:when test="${summary.latestCompletedAt != null}">
                            <div class="mb-5">
                                <div class="fm-caption fw-bold opacity-50 mb-3 text-uppercase ls-wide">Last Successful Fulfillment</div>
                                <div class="fm-card p-4 border-0 bg-light-subtle mb-3">
                                    <div class="fm-h3 mb-1 text-success">
                                        <fmt:formatNumber value="${summary.latestCompletedOrderAmount}" type="number"/> ₫
                                    </div>
                                    <div class="small fw-bold opacity-50">Value Captured</div>
                                </div>
                                <div class="d-flex align-items-center gap-2 text-muted small">
                                    <i class="bi bi-calendar-check"></i>
                                    <span>Fulfillment Timestamp: ${summary.latestCompletedAt}</span>
                                </div>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="fm-surface p-4 text-center border-dashed mb-5">
                                <i class="bi bi-bar-chart-line fs-2 opacity-10 mb-2"></i>
                                <p class="small text-muted mb-0">No completed fulfillments yet to generate insights.</p>
                            </div>
                        </c:otherwise>
                    </c:choose>

                    <div class="mb-4">
                        <div class="fm-caption fw-bold opacity-50 mb-3 text-uppercase ls-wide">Average Transaction Value</div>
                        <div class="fm-h2 mb-0">
                            <fmt:formatNumber value="${summary.averageCompletedOrderAmount}" type="number" maxFractionDigits="0"/> 
                            <span class="fs-5 opacity-50">₫</span>
                        </div>
                    </div>

                    <div class="p-3 bg-primary-subtle text-primary rounded-3 border-0 small mt-auto">
                        <i class="bi bi-shield-check me-2"></i> Your data metrics are audited hourly for financial compliance.
                    </div>
                </div>
            </div>
        </div>
    </c:if>
</div>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>