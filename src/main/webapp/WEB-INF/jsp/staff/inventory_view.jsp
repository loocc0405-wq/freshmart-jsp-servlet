<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="pageTitle" value="Inventory View"/>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"/>

<h3>Xem Tồn Kho (Inventory by Lot)</h3>

<div class="row mb-3">
    <div class="col-12">
        <form method="get" action="${pageContext.request.contextPath}/staff/inventory" class="row g-2">

            <div class="col-md-3">
                <label class="form-label">Sản phẩm</label>
                <select class="form-select" name="productId">
                    <option value="">-- Tất cả sản phẩm --</option>
                    <c:forEach items="${products}" var="p">
                        <option value="${p.id}" ${filter != null && filter.productId != null && filter.productId == p.id ? 'selected="selected"' : ''}>
                            <c:out value="${p.name}"/> (ID: ${p.id})
                        </option>
                    </c:forEach>
                </select>
            </div>

            <div class="col-md-3">
                <label class="form-label">Nhà cung cấp</label>
                <select class="form-select" name="supplierId">
                    <option value="">-- Tất cả nhà cung cấp --</option>
                    <c:forEach items="${suppliers}" var="s">
                        <option value="${s.id}" ${filter != null && filter.supplierId != null && filter.supplierId == s.id ? 'selected="selected"' : ''}>
                            <c:out value="${s.name}"/>
                        </option>
                    </c:forEach>
                </select>
            </div>

            <div class="col-md-2">
                <label class="form-label">Trạng thái</label>
                <select class="form-select" name="status">
                    <option value="">-- Tất cả --</option>
                    <option value="AVAILABLE" ${filter != null && filter.status == 'AVAILABLE' ? 'selected="selected"' : ''}>Khả dụng</option>
                    <option value="EXPIRING" ${filter != null && filter.status == 'EXPIRING' ? 'selected="selected"' : ''}>Sắp hết hạn</option>
                    <option value="EXPIRED" ${filter != null && filter.status == 'EXPIRED' ? 'selected="selected"' : ''}>Hết hạn</option>
                    <option value="CONSUMED" ${filter != null && filter.status == 'CONSUMED' ? 'selected="selected"' : ''}>Đã dùng hết</option>
                </select>
            </div>

            <div class="col-md-2">
                <label class="form-label">Tồn từ</label>
                <input type="number" class="form-control" name="minQtyLeft"
                       value="${filter != null ? filter.minQtyLeft : ''}" min="0">
            </div>

            <div class="col-md-2">
                <label class="form-label">Tồn đến</label>
                <input type="number" class="form-control" name="maxQtyLeft"
                       value="${filter != null ? filter.maxQtyLeft : ''}" min="0">
            </div>

            <div class="col-md-3">
                <label class="form-label">Ngày nhập từ</label>
                <input type="date" class="form-control" name="importFrom"
                       value="${filter != null ? filter.importFrom : ''}">
            </div>

            <div class="col-md-3">
                <label class="form-label">Ngày nhập đến</label>
                <input type="date" class="form-control" name="importTo"
                       value="${filter != null ? filter.importTo : ''}">
            </div>

            <div class="col-md-3">
                <label class="form-label">HSD từ</label>
                <input type="date" class="form-control" name="expiryFrom"
                       value="${filter != null ? filter.expiryFrom : ''}">
            </div>

            <div class="col-md-3">
                <label class="form-label">HSD đến</label>
                <input type="date" class="form-control" name="expiryTo"
                       value="${filter != null ? filter.expiryTo : ''}">
            </div>

            <div class="col-md-12 d-flex align-items-end gap-2">
                <button class="btn btn-primary" type="submit">Lọc dữ liệu</button>
                <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/staff/inventory">Reset</a>
                <a class="btn btn-outline-primary" href="${pageContext.request.contextPath}/staff/import-lot">+ Nhập lô</a>
            </div>
        </form>
    </div>
</div>

<c:if test="${not empty errorMessage}">
    <div class="alert alert-danger">
        <c:out value="${errorMessage}"/>
    </div>
</c:if>

