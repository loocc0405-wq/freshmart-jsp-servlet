<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Staff OMS"/>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"/>

<h3>OMS cho Staff</h3>
<p class="text-muted">
    Danh sách đơn hàng kèm cờ kiểm tra tồn FEFO và cảnh báo near-expiry trong ${nearExpiryWindowDays} ngày.
</p>

<c:if test="${not empty errorMessage}">
    <div class="alert alert-danger"><c:out value="${errorMessage}"/></div>
</c:if>

<form method="get" action="${pageContext.request.contextPath}/staff/orders" class="row g-2 mb-3">
    <div class="col-md-3">
        <label class="form-label">Trạng thái</label>
        <select class="form-select" name="status">
            <option value="" ${empty selectedStatus ? 'selected' : ''}>-- Tất cả --</option>
            <option value="PENDING" ${selectedStatus == 'PENDING' ? 'selected' : ''}>PENDING</option>
            <option value="PROCESSING" ${selectedStatus == 'PROCESSING' ? 'selected' : ''}>PROCESSING</option>
            <option value="SHIPPING" ${selectedStatus == 'SHIPPING' ? 'selected' : ''}>SHIPPING</option>
            <option value="COMPLETED" ${selectedStatus == 'COMPLETED' ? 'selected' : ''}>COMPLETED</option>
            <option value="CANCELED" ${selectedStatus == 'CANCELED' ? 'selected' : ''}>CANCELED</option>
        </select>
    </div>
    <div class="col-md-3 d-flex align-items-end gap-2">
        <button class="btn btn-primary" type="submit">Lọc đơn</button>
        <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/staff/orders">Reset</a>
    </div>
</form>

<div class="card">
    <div class="card-header">Danh sách đơn</div>
    <div class="card-body">
        <c:if test="${empty orders}">
            <div class="text-muted">Chưa có đơn nào phù hợp bộ lọc.</div>
        </c:if>

        <c:if test="${not empty orders}">
            <table class="table table-sm align-middle">
                <thead>
                <tr>
                    <th>ID</th>
                    <th>Mã đơn</th>
                    <th>Loại</th>
                    <th>Trạng thái</th>
                    <th>Số dòng</th>
                    <th>Tổng tiền</th>
                    <th>Kiểm tra FEFO</th>
                    <th>Near-expiry</th>
                    <th></th>
                </tr>
                </thead>
                <tbody>
                <c:forEach items="${orders}" var="row">
                    <c:set var="openOrder"
                           value="${row.order.status == 'PENDING' || row.order.status == 'PROCESSING' || row.order.status == 'SHIPPING'}"/>
                    <tr>
                        <td><c:out value="${row.order.id}"/></td>
                        <td><c:out value="${row.order.orderCode}"/></td>
                        <td><c:out value="${row.order.type}"/></td>
                        <td>
                            <c:choose>
                                <c:when test="${row.order.status == 'PENDING'}">
                                    <span class="badge bg-secondary">PENDING</span>
                                </c:when>
                                <c:when test="${row.order.status == 'PROCESSING'}">
                                    <span class="badge bg-primary">PROCESSING</span>
                                </c:when>
                                <c:when test="${row.order.status == 'SHIPPING'}">
                                    <span class="badge bg-info text-dark">SHIPPING</span>
                                </c:when>
                                <c:when test="${row.order.status == 'COMPLETED'}">
                                    <span class="badge bg-success">COMPLETED</span>
                                </c:when>
                                <c:when test="${row.order.status == 'CANCELED'}">
                                    <span class="badge bg-danger">CANCELED</span>
                                </c:when>
                                <c:otherwise>
                                    <c:out value="${row.order.status}"/>
                                </c:otherwise>
                            </c:choose>
                        </td>
                        <td><c:out value="${row.itemCount}"/></td>
                        <td><c:out value="${row.order.totalAmount}"/></td>
                        <td>
                            <c:choose>
                                <c:when test="${!openOrder}">
                                    <span class="badge bg-secondary">Preview only</span>
                                </c:when>
                                <c:when test="${row.pendingFulfillable}">
                                    <span class="badge bg-success">Đủ tồn</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="badge bg-danger">Thiếu tồn</span>
                                </c:otherwise>
                            </c:choose>
                        </td>
                        <td>
                            <c:choose>
                                <c:when test="${!openOrder}">
                                    <span class="badge bg-secondary">History</span>
                                </c:when>
                                <c:when test="${row.nearExpiryPriority}">
                                    <span class="badge bg-warning text-dark">Có lot cần ưu tiên</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="badge bg-secondary">Không</span>
                                </c:otherwise>
                            </c:choose>
                        </td>
                        <td>
                            <a class="btn btn-sm btn-outline-primary"
                               href="${pageContext.request.contextPath}/staff/orders/detail?id=${row.order.id}">
                                Xem chi tiết
                            </a>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </c:if>
    </div>
</div>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>

