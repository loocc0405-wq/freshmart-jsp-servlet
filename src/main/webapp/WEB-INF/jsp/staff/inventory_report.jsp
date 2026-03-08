<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Inventory Report"/>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"/>

<h3>Báo cáo Tồn Kho</h3>

<div class="card mb-3">
    <div class="card-body">
        <form method="get" action="${pageContext.request.contextPath}/staff/inventory-report" class="row g-2">

            <div class="col-md-3">
                <label class="form-label">Sản phẩm</label>
                <select class="form-select" name="productId">
                    <option value="">-- Tất cả sản phẩm --</option>
                    <c:forEach items="${products}" var="p">
                        <option value="${p.id}"
                            <c:if test="${filter != null && filter.productId != null && filter.productId == p.id}">selected</c:if>>
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
                        <option value="${s.id}"
                            <c:if test="${filter != null && filter.supplierId != null && filter.supplierId == s.id}">selected</c:if>>
                            <c:out value="${s.name}"/>
                        </option>
                    </c:forEach>
                </select>
            </div>

            <div class="col-md-2">
                <label class="form-label">Trạng thái lot</label>
                <select class="form-select" name="status">
                    <option value="">-- Tất cả --</option>
                    <option value="AVAILABLE" ${filter != null && filter.status == 'AVAILABLE' ? 'selected' : ''}>Khả dụng</option>
                    <option value="EXPIRING" ${filter != null && filter.status == 'EXPIRING' ? 'selected' : ''}>Sắp hết hạn</option>
                    <option value="EXPIRED" ${filter != null && filter.status == 'EXPIRED' ? 'selected' : ''}>Hết hạn</option>
                    <option value="CONSUMED" ${filter != null && filter.status == 'CONSUMED' ? 'selected' : ''}>Đã dùng hết</option>
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

            <div class="col-md-4 d-flex align-items-end">
                <button class="btn btn-primary me-2" type="submit">Lọc báo cáo</button>
                <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/staff/inventory-report">Reset</a>
            </div>
        </form>
    </div>
</div>

<c:if test="${not empty errorMessage}">
    <div class="alert alert-danger mb-3">
        <c:out value="${errorMessage}"/>
    </div>
</c:if>

<c:if test="${filter != null && (
    filter.productId != null ||
    filter.supplierId != null ||
    filter.status != null ||
    filter.importFrom != null ||
    filter.importTo != null ||
    filter.expiryFrom != null ||
    filter.expiryTo != null ||
    filter.minQtyLeft != null ||
    filter.maxQtyLeft != null
)}">
    <div class="alert alert-info">
        Báo cáo đang hiển thị theo bộ lọc đã chọn.
    </div>
</c:if>

<div class="row mb-3">
    <div class="col-md-3">
        <div class="card text-center bg-primary text-white">
            <div class="card-body">
                <h6>Tổng giá trị tồn kho</h6>
                <h4>${totalInventoryValue}</h4>
            </div>
        </div>
    </div>
    <div class="col-md-3">
        <div class="card text-center bg-info text-white">
            <div class="card-body">
                <h6>Tổng lô hoạt động</h6>
                <h4>${totalActiveLots}</h4>
            </div>
        </div>
    </div>
    <div class="col-md-3">
        <div class="card text-center bg-warning text-white">
            <div class="card-body">
                <h6>Hàng sắp hết hạn (${upcomingExpiryDays} ngày)</h6>
                <h4>${upcomingExpiryCount}</h4>
            </div>
        </div>
    </div>
    <div class="col-md-3">
        <div class="card text-center bg-danger text-white">
            <div class="card-body">
                <h6>Lô hết hạn</h6>
                <h4>${expiredLotsCount}</h4>
            </div>
        </div>
    </div>
</div>

<!-- Tabs for different reports -->
<ul class="nav nav-tabs mb-3">
    <li class="nav-item">
        <a class="nav-link active" data-bs-toggle="tab" href="#all-products">Toàn bộ sản phẩm</a>
    </li>
    <li class="nav-item">
        <a class="nav-link" data-bs-toggle="tab" href="#low-stock">Hàng ít tồn kho</a>
    </li>
    <li class="nav-item">
        <a class="nav-link" data-bs-toggle="tab" href="#upcoming-expiry">Sắp hết hạn</a>
    </li>
    <li class="nav-item">
        <a class="nav-link" data-bs-toggle="tab" href="#expired">Đã hết hạn</a>
    </li>
</ul>

