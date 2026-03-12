<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:set var="pageTitle" value="Customer Dashboard"/>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"/>

<div class="fm-page-header">
    <div>
        <h1 class="fm-page-title">Customer Dashboard</h1>
        <p class="fm-page-subtitle">Theo dõi đơn hàng và tình hình mua sắm của bạn.</p>
    </div>

    <div class="d-flex flex-wrap gap-2">
        <a class="btn btn-outline-primary" href="${pageContext.request.contextPath}/catalog">
            <i class="bi bi-bag me-1"></i>Mua sắm
        </a>
        <a class="btn btn-primary" href="${pageContext.request.contextPath}/customer/orders">
            <i class="bi bi-receipt me-1"></i>Đơn hàng của tôi
        </a>
    </div>
</div>

<c:if test="${not empty errorMessage}">
    <div class="alert alert-danger fm-surface">
        <c:out value="${errorMessage}"/>
    </div>
</c:if>

<c:if test="${not empty summary && summary.overSpendingThreshold}">
    <div class="alert alert-warning fm-surface">
        Chi tiêu 30 ngày gần nhất đã đạt
        <strong>
            <fmt:formatNumber value="${summary.spentLast30Days}" type="number" minFractionDigits="0" maxFractionDigits="2"/>
        </strong>,
        vượt ngưỡng cảnh báo
        <strong>
            <fmt:formatNumber value="${summary.spendingAlertThreshold}" type="number" minFractionDigits="0" maxFractionDigits="2"/>
        </strong>.
    </div>
</c:if>

<c:if test="${not empty summary}">
    <div class="row g-3 mb-4">
        <div class="col-12 col-md-6 col-xl-3">
            <div class="fm-kpi">
                <h6>Tổng đơn hàng</h6>
                <h4>${summary.totalOrders}</h4>
            </div>
        </div>

        <div class="col-12 col-md-6 col-xl-3">
            <div class="fm-kpi">
                <h6>Đơn đang chờ</h6>
                <h4>${summary.pendingOrders}</h4>
            </div>
        </div>

        <div class="col-12 col-md-6 col-xl-3">
            <div class="fm-kpi">
                <h6>Đơn hoàn tất</h6>
                <h4>${summary.completedOrders}</h4>
            </div>
        </div>

        <div class="col-12 col-md-6 col-xl-3">
            <div class="fm-kpi">
                <h6>Tổng chi tiêu</h6>
                <h4>
                    <fmt:formatNumber value="${summary.totalSpent}" type="number" minFractionDigits="0" maxFractionDigits="2"/>
                </h4>
            </div>
        </div>

        <div class="col-12 col-md-6 col-xl-6">
            <div class="fm-kpi">
                <h6>Chi tiêu 30 ngày</h6>
                <h4>
                    <fmt:formatNumber value="${summary.spentLast30Days}" type="number" minFractionDigits="0" maxFractionDigits="2"/>
                </h4>
            </div>
        </div>

        <div class="col-12 col-md-6 col-xl-6">
            <div class="fm-kpi">
                <h6>Giá trị đơn trung bình</h6>
                <h4>
                    <fmt:formatNumber value="${summary.averageCompletedOrderAmount}" type="number" minFractionDigits="0" maxFractionDigits="2"/>
                </h4>
            </div>
        </div>
    </div>

    <div class="row g-4">
        <div class="col-12 col-xl-8">
            <div class="fm-surface padded">
                <div class="d-flex justify-content-between align-items-center mb-3">
                    <div>
                        <h2 class="h5 mb-1">Đơn hàng gần đây</h2>
                        <div class="fm-muted small">Tối đa 5 đơn gần nhất của bạn.</div>
                    </div>
                    <a class="btn btn-outline-primary btn-sm" href="${pageContext.request.contextPath}/customer/orders">
                        Xem tất cả
                    </a>
                </div>

                <div class="table-responsive">
                    <table class="table fm-table align-middle">
                        <thead>
                        <tr>
                            <th>Mã đơn</th>
                            <th>Trạng thái</th>
                            <th>Tổng tiền</th>
                            <th>Ngày tạo</th>
                            <th>Thao tác</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:choose>
                            <c:when test="${not empty recentOrders}">
                                <c:forEach var="o" items="${recentOrders}">
                                    <tr>
                                        <td><strong>${o.orderCode}</strong></td>
                                        <td>${o.status}</td>
                                        <td>
                                            <fmt:formatNumber value="${o.totalAmount}" type="number" minFractionDigits="0" maxFractionDigits="2"/>
                                        </td>
                                        <td>${o.createdAt}</td>
                                        <td>
                                            <a class="btn btn-outline-primary btn-sm"
                                               href="${pageContext.request.contextPath}/customer/order-detail?id=${o.id}">
                                                Xem chi tiết
                                            </a>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr>
                                    <td colspan="5" class="text-center fm-muted">Chưa có đơn hàng nào.</td>
                                </tr>
                            </c:otherwise>
                        </c:choose>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>

        <div class="col-12 col-xl-4">
            <div class="fm-surface padded">
                <h2 class="h5 mb-3">Insight đơn gần nhất</h2>
                <c:choose>
                    <c:when test="${summary.latestCompletedAt != null}">
                        <div class="mb-2 small fm-muted">Giá trị đơn hoàn tất gần nhất</div>
                        <div class="fm-price mb-3">
                            <fmt:formatNumber value="${summary.latestCompletedOrderAmount}" type="number" minFractionDigits="0" maxFractionDigits="2"/> ₫
                        </div>

                        <div class="mb-2 small fm-muted">Thời gian hoàn tất</div>
                        <div>${summary.latestCompletedAt}</div>
                    </c:when>
                    <c:otherwise>
                        <div class="fm-muted">Chưa có đơn hoàn tất.</div>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </div>
</c:if>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>