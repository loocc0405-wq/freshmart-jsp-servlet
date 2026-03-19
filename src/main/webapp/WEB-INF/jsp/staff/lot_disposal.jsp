<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="pageTitle" value="Dispose Batch"/>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"/>

<div class="container py-4">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <div>
            <h3 class="mb-1">Tiêu hủy / loại bỏ lô</h3>
            <div class="text-muted small">Không xóa cứng dữ liệu. Hệ thống sẽ ghi lot_disposals và inventory_transactions.</div>
        </div>
        <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}${empty redirect ? '/staff/inventory-report' : redirect}">Quay lại</a>
    </div>

    <c:if test="${not empty errorMessage}">
        <div class="alert alert-danger"><c:out value="${errorMessage}"/></div>
    </c:if>

    <c:if test="${lot != null}">
        <div class="row g-4">
            <div class="col-lg-5">
                <div class="card shadow-sm border-0">
                    <div class="card-body">
                        <h5 class="mb-3">Thông tin lô</h5>
                        <dl class="row mb-0">
                            <dt class="col-5">Lot ID</dt>
                            <dd class="col-7">#<c:out value="${lot.id}"/></dd>
                            <dt class="col-5">Sản phẩm</dt>
                            <dd class="col-7"><c:out value="${lot.product.name}"/></dd>
                            <dt class="col-5">Nhà cung cấp</dt>
                            <dd class="col-7"><c:out value="${lot.supplier != null ? lot.supplier.name : 'N/A'}"/></dd>
                            <dt class="col-5">Ngày nhập</dt>
                            <dd class="col-7"><c:out value="${lot.importDate}"/></dd>
                            <dt class="col-5">Hạn dùng</dt>
                            <dd class="col-7"><c:out value="${lot.expiryDate}"/></dd>
                            <dt class="col-5">Qty còn lại</dt>
                            <dd class="col-7 fw-bold text-danger"><c:out value="${lot.qtyLeft}"/></dd>
                        </dl>
                    </div>
                </div>
            </div>
            <div class="col-lg-7">
                <div class="card shadow-sm border-0">
                    <div class="card-body">
                        <h5 class="mb-3">Ghi nhận tiêu hủy</h5>
                        <form method="post" action="${pageContext.request.contextPath}/staff/lot-disposals/new">
                            <input type="hidden" name="csrf_token" value="${sessionScope.CSRF_TOKEN}" />
                            <input type="hidden" name="lotId" value="${lot.id}" />
                            <input type="hidden" name="redirect" value="${empty redirect ? '/staff/inventory-report' : redirect}" />

                            <div class="mb-3">
                                <label class="form-label fw-bold">Số lượng tiêu hủy *</label>
                                <input type="number" min="1" max="${lot.qtyLeft}" name="disposeQty" class="form-control" value="${param.disposeQty != null ? param.disposeQty : lot.qtyLeft}" required>
                                <div class="form-text">Bạn có thể tiêu hủy một phần hoặc toàn bộ số lượng còn lại của lô.</div>
                            </div>

                            <div class="mb-3">
                                <label class="form-label fw-bold">Lý do *</label>
                                <input type="text" name="reason" class="form-control" maxlength="255" value="${param.reason}" placeholder="Ví dụ: Hết hạn, hư hỏng, rách bao bì..." required>
                            </div>

                            <div class="mb-3">
                                <label class="form-label fw-bold">Ghi chú thêm</label>
                                <textarea name="note" class="form-control" rows="4" maxlength="500" placeholder="Người kiểm kê, biên bản, mô tả hiện trạng...">${param.note}</textarea>
                            </div>

                            <div class="d-flex gap-2">
                                <button type="submit" class="btn btn-danger">Xác nhận tiêu hủy</button>
                                <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}${empty redirect ? '/staff/inventory-report' : redirect}">Hủy</a>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </c:if>
</div>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
