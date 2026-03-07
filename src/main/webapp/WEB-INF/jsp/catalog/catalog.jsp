<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="Catalog"/>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"/>

<div class="fm-page-header">
    <div>
        <h1 class="fm-page-title">Danh sách sản phẩm</h1>
        <p class="fm-page-subtitle">Tìm kiếm nhanh theo tên, lọc theo danh mục và thêm vào giỏ chỉ trong 1 bước.</p>
    </div>
</div>

<section class="fm-surface padded mb-4">
    <form class="row g-2 align-items-end" method="get" action="${pageContext.request.contextPath}/catalog">
        <div class="col-12 col-md-5">
            <label class="form-label small fm-muted">Tìm theo tên</label>
            <div class="input-group">
                <span class="input-group-text"><i class="bi bi-search"></i></span>
                <input class="form-control" name="q" placeholder="Ví dụ: cá hồi, tôm, rau..." value="${param.q}"/>
            </div>
        </div>

        <div class="col-12 col-md-3">
            <label class="form-label small fm-muted">Danh mục</label>
            <select class="form-select" name="category">
                <option value="">Tất cả danh mục</option>
                <c:forEach items="${categories}" var="c">
                    <option value="${c}" <c:if test="${param.category eq c}">selected</c:if>>
                        <c:out value="${c}"/>
                    </option>
                </c:forEach>
            </select>
        </div>
        <div class="col-12 col-md-3">
            <label class="form-label small fm-muted">Sắp xếp</label>
            <select class="form-select" name="sort">
                <option value="">Mặc định</option>
                <option value="name_asc" <c:if test="${param.sort eq 'name_asc'}">selected</c:if>>Tên A-Z</option>
                <option value="name_desc" <c:if test="${param.sort eq 'name_desc'}">selected</c:if>>Tên Z-A</option>
                <option value="price_asc" <c:if test="${param.sort eq 'price_asc'}">selected</c:if>>Giá tăng</option>
                <option value="price_desc" <c:if test="${param.sort eq 'price_desc'}">selected</c:if>>Giá giảm</option>
            </select>
        </div>

        <!-- preserve sort parameter when submitting -->
        <input type="hidden" name="sort" value="${param.sort}"/>
        <div class="col-12 col-md-3 d-flex gap-2">
            <button type="submit" class="btn btn-primary flex-grow-1">
                <i class="bi bi-search me-1"></i>Tìm
            </button>
            <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/catalog" title="Xóa bộ lọc">
                <i class="bi bi-x-circle"></i>
            </a>
        </div>
    </form>
</section>

<c:if test="${groupMode}">
    <c:forEach items="${groupedProducts}" var="entry">
        <div class="d-flex align-items-center gap-2 mt-4">
            <h2 class="h5 mb-0"><c:out value="${entry.key}"/></h2>
            <span class="badge rounded-pill fm-badge">${fn:length(entry.value)} SP</span>
        </div>
        <hr class="fm-divider"/>
        <div class="row g-3">
            <c:forEach items="${entry.value}" var="p">
                <div class="col-12 col-sm-6 col-lg-4">
                    <div class="card fm-product-card h-100">

                        <div class="fm-product-media">
                            <c:choose>
                                <c:when test="${not empty p.imageUrl}">
                                    <img src="${p.imageUrl}" alt="${p.name}"/>
                                </c:when>
                                <c:otherwise>
                                    <div class="text-center">
                                        <i class="bi bi-image" style="font-size: 2rem;"></i>
                                        <div class="small">Chưa có ảnh</div>
                                    </div>
                                </c:otherwise>
                            </c:choose>
                        </div>

                        <div class="card-body">
                            <div class="d-flex align-items-start justify-content-between gap-2">
                                <div>
                                    <h3 class="h6 mb-1"><c:out value="${p.name}"/></h3>
                                    <div class="small fm-muted">Đơn vị: <c:out value="${p.unit}"/></div>
                                </div>
                                <span class="badge rounded-pill fm-badge"><c:out value="${p.category}"/></span>
                            </div>

                            <div class="mt-3 d-flex align-items-baseline justify-content-between">
                                <div>
                                    <span class="fm-price">
                                        <fmt:formatNumber value="${p.sellPrice}" type="number" groupingUsed="true" maxFractionDigits="2"/>
                                    </span>
                                    <span class="fm-muted">₫</span>
                                </div>
                                <span class="small fm-muted">/ <c:out value="${p.unit}"/></span>
                            </div>
                        </div>

                        <div class="card-footer bg-transparent border-0 pt-0 pb-3">
                            <div class="d-flex flex-wrap gap-2 align-items-center">
                                <a class="btn btn-outline-primary btn-sm"
                                   href="${pageContext.request.contextPath}/product?id=${p.id}">
                                    <i class="bi bi-eye me-1"></i>Chi tiết
                                </a>

                                <form action="${pageContext.request.contextPath}/cart"
                                      method="post"
                                      class="d-flex flex-wrap align-items-center gap-2 ms-auto">
                                    <input type="hidden" name="action" value="add"/>
                                    <input type="hidden" name="productId" value="${p.id}"/>

                                    <div class="input-group input-group-sm" style="width: 140px;">
                                        <span class="input-group-text">SL</span>
                                        <input type="number" name="qty" value="1" min="1" class="form-control"/>
                                    </div>

                                    <button type="submit" class="btn btn-primary btn-sm">
                                        <i class="bi bi-cart-plus me-1"></i>Thêm
                                    </button>
                                </form>
                            </div>
                        </div>
                    </div>
                </div>
            </c:forEach>
        </div>
    </c:forEach>
