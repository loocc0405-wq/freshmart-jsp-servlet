<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Import Lot"/>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"/>

<h3>Nhập lô sản phẩm (Import Stock)</h3>

<c:if test="${not empty successMessage}">
    <div class="alert alert-success"><c:out value="${successMessage}"/></div>
</c:if>

<c:if test="${not empty errorMessage}">
    <div class="alert alert-danger"><c:out value="${errorMessage}"/></div>
</c:if>

<div class="row">
    <div class="col-lg-6">
        <form method="post" action="${pageContext.request.contextPath}/staff/import-lot" class="card">
            <div class="card-body">
                <div class="mb-3">
                    <label class="form-label">Sản phẩm *</label>
                    <select class="form-select" name="productId" required>
                        <option value="">-- Chọn sản phẩm --</option>
                        <c:forEach items="${products}" var="p">
                            <option value="${p.id}"><c:out value="${p.name}"/> (ID: ${p.id})</option>
                        </c:forEach>
                    </select>
                </div>

                <div class="mb-3">
                    <label class="form-label">Nhà cung cấp</label>
                    <select class="form-select" name="supplierId">
                        <option value="">-- Không chọn --</option>
                        <c:forEach items="${suppliers}" var="s">
                            <option value="${s.id}"><c:out value="${s.name}"/></option>
                        </c:forEach>
                    </select>
                </div>

                <div class="mb-3">
                    <label class="form-label">Ngày nhập *</label>
                    <input class="form-control" type="date" name="importDate" required/>
                </div>

                <div class="mb-3">
                    <label class="form-label">Ngày HSD (Hạn sử dụng) *</label>
                    <input class="form-control" type="date" name="expiryDate" required/>
                </div>

                <div class="mb-3">
                    <label class="form-label">Số lượng nhập *</label>
                    <input class="form-control" type="number" name="quantity" min="1" required/>
                </div>

                <div class="mb-3">
                    <label class="form-label">Giá nhập (đơn vị)</label>
                    <input class="form-control" type="number" step="0.01" name="importPrice" min="0"/>
                </div>

                <button class="btn btn-primary w-100" type="submit">Nhập lô</button>
                <a class="btn btn-outline-secondary w-100 mt-2" href="${pageContext.request.contextPath}/staff">Quay lại</a>
            </div>
        </form>
    </div>

    <div class="col-lg-6">
        <div class="card">
            <div class="card-header">Hướng dẫn</div>
            <div class="card-body">
                <p><strong>FEFO (First Expired, First Out):</strong></p>
                <ul>
                    <li>Lô hàng được quản lý theo từng đợt nhập</li>
                    <li>Khi bán hàng, hệ thống tự động chọn lô sắp hết hạn trước</li>
                    <li>Lô không còn hàng (qty_left = 0) sẽ không được chọn</li>
                    <li>Lô hết hạn (expiry_date < ngày hôm nay) sẽ không được dùng</li>
                </ul>

                <p><strong>Quy trình:</strong></p>
                <ol>
                    <li>Chọn sản phẩm, nhà cung cấp</li>
                    <li>Nhập ngày nhập và hạn sử dụng</li>
                    <li>Nhập số lượng và giá nhập</li>
                    <li>Nhấn "Nhập lô"</li>
                </ol>
            </div>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
