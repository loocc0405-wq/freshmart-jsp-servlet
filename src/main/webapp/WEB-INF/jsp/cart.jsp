<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:set var="pageTitle" value="Giỏ hàng"/>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"/>

<div class="fm-page-header">
    <div>
        <h1 class="fm-page-title"><i class="bi bi-cart3 me-2"></i>Giỏ hàng</h1>
        <p class="fm-page-subtitle">Kiểm tra số lượng, cập nhật giỏ và tiếp tục mua sắm.</p>
    </div>

    <a href="${pageContext.request.contextPath}/catalog" class="btn btn-outline-secondary">
        <i class="bi bi-arrow-left me-1"></i>Tiếp tục mua sắm
    </a>
</div>

<c:choose>
    <c:when test="${empty items}">
        <div class="fm-surface padded text-center">
            <div class="mb-2" style="font-size: 2.25rem;"><i class="bi bi-bag"></i></div>
            <div class="fw-bold">Giỏ hàng đang trống</div>
            <div class="fm-muted small mb-3">Hãy quay lại catalog và thêm sản phẩm bạn muốn mua.</div>
            <a href="${pageContext.request.contextPath}/catalog" class="btn btn-primary">
                <i class="bi bi-basket2 me-1"></i>Đi đến Catalog
            </a>
        </div>
    </c:when>

    <c:otherwise>
        <div class="fm-surface overflow-hidden">
            <div class="table-responsive">
                <table class="table fm-table align-middle mb-0">
                    <thead>
                    <tr>
                        <th>Sản phẩm</th>
                        <th class="text-end">Đơn giá</th>
                        <th style="width: 260px;">Số lượng</th>
                        <th class="text-end">Thành tiền</th>
                        <th style="width: 70px;" class="text-center">Xóa</th>
                    </tr>
                    </thead>
                    <tbody>

                    <c:set var="grandTotal" value="0"/>

                    <c:forEach var="item" items="${items}">
                        <c:set var="lineTotal" value="${item.product.sellPrice * item.quantity}"/>
                        <c:set var="grandTotal" value="${grandTotal + lineTotal}"/>

                        <tr>
                            <td>
                                <div class="fw-semibold">${item.product.name}</div>
                                <div class="small fm-muted">${item.product.category} • ${item.product.unit}</div>
                            </td>

                            <td class="text-end">
                                <span class="fw-semibold">
                                    <fmt:formatNumber value="${item.product.sellPrice}" type="number" groupingUsed="true" maxFractionDigits="2"/>
                                </span>
                                <span class="fm-muted">₫</span>
                            </td>

                            <td>
                                <form action="${pageContext.request.contextPath}/cart" method="post" class="d-flex gap-2">
                                    <input type="hidden" name="action" value="update"/>
                                    <input type="hidden" name="productId" value="${item.product.id}"/>

                                    <div class="input-group input-group-sm" style="max-width: 150px;">
                                        <span class="input-group-text">SL</span>
                                        <input type="number" name="qty" value="${item.quantity}" min="1" class="form-control"/>
                                    </div>

                                    <button class="btn btn-outline-primary btn-sm" type="submit">
                                        <i class="bi bi-arrow-repeat me-1"></i>Cập nhật
                                    </button>
                                </form>
                            </td>

                            <td class="text-end">
                                <span class="fw-bold">
                                    <fmt:formatNumber value="${lineTotal}" type="number" groupingUsed="true" maxFractionDigits="2"/>
                                </span>
                                <span class="fm-muted">₫</span>
                            </td>

                            <td class="text-center">
                                <form action="${pageContext.request.contextPath}/cart" method="post" class="m-0">
                                    <input type="hidden" name="action" value="remove"/>
                                    <input type="hidden" name="productId" value="${item.product.id}"/>
                                    <button class="btn btn-outline-danger btn-sm" type="submit" title="Xóa khỏi giỏ">
                                        <i class="bi bi-trash"></i>
                                    </button>
                                </form>
                            </td>
                        </tr>
                    </c:forEach>

                    </tbody>
                </table>
            </div>

            <div class="p-3 border-top d-flex flex-wrap justify-content-between align-items-center gap-2">
                <div class="fm-muted small">* Giá hiển thị theo đơn vị sản phẩm.</div>
                <div class="text-end">
                    <div class="small fm-muted">Tổng cộng</div>
                    <div style="font-size: 1.25rem;" class="fw-bold">
                        <fmt:formatNumber value="${grandTotal}" type="number" groupingUsed="true" maxFractionDigits="2"/>
                        <span class="fm-muted">₫</span>
                    </div>
                </div>
            </div>
        </div>
    </c:otherwise>
</c:choose>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
