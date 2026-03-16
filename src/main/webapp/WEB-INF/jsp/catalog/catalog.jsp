<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="pageTitle" value="Fresh Catalog"/>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"/>

<div class="container-fluid px-4 py-4">
    <!-- Catalog Header & Banner -->
    <div class="fm-surface p-0 overflow-hidden mb-5 border-0 shadow-sm" style="min-height: 280px; position: relative;">
        <!-- FM_IMAGE_PROMPT: 
        Ultra-realistic 8K premium grocery catalog banner, wide angle artistic shot of high-end fresh produce, vibrant vegetables and fruits arranged elegantly on a dark stone surface, soft cinematic morning light, luxury organic market atmosphere, no text, no watermark
        -->
        <div class="position-absolute top-0 start-0 w-100 h-100" style="background: linear-gradient(90deg, rgba(15,23,42,0.9) 0%, rgba(15,23,42,0.4) 100%), url('https://images.unsplash.com/photo-1542838132-92c53300491e?auto=format&fit=crop&q=80&w=2000') center/cover; z-index: 1;"></div>
        
        <div class="position-relative h-100 d-flex flex-column justify-content-center p-5 text-white" style="z-index: 2; max-width: 700px;">
            <div class="fm-caption text-uppercase fw-bold text-primary mb-2 ls-wide">Direct from Source</div>
            <h1 class="display-4 fw-bold mb-3" style="font-family: 'Outfit', sans-serif;">Premium Fresh Selection</h1>
            <p class="opacity-75 fs-5">Traceable, high-quality groceries managed with real-time FEFO precision. Freshness guaranteed from our hub to your door.</p>
        </div>
    </div>

    <div class="row g-4">
        <!-- Sidebar Filters -->
        <div class="col-lg-3">
            <div class="sticky-top" style="top: 100px;">
                <div class="fm-surface p-4 mb-4">
                    <h5 class="fm-h3 border-bottom pb-3 mb-4">Refine Selection</h5>
                    
                    <form method="get" action="${pageContext.request.contextPath}/catalog" id="filterForm">
                        <div class="mb-4">
                            <label class="fm-caption fw-bold d-block mb-2">Search Catalog</label>
                            <div class="position-relative">
                                <i class="bi bi-search position-absolute top-50 start-0 translate-middle-y ms-3 opacity-50"></i>
                                <input type="text" name="q" class="fm-form-control ps-5" placeholder="Search products..." value="${param.q}">
                            </div>
                        </div>

                        <div class="mb-4">
                            <label class="fm-caption fw-bold d-block mb-2">Department</label>
                            <select class="fm-form-control" name="category">
                                <option value="">All Departments</option>
                                <c:forEach items="${categories}" var="c">
                                    <option value="${c}" ${param.category eq c ? 'selected' : ''}><c:out value="${c}"/></option>
                                </c:forEach>
                            </select>
                        </div>

                        <div class="mb-4">
                            <label class="fm-caption fw-bold d-block mb-2">Availability</label>
                            <select class="fm-form-control" name="stockStatus">
                                <option value="all" ${empty param.stockStatus or param.stockStatus eq 'all' ? 'selected' : ''}>Show All</option>
                                <option value="inStock" ${param.stockStatus eq 'inStock' ? 'selected' : ''}>In Stock Only</option>
                            </select>
                        </div>

                        <div class="mb-4">
                            <label class="fm-caption fw-bold d-block mb-2">Sorting</label>
                            <select class="fm-form-control" name="sort">
                                <option value="">Featured</option>
                                <option value="name_asc" ${param.sort eq 'name_asc' ? 'selected' : ''}>Name: A-Z</option>
                                <option value="price_asc" ${param.sort eq 'price_asc' ? 'selected' : ''}>Price: Low to High</option>
                                <option value="price_desc" ${param.sort eq 'price_desc' ? 'selected' : ''}>Price: High to Low</option>
                            </select>
                        </div>

                        <div class="d-grid gap-2">
                            <button type="submit" class="fm-btn fm-btn-primary">Apply Filters</button>
                            <a href="${pageContext.request.contextPath}/catalog" class="fm-btn btn-light border small text-muted text-center">Clear All</a>
                        </div>
                    </form>
                </div>

                <!-- Promo Card -->
                <div class="fm-card bg-primary text-white border-0 p-4 overflow-hidden position-relative">
                    <div class="position-relative" style="z-index: 2;">
                        <h6 class="fw-bold mb-2">Enterprise Perks</h6>
                        <p class="small opacity-75 mb-3">Login to unlock corporate discounts and priority fulfillment.</p>
                        <a href="${pageContext.request.contextPath}/login" class="btn btn-sm btn-light fw-bold px-3">Login Now</a>
                    </div>
                    <i class="bi bi-shield-lock-fill position-absolute bottom-0 end-0 m-n3 opacity-10" style="font-size: 8rem;"></i>
                </div>
            </div>
        </div>

        <!-- Product Grid -->
        <div class="col-lg-9">
            <c:if test="${groupMode}">
                <c:forEach items="${groupedProducts}" var="entry">
                    <div class="d-flex align-items-center justify-content-between mb-4 mt-2">
                        <h2 class="fm-h3 mb-0"><c:out value="${entry.key}"/></h2>
                        <span class="fm-caption fw-bold opacity-50">${fn:length(entry.value)} Items found</span>
                    </div>
                    
                    <div class="row g-4 mb-5">
                        <c:forEach items="${entry.value}" var="p">
                            <div class="col-sm-6 col-xl-4">
                                <c:set var="isAvailable" value="${availableQtyMap[p.id] > 0}"/>
                                <div class="fm-card fm-product-card h-100 d-flex flex-column">
                                    <div class="fm-product-media border-bottom">
                                        <c:choose>
                                            <c:when test="${not empty p.imageUrl}">
                                                <img src="${p.imageUrl}" alt="${p.name}" loading="lazy">
                                            </c:when>
                                            <c:otherwise>
                                                <div class="w-100 h-100 bg-light d-flex align-items-center justify-content-center text-muted">
                                                    <i class="bi bi-image fs-1 opacity-25"></i>
                                                </div>
                                            </c:otherwise>
                                        </c:choose>
                                        <div class="position-absolute top-0 end-0 p-3">
                                            <button class="btn btn-white btn-sm rounded-circle shadow-sm" title="Add to Wishlist"><i class="bi bi-heart"></i></button>
                                        </div>
                                        <c:if test="${!isAvailable}">
                                            <div class="position-absolute top-50 start-50 translate-middle w-100 text-center">
                                                <span class="badge bg-dark px-3 py-2 opacity-75">Currently Unavailable</span>
                                            </div>
                                        </c:if>
                                    </div>
                                    <div class="p-3 flex-grow-1">
                                        <div class="fm-caption fw-bold text-primary mb-1"><c:out value="${p.category}"/></div>
                                        <h3 class="fm-h3 mb-1 fs-6 lh-base" style="min-height: 2.8em; overflow: hidden;"><c:out value="${p.name}"/></h3>
                                        <div class="small fm-text-secondary mb-3">Unit: <c:out value="${p.unit}"/></div>
                                        
                                        <div class="d-flex align-items-end justify-content-between">
                                            <div>
                                                <div class="fm-caption fw-bold opacity-50">UNIT PRICE</div>
                                                <div class="fm-h3 text-primary mb-0">
                                                    <fmt:formatNumber value="${p.sellPrice}" type="number" groupingUsed="true" maxFractionDigits="2"/>
                                                    <span class="fs-6 opacity-50">₫</span>
                                                </div>
                                            </div>
                                            <div class="text-end">
                                                <c:if test="${isAvailable}">
                                                    <span class="badge bg-success-subtle text-success small border-0"><i class="bi bi-box-seam me-1"></i> Stock: ${availableQtyMap[p.id]}</span>
                                                </c:if>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="p-3 pt-0 mt-auto">
                                        <div class="d-grid gap-2 d-xl-flex">
                                            <a href="${pageContext.request.contextPath}/product?id=${p.id}" class="fm-btn btn-light border py-2 flex-grow-1 text-center small fw-bold">View Detail</a>
                                            <c:if test="${isAvailable}">
                                                <form action="${pageContext.request.contextPath}/cart" method="post" class="flex-grow-1">
                                                    <input type="hidden" name="csrf_token" value="${sessionScope.CSRF_TOKEN}">
                                                    <input type="hidden" name="action" value="add">
                                                    <input type="hidden" name="productId" value="${p.id}">
                                                    <input type="hidden" name="qty" value="1">
                                                    <button type="submit" class="fm-btn fm-btn-primary w-100 py-2 small"><i class="bi bi-cart-plus-fill me-1"></i> Add</button>
                                                </form>
                                            </c:if>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </c:forEach>
                    </div>
                </c:forEach>
            </c:if>

            <c:if test="${not groupMode}">
                <div class="row g-4 mb-5">
                    <c:forEach items="${products}" var="p">
                        <div class="col-sm-6 col-xl-4">
                            <!-- Same product card as above -->
                            <c:set var="isAvailable" value="${availableQtyMap[p.id] > 0}"/>
                            <div class="fm-card fm-product-card h-100 d-flex flex-column">
                                <div class="fm-product-media border-bottom">
                                    <c:choose>
                                        <c:when test="${not empty p.imageUrl}">
                                            <img src="${p.imageUrl}" alt="${p.name}" loading="lazy">
                                        </c:when>
                                        <c:otherwise>
                                            <div class="w-100 h-100 bg-light d-flex align-items-center justify-content-center text-muted">
                                                <i class="bi bi-image fs-1 opacity-25"></i>
                                            </div>
                                        </c:otherwise>
                                    </c:choose>
                                    <c:if test="${!isAvailable}">
                                        <div class="position-absolute top-50 start-50 translate-middle w-100 text-center">
                                            <span class="badge bg-dark px-3 py-2 opacity-75">Currently Unavailable</span>
                                        </div>
                                    </c:if>
                                </div>
                                <div class="p-3 flex-grow-1 text-decoration-none text-dark" style="cursor: default;">
                                    <div class="fm-caption fw-bold text-primary mb-1"><c:out value="${p.category}"/></div>
                                    <h3 class="fm-h3 mb-1 fs-6 lh-base" style="min-height: 2.8em; overflow: hidden;"><c:out value="${p.name}"/></h3>
                                    <div class="small fm-text-secondary mb-3">Unit: <c:out value="${p.unit}"/></div>
                                    
                                    <div class="d-flex align-items-end justify-content-between">
                                        <div>
                                            <div class="fm-caption fw-bold opacity-50">UNIT PRICE</div>
                                            <div class="fm-h3 text-primary mb-0">
                                                <fmt:formatNumber value="${p.sellPrice}" type="number" groupingUsed="true" maxFractionDigits="2"/>
                                                <span class="fs-6 opacity-50">₫</span>
                                            </div>
                                        </div>
                                        <div class="text-end">
                                            <c:if test="${isAvailable}">
                                                <span class="badge bg-success-subtle text-success small border-0"><i class="bi bi-box-seam me-1"></i> Stock: ${availableQtyMap[p.id]}</span>
                                            </c:if>
                                        </div>
                                    </div>
                                </div>
                                <div class="p-3 pt-0 mt-auto">
                                    <div class="d-grid gap-2 d-xl-flex">
                                        <a href="${pageContext.request.contextPath}/product?id=${p.id}" class="fm-btn btn-light border py-2 flex-grow-1 text-center small fw-bold">View Detail</a>
                                        <c:if test="${isAvailable}">
                                            <form action="${pageContext.request.contextPath}/cart" method="post" class="flex-grow-1">
                                                <input type="hidden" name="csrf_token" value="${sessionScope.CSRF_TOKEN}">
                                                <input type="hidden" name="action" value="add">
                                                <input type="hidden" name="productId" value="${p.id}">
                                                <input type="hidden" name="qty" value="1">
                                                <button type="submit" class="fm-btn fm-btn-primary w-100 py-2 small"><i class="bi bi-cart-plus-fill me-1"></i> Add</button>
                                            </form>
                                        </c:if>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </c:forEach>
                </div>
            </c:if>

            <c:if test="${empty products && empty groupedProducts}">
                <div class="fm-surface p-5 text-center">
                    <i class="bi bi-search fs-1 text-muted opacity-25 mb-3"></i>
                    <h4 class="fw-bold">No results found</h4>
                    <p class="text-muted">Try adjusting your filters or search terms.</p>
                    <a href="${pageContext.request.contextPath}/catalog" class="fm-btn fm-btn-primary px-4 mt-3">Reset Catalog</a>
                </div>
            </c:if>

            <!-- Pagination -->
            <c:if test="${totalPages gt 1}">
                <nav class="mt-5">
                    <ul class="pagination pagination-enterprise justify-content-center gap-2">
                        <c:if test="${currentPage > 1}">
                            <li class="page-item">
                                <c:url var="prevUrl" value="/catalog">
                                    <c:param name="q" value="${param.q}"/><c:param name="category" value="${param.category}"/><c:param name="stockStatus" value="${param.stockStatus}"/><c:param name="sort" value="${param.sort}"/><c:param name="page" value="${currentPage - 1}"/>
                                </c:url>
                                <a class="page-link rounded" href="${prevUrl}"><i class="bi bi-chevron-left"></i> Previous</a>
                            </li>
                        </c:if>
                        <c:forEach begin="1" end="${totalPages}" var="i">
                            <li class="page-item ${i == currentPage ? 'active' : ''}">
                                <c:url var="pageUrl" value="/catalog">
                                    <c:param name="q" value="${param.q}"/><c:param name="category" value="${param.category}"/><c:param name="stockStatus" value="${param.stockStatus}"/><c:param name="sort" value="${param.sort}"/><c:param name="page" value="${i}"/>
                                </c:url>
                                <a class="page-link rounded fw-bold" href="${pageUrl}">${i}</a>
                            </li>
                        </c:forEach>
                        <c:if test="${currentPage < totalPages}">
                            <li class="page-item">
                                <c:url var="nextUrl" value="/catalog">
                                    <c:param name="q" value="${param.q}"/><c:param name="category" value="${param.category}"/><c:param name="stockStatus" value="${param.stockStatus}"/><c:param name="sort" value="${param.sort}"/><c:param name="page" value="${currentPage + 1}"/>
                                </c:url>
                                <a class="page-link rounded" href="${nextUrl}">Next <i class="bi bi-chevron-right"></i></a>
                            </li>
                        </c:if>
                    </ul>
                </nav>
            </c:if>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>