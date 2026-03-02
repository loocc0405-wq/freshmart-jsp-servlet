<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Staff"/>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"/>

<h3>Staff Home - Quản lý kho hàng</h3>

<div class="row g-3">
    <!-- Inventory Management -->
    <div class="col-md-6">
        <div class="card border-primary">
            <div class="card-header bg-primary text-white">
                <h5 class="mb-0">📦 Quản lý tồn kho (FEFO)</h5>
            </div>
            <div class="card-body">
                <p class="card-text">Quản lý thị trường sản phẩm theo lô (batch), theo dõi hạn sử dụng, ưu tiên bán hàng sắp hết hạn.</p>
                <div class="d-flex gap-2">
                    <a class="btn btn-primary btn-sm" href="${pageContext.request.contextPath}/staff/import-lot">
                        ➕ Nhập lô hàng
                    </a>
                    <a class="btn btn-info btn-sm" href="${pageContext.request.contextPath}/staff/inventory">
                        👁️ Xem tồn kho
                    </a>
                    <a class="btn btn-warning btn-sm" href="${pageContext.request.contextPath}/staff/inventory-report">
                        📊 Báo cáo
                    </a>
                </div>
            </div>
        </div>
    </div>

    <!-- Sales & POS -->
    <div class="col-md-6">
        <div class="card border-success">
            <div class="card-header bg-success text-white">
                <h5 class="mb-0">🛒 Bán hàng (POS)</h5>
            </div>
            <div class="card-body">
                <p class="card-text">Hệ thống bán tại quầy, tự động trừ tồn kho theo FEFO, cập nhật doanh thu.</p>
                <div class="d-flex gap-2">
                    <a class="btn btn-success btn-sm" href="${pageContext.request.contextPath}/seller/pos">
                        💼 Seller POS
                    </a>
                    <a class="btn btn-info btn-sm" href="${pageContext.request.contextPath}/pro/dashboard">
                        📈 Dashboard doanh thu
                    </a>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- Info Cards -->
<div class="row g-3 mt-3">
    <div class="col-md-4">
        <div class="card text-center">
            <div class="card-body">
                <h6 class="text-muted">FEFO Logic</h6>
                <p class="card-text">
                    <small>Tự động sử dụng lô sắp hết hạn trước để giảm lãng phí</small>
                </p>
            </div>
        </div>
    </div>
    <div class="col-md-4">
        <div class="card text-center">
            <div class="card-body">
                <h6 class="text-muted">Chi phí rõ ràng</h6>
                <p class="card-text">
                    <small>Theo dõi giá nhập từng lô, tính toán giá thành chính xác</small>
                </p>
            </div>
        </div>
    </div>
    <div class="col-md-4">
        <div class="card text-center">
            <div class="card-body">
                <h6 class="text-muted">Báo cáo chi tiết</h6>
                <p class="card-text">
                    <small>Xem chi tiết tồn kho, cảnh báo HSD, dự báo doanh thu</small>
                </p>
            </div>
        </div>
    </div>
</div>

<!-- Quick Info -->
<div class="alert alert-info mt-3">
    <h5>💡 Hệ thống FEFO (First Expired, First Out)</h5>
    <p class="mb-0">
        <strong>Nguyên tắc:</strong> Khi bán hàng, hệ thống tự động chọn lô sắp hết hạn trước (HSD sớm nhất). 
        Điều này giúp giảm lãng phí, đảm bảo chất lượng sản phẩm, và tối ưu hóa chi phí.
    </p>
    <hr/>
    <p class="mb-0">
        <strong>Ví dụ:</strong> Nếu bán 30 kg rau muống và có 2 lô:
        <br/>- Lô A: 50 kg, HSD 3 ngày
        <br/>- Lô B: 80 kg, HSD 7 ngày
        <br/>→ Hệ thống sẽ lấy từ Lô A trước (HSD sớm hơn)
    </p>
</div>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