<c:if test="${not empty filteredLots}">
    <div class="card mb-3">
        <div class="card-header">
            Kết quả lọc lô hàng (${filteredCount} lô)
        </div>
        <div class="card-body">
            <table class="table table-sm table-bordered align-middle">
                <thead>
                <tr>
                    <th>Lô ID</th>
                    <th>Sản phẩm</th>
                    <th>Nhà cung cấp</th>
                    <th>Ngày nhập</th>
                    <th>HSD</th>
                    <th>Nhập</th>
                    <th>Còn lại</th>
                    <th>Đã dùng</th>
                    <th>Giá nhập</th>
                    <th>Trạng thái</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach items="${filteredLots}" var="lot">
                    <c:set var="status" value="AVAILABLE"/>
                    <c:if test="${lot.expiryDate lt today}">
                        <c:set var="status" value="EXPIRED"/>
                    </c:if>
                    <c:if test="${lot.qtyLeft == 0}">
                        <c:set var="status" value="CONSUMED"/>
                    </c:if>
                    <c:if test="${lot.qtyLeft > 0 && lot.expiryDate ge today && (lot.expiryDate.toEpochDay() - today.toEpochDay()) <= 7}">
                        <c:set var="status" value="EXPIRING"/>
                    </c:if>

                    <tr>
                        <td><c:out value="${lot.id}"/></td>
                        <td><c:out value="${lot.product.name}"/></td>
                        <td><c:out value="${lot.supplier != null ? lot.supplier.name : '-'}"/></td>
                        <td><c:out value="${lot.importDate}"/></td>
                        <td><c:out value="${lot.expiryDate}"/></td>
                        <td><c:out value="${lot.qtyIn}"/></td>
                        <td><strong><c:out value="${lot.qtyLeft}"/></strong></td>
                        <td><c:out value="${lot.qtyIn - lot.qtyLeft}"/></td>
                        <td><c:out value="${lot.importPrice}"/></td>
                        <td>
                            <c:choose>
                                <c:when test="${status == 'EXPIRED'}">
                                    <span class="badge bg-danger">Hết hạn</span>
                                </c:when>
                                <c:when test="${status == 'CONSUMED'}">
                                    <span class="badge bg-secondary">Đã dùng hết</span>
                                </c:when>
                                <c:when test="${status == 'EXPIRING'}">
                                    <span class="badge bg-warning text-dark">Sắp hết hạn</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="badge bg-success">Khả dụng</span>
                                </c:otherwise>
                            </c:choose>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </div>
</c:if>

<c:if test="${filteredLots != null && empty filteredLots}">
    <div class="alert alert-warning">
        Không tìm thấy lô nào khớp với điều kiện lọc.
    </div>
</c:if>

