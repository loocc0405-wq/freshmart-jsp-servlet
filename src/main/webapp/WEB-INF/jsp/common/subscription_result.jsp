<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:set var="pageTitle" value="Subscription Result"/>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"/>

<div class="fm-page-header">
    <div>
        <h1 class="fm-page-title">Thanh toán thành công</h1>
        <p class="fm-page-subtitle">
            Subscription đã được cập nhật và payment history đã được lưu.
        </p>
    </div>
</div>

<div class="alert alert-success">
    <i class="bi bi-check-circle-fill me-1"></i>
    Fake payment thành công. Tài khoản đã được nâng cấp / gia hạn PRO.
</div>

<div class="row g-4">
    <div class="col-lg-7">
        <div class="fm-surface padded h-100">
            <h5 class="mb-3">Receipt / Payment detail</h5>

            <dl class="row mb-0">
                <dt class="col-sm-4">Mã payment</dt>
                <dd class="col-sm-8"><span class="fw-bold"><c:out value="${payment.paymentCode}"/></span></dd>

                <dt class="col-sm-4">Gói</dt>
                <dd class="col-sm-8"><c:out value="${payment.planName}"/></dd>

                <dt class="col-sm-4">Số ngày</dt>
                <dd class="col-sm-8"><c:out value="${payment.planDays}"/> ngày</dd>

                <dt class="col-sm-4">Số tiền</dt>
                <dd class="col-sm-8">
                    <fmt:formatNumber value="${payment.amount}" type="number" maxFractionDigits="0"/> đ
                </dd>

                <dt class="col-sm-4">Phương thức</dt>
                <dd class="col-sm-8"><c:out value="${payment.paymentMethod}"/></dd>

                <dt class="col-sm-4">Trạng thái</dt>
                <dd class="col-sm-8">
                    <span class="badge text-bg-success"><c:out value="${payment.paymentStatus}"/></span>
                </dd>

                <dt class="col-sm-4">Ngày bắt đầu</dt>
                <dd class="col-sm-8"><c:out value="${payment.startDate}"/></dd>

                <dt class="col-sm-4">Ngày kết thúc</dt>
                <dd class="col-sm-8"><c:out value="${payment.endDate}"/></dd>

                <dt class="col-sm-4">Thời gian tạo</dt>
                <dd class="col-sm-8"><c:out value="${payment.createdAt}"/></dd>

                <dt class="col-sm-4">Ghi chú</dt>
                <dd class="col-sm-8">
                    <c:choose>
                        <c:when test="${not empty payment.note}">
                            <c:out value="${payment.note}"/>
                        </c:when>
                        <c:otherwise>-</c:otherwise>
                    </c:choose>
                </dd>
            </dl>
        </div>
    </div>

    <div class="col-lg-5">
        <div class="fm-surface padded h-100">
            <h5 class="mb-3">Thông tin tài khoản sau cập nhật</h5>

            <dl class="row mb-0">
                <dt class="col-sm-5">Username</dt>
                <dd class="col-sm-7"><c:out value="${updatedUser.username}"/></dd>

                <dt class="col-sm-5">Tier</dt>
                <dd class="col-sm-7">
                    <span class="badge text-bg-success"><c:out value="${updatedUser.tier}"/></span>
                </dd>

                <dt class="col-sm-5">Expired date</dt>
                <dd class="col-sm-7"><c:out value="${updatedUser.expiredDate}"/></dd>

                <dt class="col-sm-5">Còn lại</dt>
                <dd class="col-sm-7">
                    <c:choose>
                        <c:when test="${updatedUser.proActive}">
                            <span class="text-success fw-semibold">${updatedUser.remainingProDays} ngày</span>
                        </c:when>
                        <c:otherwise>
                            <span class="text-muted">0 ngày</span>
                        </c:otherwise>
                    </c:choose>
                </dd>
            </dl>

            <hr class="fm-divider"/>

            <div class="d-grid gap-2">
                <a class="btn btn-primary" href="${pageContext.request.contextPath}/pro/dashboard">
                    Đi tới PRO Dashboard
                </a>
                <a class="btn btn-outline-primary" href="${pageContext.request.contextPath}/subscription/upgrade">
                    Xem lại payment history
                </a>
                <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/catalog">
                    Quay về Catalog
                </a>
            </div>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>