</c:if>

<c:if test="${not groupMode}">
    <div class="row g-3">
        <c:forEach items="${products}" var="p">
            <div class="col-12 col-sm-6 col-lg-4">
                <div class="card fm-product-card h-100">

                    <div class="fm-product-media">
                        <c:choose>
                            <c:when test="${not empty p.imageUrl}">
                                <img src="${p.imageUrl}" alt="${p.name}"/>
                            </c:when>
                            <c:otherwise>
                                <div class="text-center">
                                    <i class="bi bi-image" style="font-size: 2rem;"></i>
                                    <div class="small">Chưa có ảnh</div>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>

                    <div class="card-body">
                        <div class="d-flex align-items-start justify-content-between gap-2">
                            <div>
                                <h3 class="h6 mb-1"><c:out value="${p.name}"/></h3>
                                <div class="small fm-muted">Đơn vị: <c:out value="${p.unit}"/></div>
                            </div>
                            <span class="badge rounded-pill fm-badge"><c:out value="${p.category}"/></span>
                        </div>

                        <div class="mt-3 d-flex align-items-baseline justify-content-between">
                            <div>
                                <span class="fm-price">
                                    <fmt:formatNumber value="${p.sellPrice}" type="number" groupingUsed="true" maxFractionDigits="2"/>
                                </span>
                                <span class="fm-muted">₫</span>
                            </div>
                            <span class="small fm-muted">/ <c:out value="${p.unit}"/></span>
                        </div>
                    </div>

                    <div class="card-footer bg-transparent border-0 pt-0 pb-3">
                        <div class="d-flex flex-wrap gap-2 align-items-center">
                            <a class="btn btn-outline-primary btn-sm"
                               href="${pageContext.request.contextPath}/product?id=${p.id}">
                                <i class="bi bi-eye me-1"></i>Chi tiết
                            </a>

                            <form action="${pageContext.request.contextPath}/cart"
                                  method="post"
                                  class="d-flex flex-wrap align-items-center gap-2 ms-auto">
                                <input type="hidden" name="action" value="add"/>
                                <input type="hidden" name="productId" value="${p.id}"/>

                                <div class="input-group input-group-sm" style="width: 140px;">
                                    <span class="input-group-text">SL</span>
                                    <input type="number" name="qty" value="1" min="1" class="form-control"/>
                                </div>

                                <button type="submit" class="btn btn-primary btn-sm">
                                    <i class="bi bi-cart-plus me-1"></i>Thêm
                                </button>
                            </form>
                        </div>
                    </div>
                </div>
            </div>
        </c:forEach>
    </div>

    <!-- pagination controls -->
    <c:if test="${totalPages gt 1}">
        <nav aria-label="Page navigation" class="mt-4">
            <ul class="pagination justify-content-center">
                <c:if test="${currentPage > 1}">
                    <li class="page-item">
                        <c:url var="prevUrl" value="/catalog">
                            <c:param name="q" value="${param.q}"/>
                            <c:param name="category" value="${param.category}"/>
                            <c:param name="sort" value="${param.sort}"/>
                            <c:param name="page" value="${currentPage - 1}"/>
                        </c:url>
                        <a class="page-link" href="${prevUrl}">Trước</a>
                    </li>
                </c:if>
                <c:forEach begin="1" end="${totalPages}" var="i">
                    <li class="page-item <c:if test='${i == currentPage}'>active</c:if>">
                        <c:url var="pageUrl" value="/catalog">
                            <c:param name="q" value="${param.q}"/>
                            <c:param name="category" value="${param.category}"/>
                            <c:param name="sort" value="${param.sort}"/>
                            <c:param name="page" value="${i}"/>
                        </c:url>
                        <a class="page-link" href="${pageUrl}">${i}</a>
                    </li>
                </c:forEach>
                <c:if test="${currentPage < totalPages}">
                    <li class="page-item">
                        <c:url var="nextUrl" value="/catalog">
                            <c:param name="q" value="${param.q}"/>
                            <c:param name="category" value="${param.category}"/>
                            <c:param name="sort" value="${param.sort}"/>
                            <c:param name="page" value="${currentPage + 1}"/>
                        </c:url>
                        <a class="page-link" href="${nextUrl}">Tiếp</a>
                    </li>
                </c:if>
            </ul>
        </nav>
    </c:if>
</c:if>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>