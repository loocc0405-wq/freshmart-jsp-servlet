<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Inventory View"/>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"/>

<h3>Xem Tồn Kho (Inventory by Lot)</h3>

<div class="row mb-3">
    <div class="col-lg-8">
        <form method="get" action="${pageContext.request.contextPath}/staff/inventory" class="d-flex gap-2">
            <select class="form-select" name="productId" onchange="this.form.submit()">
                <option value="">-- Chọn sản phẩm --</option>
                <c:forEach items="${products}" var="p">
                    <option value="${p.id}" <c:if test="${selectedProduct.id == p.id}">selected</c:if>>
                        <c:out value="${p.name}"/> (ID: ${p.id})
                    </option>
                </c:forEach>
            </select>
            <a class="btn btn-outline-primary" href="${pageContext.request.contextPath}/staff/import-lot">+ Nhập lô</a>
        </form>
    </div>
</div>

<c:if test="${not empty errorMessage}">
    <div class="alert alert-danger"><c:out value="${errorMessage}"/></div>
</c:if>

<c:if test="${selectedProduct != null}">
    <!-- Summary -->
    <div class="row mb-3">
        <div class="col-md-3">
            <div class="card text-center">
                <div class="card-body">
                    <h6 class="text-muted">Tổng nhập</h6>
                    <h3><c:out value="${summary.totalIn}"/></h3>
                </div>
            </div>
        </div>
        <div class="col-md-3">
            <div class="card text-center">
                <div class="card-body">
                    <h6 class="text-muted">Còn lại</h6>
                    <h3 class="text-success"><c:out value="${summary.totalLeft}"/></h3>
                </div>
            </div>
        </div>
        <div class="col-md-3">
            <div class="card text-center">
                <div class="card-body">
                    <h6 class="text-muted">Đã dùng</h6>
                    <h3 class="text-info"><c:out value="${summary.totalConsumed}"/></h3>
                </div>
            </div>
        </div>
        <div class="col-md-3">
            <div class="card text-center">
                <div class="card-body">
                    <h6 class="text-muted">HSD gần nhất</h6>
                    <h5><c:out value="${availableLots[0].expiryDate}"/></h5>
                </div>
            </div>
        </div>
    </div>

    <!-- Available Lots (FEFO) -->
    <div class="card mb-3">
        <div class="card-header">Lô còn dùng được (khả dụng - FEFO)</div>
        <div class="card-body">
            <c:if test="${empty availableLots}">
                <div class="text-muted">Không có lô khả dụng.</div>
            </c:if>

            <c:if test="${not empty availableLots}">
                <table class="table table-sm">
                    <thead>
                    <tr>
                        <th>Lô ID</th>
                        <th>Nhập ngày</th>
                        <th>HSD (Hạn sử dụng)</th>
                        <th>Số ngày còn lại</th>
                        <th>Nhập vào</th>
                        <th>Còn lại</th>
                        <th>Đã dùng</th>
                        <th>Giá nhập</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach items="${availableLots}" var="lot">
                        <c:set var="daysLeft" value="${lot.expiryDate.toEpochDay() - today.toEpochDay()}"/>
                        <tr <c:if test="${daysLeft <= 3}">class="table-warning"</c:if>>
                            <td><c:out value="${lot.id}"/></td>
                            <td><c:out value="${lot.importDate}"/></td>
                            <td><c:out value="${lot.expiryDate}"/></td>
                            <td>
                                <c:choose>
                                    <c:when test="${daysLeft <= 0}">Hết hạn (hôm nay)</c:when>
                                    <c:when test="${daysLeft <= 3}"><span class="badge bg-warning"><c:out value="${daysLeft}"/> ngày</span></c:when>
                                    <c:otherwise><c:out value="${daysLeft}"/> ngày</c:otherwise>
                                </c:choose>
                            </td>
                            <td><c:out value="${lot.qtyIn}"/></td>
                            <td><strong><c:out value="${lot.qtyLeft}"/></strong></td>
                            <td><c:out value="${lot.qtyIn - lot.qtyLeft}"/></td>
                            <td><c:out value="${lot.importPrice}"/></td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </c:if>
        </div>
    </div>

    <!-- Upcoming Expiry (7 days) -->
    <c:if test="${not empty upcomingExpiry}">
        <div class="card mb-3">
            <div class="card-header bg-warning">⚠️ Sắp hết hạn (trong 7 ngày tới)</div>
            <div class="card-body">
                <table class="table table-sm">
                    <thead>
                    <tr>
                        <th>Lô ID</th>
                        <th>HSD</th>
                        <th>Số ngày</th>
                        <th>Số lượng còn lại</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach items="${upcomingExpiry}" var="lot">
                        <tr class="table-warning">
                            <td><c:out value="${lot.id}"/></td>
                            <td><c:out value="${lot.expiryDate}"/></td>
                            <td>
                                <c:set var="days" value="${lot.expiryDate.toEpochDay() - today.toEpochDay()}"/>
                                <span class="badge bg-danger"><c:out value="${days}"/> ngày</span>
                            </td>
                            <td><c:out value="${lot.qtyLeft}"/></td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>
    </c:if>

    <!-- Expired Lots -->
    <c:if test="${not empty expiredLots}">
        <div class="card mb-3">
            <div class="card-header bg-danger text-white">Lô đã hết hạn (cần loại bỏ)</div>
            <div class="card-body">
                <table class="table table-sm">
                    <thead>
                    <tr>
                        <th>Lô ID</th>
                        <th>HSD</th>
                        <th>Số lượng còn lại</th>
                        <th>Ghi chú</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach items="${expiredLots}" var="lot">
                        <tr class="table-danger">
                            <td><c:out value="${lot.id}"/></td>
                            <td><c:out value="${lot.expiryDate}"/></td>
                            <td><c:out value="${lot.qtyLeft}"/></td>
                            <td>
                                <c:choose>
                                    <c:when test="${lot.qtyLeft > 0}">Cần loại bỏ ngay</c:when>
                                    <c:otherwise>Đã dùng hết</c:otherwise>
                                </c:choose>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>
    </c:if>

    <!-- All Lots History -->
    <div class="card">
        <div class="card-header">Toàn bộ lô (lịch sử)</div>
        <div class="card-body">
            <table class="table table-sm">
                <thead>
                <tr>
                    <th>Lô ID</th>
                    <th>Nhà cung cấp</th>
                    <th>Nhập ngày</th>
                    <th>HSD</th>
                    <th>Nhập</th>
                    <th>Còn</th>
                    <th>Giá</th>
                    <th>Trạng thái</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach items="${allLots}" var="lot">
                    <c:set var="status" value="active"/>
                    <c:if test="${lot.expiryDate lt today}">
                        <c:set var="status" value="expired"/>
                    </c:if>
                    <c:if test="${lot.qtyLeft == 0}">
                        <c:set var="status" value="consumed"/>
                    </c:if>

                    <tr <c:if test="${status == 'expired'}">class="table-danger"</c:if> <c:if test="${status == 'consumed'}">class="table-secondary"</c:if>>
                        <td><c:out value="${lot.id}"/></td>
                        <td><c:out value="${lot.supplier.name}"/></td>
                        <td><c:out value="${lot.importDate}"/></td>
                        <td><c:out value="${lot.expiryDate}"/></td>
                        <td><c:out value="${lot.qtyIn}"/></td>
                        <td><c:out value="${lot.qtyLeft}"/></td>
                        <td><c:out value="${lot.importPrice}"/></td>
                        <td>
                            <c:choose>
                                <c:when test="${status == 'expired'}"><span class="badge bg-danger">Hết hạn</span></c:when>
                                <c:when test="${status == 'consumed'}"><span class="badge bg-secondary">Đã dùng hết</span></c:when>
                                <c:otherwise><span class="badge bg-success">Khả dụng</span></c:otherwise>
                            </c:choose>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </div>

</c:if>

<c:if test="${selectedProduct == null}">
    <div class="alert alert-info">Vui lòng chọn một sản phẩm để xem chi tiết tồn kho.</div>
</c:if>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
