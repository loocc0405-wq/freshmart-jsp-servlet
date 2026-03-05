<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Seller POS"/>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"/>

<h3>Seller POS (Bán tại quầy)</h3>

<div class="row">
    <div class="col-lg-7">
        <div class="card mb-3">
            <div class="card-header">Chọn sản phẩm</div>
            <div class="card-body">
                <table class="table table-sm align-middle">
                    <thead>
                    <tr>
                        <th>ID</th>
                        <th>Tên</th>
                        <th>Giá</th>
                        <th>Tồn</th>
                        <th>HSD gần nhất</th>
                        <th>Thêm</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach items="${products}" var="p">
                        <tr>
                            <td><c:out value="${p.id}"/></td>
                            <td><c:out value="${p.name}"/></td>
                            <td><c:out value="${p.sellPrice}"/></td>
                            <td><c:out value="${availableMap[p.id]}"/></td>
                            <td><c:out value="${nearestExpiryMap[p.id]}"/></td>
                            <td>
                                <form class="d-flex gap-2" method="post" action="${pageContext.request.contextPath}/seller/pos">
                                    <input type="hidden" name="csrf_token" value="${sessionScope.CSRF_TOKEN}" />
                                    <input type="hidden" name="productId" value="${p.id}"/>
                                    <input class="form-control form-control-sm" style="width: 90px" name="quantity" type="number" min="1" value="1"/>
                                    <button class="btn btn-sm btn-primary" type="submit">Add</button>
                                </form>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>
    </div>

    <div class="col-lg-5">
        <div class="card mb-3">
            <div class="card-header">Giỏ POS</div>
            <div class="card-body">
                <c:if test="${empty lines}">
                    <div class="text-muted">Chưa có sản phẩm.</div>
                </c:if>

                <c:if test="${not empty lines}">
                    <table class="table table-sm">
                        <thead>
                        <tr>
                            <th>Sản phẩm</th>
                            <th>SL</th>
                            <th>Thành tiền</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach items="${lines}" var="l">
                            <tr>
                                <td><c:out value="${l.product.name}"/></td>
                                <td><c:out value="${l.quantity}"/></td>
                                <td><c:out value="${l.lineTotal}"/></td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>

                    <div class="d-flex justify-content-between">
                        <span><b>Tổng</b></span>
                        <span><b><c:out value="${total}"/></b></span>
                    </div>

                    <hr/>

                    <form method="post" action="${pageContext.request.contextPath}/seller/pos/checkout" class="d-flex gap-2">
                        <input type="hidden" name="csrf_token" value="${sessionScope.CSRF_TOKEN}" />
                        <select class="form-select" name="paymentMethod">
                            <option value="CASH">Tiền mặt</option>
                            <option value="BANK_TRANSFER">Chuyển khoản</option>
                            <option value="QR">QR</option>
                        </select>
                        <button class="btn btn-success" type="submit">Checkout (COMPLETED)</button>
                    </form>

                    <form method="post" action="${pageContext.request.contextPath}/seller/pos/clear" class="mt-2">
                        <input type="hidden" name="csrf_token" value="${sessionScope.CSRF_TOKEN}" />
                        <button class="btn btn-outline-danger w-100" type="submit">Clear cart</button>
                    </form>
                </c:if>
            </div>
        </div>

        <div class="alert alert-info">
            <b>Note:</b> Khi checkout, hệ thống sẽ trừ tồn kho theo <b>FEFO</b> và cập nhật <b>revenue_daily</b>.
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>