<c:if test="${selectedProduct != null}">
    <c:if test="${stockSummary.expiredQty > 0}">
        <div class="alert alert-warning mb-3">
            <strong>⚠️ Cảnh báo:</strong>
            Có <strong><c:out value="${stockSummary.expiredQty}"/></strong> đơn vị sản phẩm
            <strong><c:out value="${selectedProduct.name}"/></strong> đã hết hạn còn tồn kho. Vui lòng xử lý.
        </div>
    </c:if>

    <div class="row mb-3">
        <div class="col-md-3">
            <div class="card text-center">
                <div class="card-body">
                    <h6 class="text-muted">Tổng nhập</h6>
                    <h3><c:out value="${stockSummary.totalIn}"/></h3>
                </div>
            </div>
        </div>

        <div class="col-md-3">
            <div class="card text-center">
                <div class="card-body">
                    <h6 class="text-muted">Còn dùng được</h6>
                    <h3 class="text-success"><c:out value="${stockSummary.availableQty}"/></h3>
                </div>
            </div>
        </div>

        <div class="col-md-3">
            <div class="card text-center">
                <div class="card-body">
                    <h6 class="text-muted">Đã dùng</h6>
                    <h3 class="text-info"><c:out value="${stockSummary.consumedQty}"/></h3>
                </div>
            </div>
        </div>

        <div class="col-md-3">
            <div class="card text-center">
                <div class="card-body">
                    <h6 class="text-muted">HSD gần nhất</h6>
                    <c:choose>
                        <c:when test="${stockSummary.nearestExpiry != null}">
                            <h5><c:out value="${stockSummary.nearestExpiry}"/></h5>
                        </c:when>
                        <c:otherwise>
                            <h5 class="text-muted">-</h5>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </div>
    </div>

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
                        <th>Hành động</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach items="${availableLots}" var="lot">
                        <c:set var="daysLeft" value="${lot.expiryDate.toEpochDay() - today.toEpochDay()}"/>
                        <c:set var="rowClass" value=""/>
                        <c:if test="${daysLeft <= 3}">
                            <c:set var="rowClass" value="table-warning"/>
                        </c:if>

                        <tr class="${rowClass}">
                            <td><c:out value="${lot.id}"/></td>
                            <td><c:out value="${lot.importDate}"/></td>
                            <td><c:out value="${lot.expiryDate}"/></td>
                            <td>
                                <c:choose>
                                    <c:when test="${daysLeft == 0}">
                                        <span class="badge bg-danger">HSD hôm nay - bán ngay</span>
                                    </c:when>
                                    <c:when test="${daysLeft < 0}">
                                        <span class="badge bg-danger">Đã hết hạn</span>
                                    </c:when>
                                    <c:when test="${daysLeft <= 3}">
                                        <span class="badge bg-warning"><c:out value="${daysLeft}"/> ngày</span>
                                    </c:when>
                                    <c:otherwise>
                                        <c:out value="${daysLeft}"/> ngày
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td><c:out value="${lot.qtyIn}"/></td>
                            <td><strong><c:out value="${lot.qtyLeft}"/></strong></td>
                            <td><c:out value="${lot.qtyIn - lot.qtyLeft}"/></td>
                            <td><c:out value="${lot.importPrice}"/></td>
                            <td>
                                <a class="btn btn-sm btn-outline-primary"
                                   href="${pageContext.request.contextPath}/staff/import-lot?id=${lot.id}">
                                    Sửa
                                </a>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </c:if>
        </div>
    </div>

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
                        <th>Hành động</th>
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
                                    <c:when test="${lot.qtyLeft > 0}">
                                        Cần loại bỏ ngay
                                    </c:when>
                                    <c:otherwise>
                                        Đã dùng hết
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td>
                                <form action="${pageContext.request.contextPath}/staff/delete-lot" method="post" class="d-inline">
                                    <input type="hidden" name="csrf_token" value="${sessionScope.CSRF_TOKEN}"/>
                                    <input type="hidden" name="lotId" value="${lot.id}"/>
                                    <input type="hidden" name="redirect" value="/staff/inventory?productId=${selectedProduct.id}"/>
                                    <button class="btn btn-sm btn-danger" onclick="return confirm('Xác nhận loại bỏ lô này?');">
                                        Loại bỏ
                                    </button>
                                </form>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>
    </c:if>

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
                    <th>Hành động</th>
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

                    <c:set var="rowClass" value=""/>
                    <c:if test="${status == 'expired'}">
                        <c:set var="rowClass" value="table-danger"/>
                    </c:if>
                    <c:if test="${status == 'consumed'}">
                        <c:set var="rowClass" value="table-secondary"/>
                    </c:if>

                    <tr class="${rowClass}">
                        <td><c:out value="${lot.id}"/></td>
                        <td><c:out value="${lot.supplier != null ? lot.supplier.name : '-'}"/></td>
                        <td><c:out value="${lot.importDate}"/></td>
                        <td><c:out value="${lot.expiryDate}"/></td>
                        <td><c:out value="${lot.qtyIn}"/></td>
                        <td><c:out value="${lot.qtyLeft}"/></td>
                        <td><c:out value="${lot.importPrice}"/></td>
                        <td>
                            <c:choose>
                                <c:when test="${status == 'expired'}">
                                    <span class="badge bg-danger">Hết hạn</span>
                                </c:when>
                                <c:when test="${status == 'consumed'}">
                                    <span class="badge bg-secondary">Đã dùng hết</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="badge bg-success">Khả dụng</span>
                                </c:otherwise>
                            </c:choose>
                        </td>
                        <td>
                            <a class="btn btn-sm btn-outline-primary"
                               href="${pageContext.request.contextPath}/staff/import-lot?id=${lot.id}">
                                Sửa
                            </a>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </div>
</c:if>

<c:if test="${selectedProduct == null}">
    <div class="alert alert-info">
        Vui lòng chọn một sản phẩm để xem chi tiết tồn kho.
    </div>
</c:if>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>