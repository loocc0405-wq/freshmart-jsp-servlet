<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:set var="pageTitle" value="My Orders"/>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"/>

<div class="fm-page-header">
    <div>
        <h1 class="fm-page-title">Đơn hàng của tôi</h1>
        <p class="fm-page-subtitle">Lọc nhanh và xem chi tiết các đơn hàng đã đặt.</p>
    </div>

    <a class="btn btn-outline-primary" href="${pageContext.request.contextPath}/catalog">
        <i class="bi bi-bag me-1"></i>Tiếp tục mua sắm
    </a>
</div>

<c:if test="${not empty errorMessage}">
    <div class="alert alert-danger fm-surface">
        <c:out value="${errorMessage}"/>
    </div>
</c:if>

<div class="fm-surface padded mb-4">
    <form class="row g-3 align-items-end" method="get"
          action="${pageContext.request.contextPath}/customer/orders">
        <div class="col-12 col-md-3">
            <label class="form-label">Trạng thái</label>
            <select name="status" class="form-select">
                <option value="">Tất cả</option>
                <option value="PENDING"    ${selectedStatus == 'PENDING'    ? 'selected' : ''}>PENDING</option>
                <option value="PROCESSING" ${selectedStatus == 'PROCESSING' ? 'selected' : ''}>PROCESSING</option>
                <option value="SHIPPING"   ${selectedStatus == 'SHIPPING'   ? 'selected' : ''}>SHIPPING</option>
                <option value="COMPLETED"  ${selectedStatus == 'COMPLETED'  ? 'selected' : ''}>COMPLETED</option>
                <option value="CANCELED"   ${(selectedStatus == 'CANCELED' || selectedStatus == 'CANCELLED') ? 'selected' : ''}>CANCELED</option>
            </select>
        </div>

        <div class="col-12 col-md-3">
            <label class="form-label">Từ ngày</label>
            <input type="date" class="form-control" name="fromDate" value="${fromDate}"/>
        </div>

        <div class="col-12 col-md-3">
            <label class="form-label">Đến ngày</label>
            <input type="date" class="form-control" name="toDate" value="${toDate}"/>
        </div>

        <div class="col-12 col-md-3">
            <button type="submit" class="btn btn-primary w-100">Lọc</button>
        </div>
    </form>

    <div class="mt-3 fm-muted">
        Tổng đơn phù hợp: <strong>${totalItems}</strong>
    </div>
</div>

<div class="fm-surface padded">
    <div class="table-responsive">
        <table class="table fm-table align-middle">
            <thead>
            <tr>
                <th>Mã đơn</th>
                <th>Trạng thái</th>
                <th>Loại</th>
                <th>Thanh toán</th>
                <th>Tổng</th>
                <th>Ngày tạo</th>
                <th>Thao tác</th>
            </tr>
            </thead>
            <tbody>
            <c:choose>
                <c:when test="${not empty orders}">
                    <c:forEach var="o" items="${orders}">
                        <tr>
                            <td><strong>${o.orderCode}</strong></td>
                            <td>${o.status}</td>
                            <td>${o.type}</td>
                            <td>${o.paymentMethod}</td>
                            <td>
                                <fmt:formatNumber value="${o.totalAmount}" type="number"
                                                  minFractionDigits="0" maxFractionDigits="2"/>
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
                        <td colspan="7" class="text-center fm-muted">Không có đơn hàng nào phù hợp.</td>
                    </tr>
                </c:otherwise>
            </c:choose>
            </tbody>
        </table>
    </div>

    <c:if test="${totalPages > 1}">
        <nav class="mt-3">
            <ul class="pagination mb-0 flex-wrap">
                <c:if test="${currentPage > 0}">
                    <li class="page-item">
                        <a class="page-link"
                           href="${pageContext.request.contextPath}/customer/orders?status=${selectedStatus}&fromDate=${fromDate}&toDate=${toDate}&page=${currentPage - 1}">
                            Trước
                        </a>
                    </li>
                </c:if>

                <c:forEach var="i" begin="0" end="${totalPages - 1}">
                    <li class="page-item ${i == currentPage ? 'active' : ''}">
                        <a class="page-link"
                           href="${pageContext.request.contextPath}/customer/orders?status=${selectedStatus}&fromDate=${fromDate}&toDate=${toDate}&page=${i}">
                            ${i + 1}
                        </a>
                    </li>
                </c:forEach>

                <c:if test="${currentPage + 1 < totalPages}">
                    <li class="page-item">
                        <a class="page-link"
                           href="${pageContext.request.contextPath}/customer/orders?status=${selectedStatus}&fromDate=${fromDate}&toDate=${toDate}&page=${currentPage + 1}">
                            Sau
                        </a>
                    </li>
                </c:if>
            </ul>
        </nav>
    </c:if>
</div>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>