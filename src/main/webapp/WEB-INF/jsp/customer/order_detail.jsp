<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:set var="pageTitle" value="Order Detail"/>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"/>

<div class="fm-page-header">
    <div>
        <h1 class="fm-page-title">Chi tiết đơn hàng</h1>
        <p class="fm-page-subtitle">Xem trạng thái và các sản phẩm trong đơn của bạn.</p>
    </div>

    <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/customer/orders">
        <i class="bi bi-arrow-left me-1"></i>Quay lại
    </a>
</div>

<c:if test="${not empty errorMessage}">
    <div class="alert alert-danger fm-surface">
        <c:out value="${errorMessage}"/>
    </div>
</c:if>

<c:if test="${not empty order}">
    <div class="row g-4">
        <div class="col-12 col-lg-4">
            <div class="fm-surface padded h-100">
                <h2 class="h5 mb-3">Thông tin đơn hàng</h2>

                <div class="mb-2 small fm-muted">Mã đơn</div>
                <div class="mb-3"><strong>${order.orderCode}</strong></div>

                <div class="mb-2 small fm-muted">Trạng thái</div>
                <div class="mb-3">${order.status}</div>

                <div class="mb-2 small fm-muted">Loại đơn</div>
                <div class="mb-3">${order.type}</div>

                <div class="mb-2 small fm-muted">Thanh toán</div>
                <div class="mb-3">${order.paymentMethod}</div>

                <div class="mb-2 small fm-muted">Ngày tạo</div>
                <div class="mb-3">${order.createdAt}</div>

                <div class="mb-2 small fm-muted">Tổng tiền</div>
                <div class="fm-price">
                    <fmt:formatNumber value="${order.totalAmount}" type="number"
                                      minFractionDigits="0" maxFractionDigits="2"/> ₫
                </div>
            </div>
        </div>

        <div class="col-12 col-lg-8">
            <div class="fm-surface padded h-100">
                <h2 class="h5 mb-3">Sản phẩm trong đơn</h2>

                <div class="table-responsive">
                    <table class="table fm-table align-middle">
                        <thead>
                        <tr>
                            <th>Sản phẩm</th>
                            <th>Số lượng</th>
                            <th>Đơn giá</th>
                            <th>Thành tiền</th>
                            <th></th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach var="item" items="${order.items}">
                            <tr>
                                <td><strong>${item.product.name}</strong></td>
                                <td>${item.quantity}</td>
                                <td>
                                    <fmt:formatNumber value="${item.unitPrice}" type="number"
                                                      minFractionDigits="0" maxFractionDigits="2"/>
                                </td>
                                <td>
                                    <fmt:formatNumber value="${item.lineTotal}" type="number"
                                                      minFractionDigits="0" maxFractionDigits="2"/>
                                </td>
                                <td>
                                    <a class="btn btn-outline-primary btn-sm"
                                       href="${pageContext.request.contextPath}/product?id=${item.product.id}">
                                        Xem sản phẩm
                                    </a>
                                </td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </div>

                <div class="mt-3">
                    <c:choose>
                        <c:when test="${order.status == 'PENDING'}">
                            <div class="alert alert-light border mb-0">Đơn hàng đang chờ xử lý.</div>
                        </c:when>
                        <c:when test="${order.status == 'PROCESSING'}">
                            <div class="alert alert-light border mb-0">Đơn hàng đang được chuẩn bị.</div>
                        </c:when>
                        <c:when test="${order.status == 'SHIPPING'}">
                            <div class="alert alert-light border mb-0">Đơn hàng đang được giao.</div>
                        </c:when>
                        <c:when test="${order.status == 'COMPLETED'}">
                            <div class="alert alert-success mb-0">Đơn hàng đã hoàn tất.</div>
                        </c:when>
                        <c:otherwise>
                            <div class="alert alert-secondary mb-0">Đơn hàng đã bị hủy.</div>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </div>
    </div>
</c:if>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>