<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="Product Detail"/>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"/>

<c:if test="${product == null}">
    <div class="alert alert-warning">Không tìm thấy sản phẩm.</div>
</c:if>

<c:if test="${product != null}">
    <div class="fm-page-header">
        <div>
            <h1 class="fm-page-title"><c:out value="${product.name}"/></h1>
            <p class="fm-page-subtitle">Thông tin chi tiết sản phẩm và tồn kho khả dụng.</p>
        </div>

        <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/catalog">
            <i class="bi bi-arrow-left me-1"></i>Quay lại
        </a>
    </div>

    <div class="row g-4">
        <div class="col-12 col-md-5">
            <div class="fm-surface overflow-hidden">
                <div class="fm-product-media">
                    <c:choose>
                        <c:when test="${not empty product.imageUrl}">
                            <img src="${product.imageUrl}" alt="${product.name}"/>
                        </c:when>
                        <c:otherwise>
                            <div class="text-center">
                                <i class="bi bi-image" style="font-size: 2.25rem;"></i>
                                <div class="small">Chưa có ảnh</div>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </div>

        <div class="col-12 col-md-7">
            <div class="fm-surface padded">
                <div class="d-flex flex-wrap align-items-center gap-2 mb-3">
                    <span class="badge rounded-pill fm-badge"><c:out value="${product.category}"/></span>
                    <span class="small fm-muted">Đơn vị: <c:out value="${product.unit}"/></span>
                </div>

                <div class="d-flex align-items-baseline justify-content-between mb-2">
                    <div>
                        <span class="fm-price" style="font-size: 1.5rem;">
                            <fmt:formatNumber value="${product.sellPrice}" type="number" groupingUsed="true" maxFractionDigits="2"/>
                        </span>
                        <span class="fm-muted">₫</span>
                    </div>
                    <div class="text-end">
                        <div class="small fm-muted">Tồn kho khả dụng</div>
                        <div class="fw-bold"><c:out value="${availableQty}"/></div>
                    </div>
                </div>

                <c:if test="${not empty product.description}">
                    <hr class="fm-divider"/>
                    <div class="small fm-muted mb-2">Mô tả</div>
                    <div>
                        <c:out value="${product.description}"/>
                    </div>
                </c:if>
            </div>
        </div>
    </div>
</c:if>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
