<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Invoice"/>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"/>

<c:if test="${order == null}">
    <div class="alert alert-warning">Không có dữ liệu đơn hàng.</div>
</c:if>

<c:if test="${order != null}">
    <div class="d-flex justify-content-between align-items-center">
        <h3>Hóa đơn</h3>
        <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/seller/pos">Back to POS</a>
    </div>

    <div class="card mb-3">
        <div class="card-body">
            <div>Mã đơn: <b><c:out value="${order.orderCode}"/></b></div>
            <div>Trạng thái: <b><c:out value="${order.status}"/></b></div>
            <div>Thanh toán: <b><c:out value="${order.paymentMethod}"/></b></div>
            <div>Thời gian: <b><c:out value="${order.completedAt}"/></b></div>
        </div>
    </div>

    <table class="table table-bordered">
        <thead>
        <tr>
            <th>Sản phẩm</th>
            <th>SL</th>
            <th>Đơn giá</th>
            <th>Thành tiền</th>
        </tr>
        </thead>
        <tbody>
        <c:forEach items="${order.items}" var="it">
            <tr>
                <td><c:out value="${it.product.name}"/></td>
                <td><c:out value="${it.quantity}"/></td>
                <td><c:out value="${it.unitPrice}"/></td>
                <td><c:out value="${it.lineTotal}"/></td>
            </tr>
        </c:forEach>
        </tbody>
        <tfoot>
        <tr>
            <th colspan="3" class="text-end">Tổng</th>
            <th><c:out value="${order.totalAmount}"/></th>
        </tr>
        </tfoot>
    </table>
</c:if>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
