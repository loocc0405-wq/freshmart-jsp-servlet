<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Inventory Report"/>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"/>

<h3>Báo cáo Tồn Kho</h3>

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
                <h6>Hàng hsakotonghạn (7 ngày)</h6>
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
            <div class="card-header">Sản phẩm có số lượng tồn kho dưới 50 đơn vị</div>
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
            <div class="card-header bg-warning">Sản phẩm sắp hết hạn (7 ngày tới)</div>
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
                <c:if test="${empty expiredProducts}">
                    <div class="alert alert-success">Không có lô nào hết hạn.</div>
                </c:if>
                <c:if test="${not empty expiredProducts}">
                    <table class="table table-danger">
                        <thead>
                        <tr>
                            <th>Sản phẩm</th>
                            <th>Số lô hết hạn</th>
                            <th>Ghi chú</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach items="${expiredProducts}" var="item">
                            <tr>
                                <td><c:out value="${item.productName}"/></td>
                                <td><c:out value="${item.count}"/></td>
                                <td>Cần loại bỏ sắp</td>
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