<div class="tab-content">
    <!-- All Products -->
    <div id="all-products" class="tab-pane fade show active">
        <div class="card">
            <div class="card-body">
                <table class="table table-hover">
                    <thead>
                    <tr>
                        <th>Sản phẩm</th>
                        <th>Tổng nhập</th>
                        <th>Còn lại</th>
                        <th>Đã dùng</th>
                        <th>Số lô</th>
                        <th>HSD gần nhất</th>
                        <th>Giá trị tồn kho</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach items="${allProductsOverview}" var="o">
                        <tr>
                            <td><c:out value="${o.productName}"/></td>
                            <td><c:out value="${o.totalQtyIn}"/></td>
                            <td><c:out value="${o.totalQtyLeft}"/></td>
                            <td><c:out value="${o.totalQtyConsumed}"/></td>
                            <td><span class="badge bg-secondary"><c:out value="${o.lotsCount}"/></span></td>
                            <td><c:out value="${o.nearestExpiry}"/></td>
                            <td><c:out value="${o.totalValue}"/></td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>
    </div>

    <!-- Low Stock -->
    <div id="low-stock" class="tab-pane fade">
        <div class="card">
            <div class="card-header">Sản phẩm có số lượng tồn kho dưới ${lowStockThreshold} đơn vị</div>
            <div class="card-body">
                <c:if test="${empty lowStockProducts}">
                    <div class="text-muted">Không có sản phẩm với tồn kho thấp.</div>
                </c:if>
                <c:if test="${not empty lowStockProducts}">
                    <table class="table table-warning">
                        <thead>
                        <tr>
                            <th>Sản phẩm</th>
                            <th>Số lượng còn lại</th>
                            <th>Hành động</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach items="${lowStockProducts}" var="o">
                            <tr>
                                <td><c:out value="${o.productName}"/></td>
                                <td><c:out value="${o.totalQtyLeft}"/></td>
                                <td><a class="btn btn-sm btn-primary" href="${pageContext.request.contextPath}/staff/import-lot">Nhập thêm</a></td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </c:if>
            </div>
        </div>
    </div>

    <!-- Upcoming Expiry -->
    <div id="upcoming-expiry" class="tab-pane fade">
        <div class="card">
            <div class="card-header bg-warning">Sản phẩm sắp hết hạn (${upcomingExpiryDays} ngày tới)</div>
            <div class="card-body">
                <c:if test="${empty upcomingExpiryProducts}">
                    <div class="text-muted">Không có sản phẩm sắp hết hạn.</div>
                </c:if>
                <c:if test="${not empty upcomingExpiryProducts}">
                    <table class="table table-warning">
                        <thead>
                        <tr>
                            <th>Sản phẩm</th>
                            <th>HSD gần nhất</th>
                            <th>Số lượng</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach items="${upcomingExpiryProducts}" var="o">
                            <tr>
                                <td><c:out value="${o.productName}"/></td>
                                <td><c:out value="${o.nearestExpiry}"/></td>
                                <td><c:out value="${o.totalQtyLeft}"/></td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </c:if>
            </div>
        </div>
    </div>

    <!-- Expired Lots -->
    <div id="expired" class="tab-pane fade">
        <div class="card">
            <div class="card-header bg-danger text-white">Lô đã hết hạn (cần loại bỏ)</div>
            <div class="card-body">
                <c:if test="${empty expiredLots}">
                    <div class="alert alert-success">Không có lô nào hết hạn.</div>
                </c:if>
                <c:if test="${not empty expiredLots}">
                    <table class="table table-danger table-sm">
                        <thead>
                        <tr>
                            <th>Lô ID</th>
                            <th>Sản phẩm</th>
                            <th>HSD</th>
                            <th>Số lượng còn lại</th>
                            <th>Hành động</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach items="${expiredLots}" var="lot">
                            <tr>
                                <td><c:out value="${lot.id}"/></td>
                                <td><c:out value="${lot.product.name}"/></td>
                                <td><c:out value="${lot.expiryDate}"/></td>
                                <td><c:out value="${lot.qtyLeft}"/></td>
                                <td>
                                    <form action="${pageContext.request.contextPath}/staff/delete-lot" method="post" class="d-inline">
                                        <input type="hidden" name="csrf_token" value="${sessionScope.CSRF_TOKEN}"/>
                                        <input type="hidden" name="lotId" value="${lot.id}"/>
                                        <input type="hidden" name="redirect" value="/staff/inventory-report"/>
                                        <button class="btn btn-sm btn-danger" onclick="return confirm('Xác nhận loại bỏ lô này?');">Loại bỏ</button>
                                    </form>
                                </td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </c:if>
            </div>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
