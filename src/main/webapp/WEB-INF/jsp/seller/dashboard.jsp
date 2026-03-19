<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="pageTitle" value="Seller POS Dashboard | FreshMart Enterprise"/>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"/>

<div class="container-fluid px-4 py-4">
    <div class="fm-page-header mb-5">
        <div>
            <div class="fm-caption fw-bold text-primary mb-1 text-uppercase">Seller workspace</div>
            <h1 class="fm-page-title">Smart POS Dashboard</h1>
            <p class="fm-page-subtitle">Landing page riêng cho seller để không còn bị điều hướng sang route /staff và gặp 403.</p>
        </div>
        <div class="d-flex flex-wrap gap-2">
            <a class="fm-btn fm-btn-primary" href="${pageContext.request.contextPath}/seller/pos">
                <i class="bi bi-upc-scan me-2"></i>Open Smart POS
            </a>
            <a class="fm-btn btn-light border" href="${pageContext.request.contextPath}/home">
                <i class="bi bi-house me-2"></i>Go Home
            </a>
        </div>
    </div>

    <div class="row g-4 mb-4">
        <div class="col-md-6 col-xl-4">
            <div class="fm-surface p-4 border-0 shadow-sm h-100">
                <div class="fm-caption fw-bold opacity-50 mb-2">CURRENT CART SKU</div>
                <div class="fm-h1 mb-0">${sellerSkuCount}</div>
                <div class="small text-muted mt-2">Số lượng SKU khác nhau đang nằm trong giỏ POS phiên hiện tại.</div>
            </div>
        </div>
        <div class="col-md-6 col-xl-4">
            <div class="fm-surface p-4 border-0 shadow-sm h-100 bg-primary text-white">
                <div class="fm-caption fw-bold opacity-75 mb-2 text-white">CURRENT CART ITEMS</div>
                <div class="fm-h1 mb-0">${sellerItemCount}</div>
                <div class="small mt-2 text-white-50">Tổng số đơn vị sản phẩm đang sẵn sàng checkout tại POS.</div>
            </div>
        </div>
        <div class="col-md-12 col-xl-4">
            <div class="fm-surface p-4 border-0 shadow-sm h-100">
                <div class="fm-caption fw-bold opacity-50 mb-2">ROLE STATUS</div>
                <div class="fm-h2 mb-2">${sessionScope.authUser.role}</div>
                <div class="small text-muted">Header, Home và Dashboard hiện đã được map đúng về seller workspace thay vì /staff.</div>
            </div>
        </div>
    </div>

    <div class="row g-4">
        <div class="col-lg-8">
            <div class="fm-surface p-4 border-0 shadow-sm h-100">
                <h2 class="fm-h3 mb-4">Primary actions</h2>
                <div class="row g-3">
                    <div class="col-md-6">
                        <div class="p-4 rounded-4 bg-light-subtle border h-100">
                            <div class="fw-bold mb-2"><i class="bi bi-shop me-2 text-primary"></i>Launch POS</div>
                            <p class="small text-muted mb-3">Mở ngay màn hình POS để thêm sản phẩm, kiểm tra tồn khả dụng và checkout đơn walk-in.</p>
                            <a class="btn btn-primary btn-sm" href="${pageContext.request.contextPath}/seller/pos">Open POS</a>
                        </div>
                    </div>
                    <div class="col-md-6">
                        <div class="p-4 rounded-4 bg-light-subtle border h-100">
                            <div class="fw-bold mb-2"><i class="bi bi-arrow-clockwise me-2 text-primary"></i>Reset current cart</div>
                            <p class="small text-muted mb-3">Làm sạch giỏ hiện tại và quay lại POS để bắt đầu phiên bán mới.</p>
                            <a class="btn btn-outline-secondary btn-sm" href="${pageContext.request.contextPath}/seller/pos/clear">Clear cart</a>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <div class="col-lg-4">
            <div class="fm-surface p-4 border-0 shadow-sm h-100">
                <h2 class="fm-h3 mb-3">Navigation fix summary</h2>
                <ul class="mb-0 small text-muted ps-3">
                    <li class="mb-2">Login seller hiện vào <strong>/seller/dashboard</strong>.</li>
                    <li class="mb-2">Nút Dashboard trong header không còn trỏ sang <strong>/staff</strong>.</li>
                    <li class="mb-2">Home theo role cũng trả seller về đúng workspace.</li>
                    <li class="mb-0">Seller vẫn dùng POS là luồng nghiệp vụ chính nên dashboard này chỉ đóng vai trò entry point ổn định.</li>
                </ul>
            </div>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
