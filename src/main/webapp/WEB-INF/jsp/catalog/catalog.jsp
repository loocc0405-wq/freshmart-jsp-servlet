<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="pageTitle" value="Product Catalog | FreshMart"/>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"/>

<style>
    .fm-catalog-hero {
        min-height: 320px;
        position: relative;
        overflow: hidden;
        border-radius: var(--fm-radius-card);
        margin-bottom: var(--fm-sp-10);
        background-color: var(--fm-slate-900);
    }
    .fm-catalog-hero-img {
        position: absolute;
        inset: 0;
        width: 100%;
        height: 100%;
        object-fit: cover;
        opacity: 0.65;
        z-index: 1;
    }
    .fm-catalog-hero-content {
        position: relative;
        z-index: 2;
        padding: var(--fm-sp-12);
        color: white;
        height: 100%;
        display: flex;
        flex-direction: column;
        justify-content: center;
        background: linear-gradient(90deg, rgba(15, 23, 42, 0.95) 0%, rgba(15, 23, 42, 0.4) 100%);
    }
    .fm-filter-sidebar {
        position: sticky;
        top: 100px;
    }
    .fm-product-list-header {
        display: flex;
        align-items: center;
        justify-content: space-between;
        margin-bottom: var(--fm-sp-6);
        padding-bottom: var(--fm-sp-4);
        border-bottom: 1px solid var(--fm-slate-100);
    }
    .pagination-enterprise .page-link {
        border: 1px solid var(--fm-slate-200);
        color: var(--fm-text-secondary);
        padding: 0.625rem 1rem;
        transition: all 0.2s ease;
    }
    .pagination-enterprise .page-item.active .page-link {
        background-color: var(--fm-primary-600);
        border-color: var(--fm-primary-600);
        color: white;
    }
    .pagination-enterprise .page-link:hover:not(.active) {
        background-color: var(--fm-slate-50);
        border-color: var(--fm-slate-300);
    }
    
    /* Product Card Enhancements */
    .fm-product-card {
        transition: transform 0.3s cubic-bezier(0.34, 1.56, 0.64, 1), box-shadow 0.3s ease;
    }
    .fm-product-card:hover {
        transform: translateY(-8px);
        box-shadow: 0 12px 24px -8px rgba(0,0,0,0.15);
    }
    .fm-product-media img {
        transition: transform 0.5s ease;
    }
    .fm-product-card:hover .fm-product-media img {
        transform: scale(1.08);
    }
</style>

<div class="container-fluid px-4 py-4">
    <!-- Catalog Hero Section -->
    <div class="fm-catalog-hero shadow-sm">
        <img src="${pageContext.request.contextPath}/assets/images/heroes/catalog-hero-8k.png" 
             alt="FreshMart Catalog Hero" class="fm-catalog-hero-img">
        <div class="fm-catalog-hero-content">
            <span class="badge bg-primary text-white px-3 py-2 mb-3 rounded-pill fw-bold text-uppercase" style="font-size: 0.7rem; letter-spacing: 0.1em; width: fit-content;">
                Premium Selection
            </span>
            <h1 class="display-4 fw-extrabold mb-3 text-white text-shadow-sm" style="font-family: 'Outfit', sans-serif;">FreshMart Delivery</h1>
            <p class="lead opacity-90 text-white mb-0" style="max-width: 600px; font-weight: 500; font-size: 1.15rem; line-height: 1.6;">
                Discover our carefully curated catalog of fresh food, intelligently managed and delivered with the highest standards of freshness.
            </p>
        </div>
    </div>

    <div class="row g-5">
        <!-- Sidebar Filters -->
        <aside class="col-lg-3">
            <div class="fm-filter-sidebar">
                <div class="fm-surface p-4 mb-4 shadow-sm border-0">
                    <h5 class="fm-h3 mb-4 pb-3 border-bottom d-flex align-items-center gap-2">
                        <i class="bi bi-sliders2 text-primary"></i> Product Filters
                    </h5>
                    
                    <form method="get" action="${pageContext.request.contextPath}/catalog" id="filterForm" class="fm-form">
                        <div class="mb-4">
                            <label class="fm-label">Search</label>
                            <div class="position-relative">
                                <i class="bi bi-search position-absolute top-50 start-0 translate-middle-y ms-3 text-muted"></i>
                                <input type="text" name="q" class="fm-form-control ps-5" placeholder="Product name..." value="${param.q}">
                            </div>
                        </div>

                        <div class="mb-4">
                            <label class="fm-label">Category</label>
                            <select class="form-select fm-form-control" name="category">
                                <option value="">All Categories</option>
                                <c:forEach items="${categories}" var="c">
                                    <option value="${c}" ${param.category eq c ? 'selected' : ''}><c:out value="${c}"/></option>
                                </c:forEach>
                            </select>
                        </div>

                        <div class="mb-4">
                            <label class="fm-label">Status</label>
                            <select class="form-select fm-form-control" name="stockStatus">
                                <option value="all" ${empty param.stockStatus or param.stockStatus eq 'all' ? 'selected' : ''}>Show All</option>
                                <option value="inStock" ${param.stockStatus eq 'inStock' ? 'selected' : ''}>In Stock Only</option>
                            </select>
                        </div>

                        <div class="mb-4">
                            <label class="fm-label">Sorting</label>
                            <select class="form-select fm-form-control" name="sort">
                                <option value="">Default</option>
                                <option value="name_asc" ${param.sort eq 'name_asc' ? 'selected' : ''}>Name: A-Z</option>
                                <option value="price_asc" ${param.sort eq 'price_asc' ? 'selected' : ''}>Price: Low to High</option>
                                <option value="price_desc" ${param.sort eq 'price_desc' ? 'selected' : ''}>Price: High to Low</option>
                            </select>
                        </div>

                        <div class="d-grid gap-2 pt-2">
                            <button type="submit" class="fm-btn fm-btn-primary py-2 shadow-sm">Apply Filters</button>
                            <a href="${pageContext.request.contextPath}/catalog" class="btn btn-light border small text-muted text-center py-2 transition hvr-soft">Clear All</a>
                        </div>
                    </form>
                </div>

                <!-- Info Card -->
                <div class="fm-card bg-primary text-white border-0 p-4 shadow-sm overflow-hidden position-relative">
                    <div class="position-relative" style="z-index: 2;">
                        <span class="badge border border-white text-white mb-2 fw-bold" style="background: rgba(255,255,255,0.15)">PRO ACCOUNT</span>
                        <h6 class="fw-bold fs-5 mb-2">Enterprise Perks</h6>
                        <p class="small opacity-90 mb-4">Login to unlock corporate discounts and priority fulfillment.</p>
                        <a href="${pageContext.request.contextPath}/login" class="fm-btn btn-light text-primary fw-bold px-4 py-2 border-0 shadow-sm transition hvr-elevate">Login Now</a>
                    </div>
                    <i class="bi bi-lightning-charge-fill position-absolute bottom-0 end-0 m-n3 opacity-10" style="font-size: 10rem;"></i>
                </div>
            </div>
        </aside>

        <!-- Product Grid -->
        <main class="col-lg-9">
            <c:choose>
                <c:when test="${groupMode}">
                    <c:forEach items="${groupedProducts}" var="entry">
                        <div class="fm-product-list-header mt-2">
                            <h2 class="fm-h3 mb-0 d-flex align-items-center gap-3">
                                <span class="bg-primary-subtle text-primary rounded-pill px-1" style="height: 24px; min-width: 4px;"></span>
                                <c:out value="${entry.key}"/>
                            </h2>
                            <span class="badge bg-slate-100 text-slate-500 fw-bold border border-slate-200 px-3 py-2 rounded-pill shadow-sm" style="font-size: 0.75rem;">
                                ${fn:length(entry.value)} items
                            </span>
                        </div>
                        
                        <div class="row g-4 mb-10">
                            <c:forEach items="${entry.value}" var="p">
                                <div class="col-sm-6 col-md-6 col-xl-4">
                                    <c:set var="isAvail" value="${availableQtyMap[p.id] > 0}"/>
                                    <div class="fm-card fm-product-card h-100 d-flex flex-column shadow-sm border-0">
                                        <div class="fm-product-media border-0 bg-slate-50 position-relative overflow-hidden" style="aspect-ratio: 1/1;">
                                            <c:choose>
                                                <c:when test="${not empty p.imageUrl}">
                                                    <img src="${p.imageUrl}" alt="${p.name}" loading="lazy" class="w-100 h-100 object-fit-cover transition">
                                                </c:when>
                                                <c:otherwise>
                                                    <div class="w-100 h-100 d-flex align-items-center justify-content-center text-slate-300">
                                                        <i class="bi bi-image-fill fs-1"></i>
                                                    </div>
                                                </c:otherwise>
                                            </c:choose>
                                            <div class="position-absolute top-0 end-0 p-3 opacity-0 transition product-action-btn" style="z-index: 5;">
                                                <button class="btn btn-white btn-sm rounded-circle shadow-sm border-0 hvr-pulse" title="Add to Wishlist"><i class="bi bi-heart text-danger"></i></button>
                                            </div>
                                            <c:if test="${!isAvail}">
                                                <div class="position-absolute inset-0 d-flex align-items-center justify-content-center bg-white bg-opacity-75" style="z-index: 4;">
                                                    <span class="badge bg-dark px-3 py-2 rounded shadow-sm fw-bold">Out of Stock</span>
                                                </div>
                                            </c:if>
                                        </div>
                                        <div class="p-4 flex-grow-1 d-flex flex-column">
                                            <div class="d-flex align-items-center justify-content-between mb-2">
                                                <span class="badge bg-primary-subtle text-primary border-0 rounded-pill px-3 py-1 fw-bold" style="font-size: 0.65rem; text-transform: uppercase;">
                                                    <c:out value="${p.category}"/>
                                                </span>
                                                <c:if test="${isAvail}">
                                                    <span class="small text-success fw-bold d-flex align-items-center gap-1" style="font-size: 0.75rem;">
                                                        <i class="bi bi-check-circle-fill"></i> In Stock
                                                    </span>
                                                </c:if>
                                            </div>
                                            <h3 class="fm-h3 mb-1 fs-6 lh-base text-slate-900" style="min-height: 2.8em; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;">
                                                <c:out value="${p.name}"/>
                                            </h3>
                                            <div class="small text-slate-400 mb-4 d-flex align-items-center gap-2">
                                                <i class="bi bi-tag small"></i> Unit: <c:out value="${p.unit}"/>
                                            </div>
                                            
                                            <div class="mt-auto d-flex align-items-end justify-content-between pt-3 border-top">
                                                <div>
                                                    <div class="fm-caption fw-bold text-slate-400 mb-1" style="font-size: 0.6rem;">UNIT PRICE</div>
                                                    <div class="fm-h3 text-primary mb-0 d-flex align-items-baseline gap-1" style="font-family: 'Outfit', sans-serif;">
                                                        <span class="fs-4 fw-extrabold"><fmt:formatNumber value="${p.sellPrice}" type="number" groupingUsed="true" maxFractionDigits="2"/></span>
                                                        <span class="small fw-bold opacity-75">₫</span>
                                                    </div>
                                                </div>
                                                <div>
                                                    <a href="${pageContext.request.contextPath}/product?id=${p.id}" class="fm-btn btn-light border-0 text-slate-500 rounded-circle d-flex align-items-center justify-content-center hvr-boost" style="width: 44px; height: 44px;" title="View Detail">
                                                        <i class="bi bi-arrow-up-right"></i>
                                                    </a>
                                                </div>
                                            </div>
                                        </div>
                                        <div class="px-4 pb-4">
                                            <c:if test="${isAvail}">
                                                <form action="${pageContext.request.contextPath}/cart" method="post">
                                                    <input type="hidden" name="csrf_token" value="${sessionScope.CSRF_TOKEN}">
                                                    <input type="hidden" name="action" value="add">
                                                    <input type="hidden" name="productId" value="${p.id}">
                                                    <input type="hidden" name="qty" value="1">
                                                    <button type="submit" class="fm-btn fm-btn-primary w-100 py-2 shadow-sm border-0 transition hvr-elevate">
                                                        <i class="bi bi-cart-plus-fill me-2"></i> Add to Cart
                                                    </button>
                                                </form>
                                            </c:if>
                                            <c:if test="${!isAvail}">
                                                <button class="fm-btn btn-slate-100 text-slate-400 w-100 py-2 border-0 cursor-not-allowed" disabled>
                                                    Unavailable
                                                </button>
                                            </c:if>
                                        </div>
                                    </div>
                                </div>
                            </c:forEach>
                        </div>
                    </c:forEach>
                </c:when>
                
                <c:when test="${not empty products}">
                    <div class="fm-product-list-header">
                        <h2 class="fm-h3 mb-0">All Products</h2>
                        <span class="badge bg-slate-100 text-slate-500 fw-bold border border-slate-200 px-3 py-2 rounded-pill shadow-sm" style="font-size: 0.75rem;">
                            Page ${currentPage} / ${totalPages}
                        </span>
                    </div>
                    <div class="row g-4 mb-5">
                        <c:forEach items="${products}" var="p">
                            <div class="col-sm-6 col-md-6 col-xl-4">
                                <c:set var="isAvail" value="${availableQtyMap[p.id] > 0}"/>
                                <div class="fm-card fm-product-card h-100 d-flex flex-column shadow-sm border-0">
                                    <div class="fm-product-media border-0 bg-slate-50 position-relative overflow-hidden" style="aspect-ratio: 1/1;">
                                        <c:choose>
                                            <c:when test="${not empty p.imageUrl}">
                                                <img src="${p.imageUrl}" alt="${p.name}" loading="lazy" class="w-100 h-100 object-fit-cover transition">
                                            </c:when>
                                            <c:otherwise>
                                                <div class="w-100 h-100 d-flex align-items-center justify-content-center text-slate-300">
                                                    <i class="bi bi-image-fill fs-1"></i>
                                                </div>
                                            </c:otherwise>
                                        </c:choose>
                                        <c:if test="${!isAvail}">
                                            <div class="position-absolute inset-0 d-flex align-items-center justify-content-center bg-white bg-opacity-75" style="z-index: 4;">
                                                <span class="badge bg-dark px-3 py-2 rounded shadow-sm fw-bold">Out of Stock</span>
                                            </div>
                                        </c:if>
                                    </div>
                                    <div class="p-4 flex-grow-1 d-flex flex-column">
                                        <div class="d-flex align-items-center justify-content-between mb-2">
                                            <span class="badge bg-primary-subtle text-primary border-0 rounded-pill px-3 py-1 fw-bold" style="font-size: 0.65rem; text-transform: uppercase;">
                                                <c:out value="${p.category}"/>
                                            </span>
                                            <c:if test="${isAvail}">
                                                <span class="small text-success fw-bold d-flex align-items-center gap-1" style="font-size: 0.75rem;">
                                                    <i class="bi bi-check-circle-fill"></i> In Stock
                                                </span>
                                            </c:if>
                                        </div>
                                        <h3 class="fm-h3 mb-1 fs-6 lh-base text-slate-900" style="min-height: 2.8em; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;">
                                            <c:out value="${p.name}"/>
                                        </h3>
                                        <div class="small text-slate-400 mb-4 d-flex align-items-center gap-2">
                                            <i class="bi bi-tag small"></i> Unit: <c:out value="${p.unit}"/>
                                        </div>
                                        
                                        <div class="mt-auto d-flex align-items-end justify-content-between pt-3 border-top">
                                            <div>
                                                <div class="fm-caption fw-bold text-slate-400 mb-1" style="font-size: 0.6rem;">UNIT PRICE</div>
                                                <div class="fm-h3 text-primary mb-0 d-flex align-items-baseline gap-1" style="font-family: 'Outfit', sans-serif;">
                                                    <span class="fs-4 fw-extrabold"><fmt:formatNumber value="${p.sellPrice}" type="number" groupingUsed="true" maxFractionDigits="2"/></span>
                                                    <span class="small fw-bold opacity-75">₫</span>
                                                </div>
                                            </div>
                                            <div>
                                                <a href="${pageContext.request.contextPath}/product?id=${p.id}" class="fm-btn btn-light border-0 text-slate-500 rounded-circle d-flex align-items-center justify-content-center hvr-boost" style="width: 44px; height: 44px;" title="View Detail">
                                                    <i class="bi bi-arrow-up-right"></i>
                                                </a>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="px-4 pb-4">
                                        <c:if test="${isAvail}">
                                            <form action="${pageContext.request.contextPath}/cart" method="post">
                                                <input type="hidden" name="csrf_token" value="${sessionScope.CSRF_TOKEN}">
                                                <input type="hidden" name="action" value="add">
                                                <input type="hidden" name="productId" value="${p.id}">
                                                <input type="hidden" name="qty" value="1">
                                                <button type="submit" class="fm-btn fm-btn-primary w-100 py-2 shadow-sm border-0 transition hvr-elevate">
                                                    <i class="bi bi-cart-plus-fill me-2"></i> Add to Cart
                                                </button>
                                            </form>
                                        </c:if>
                                        <c:if test="${!isAvail}">
                                            <button class="fm-btn btn-slate-100 text-slate-400 w-100 py-2 border-0 cursor-not-allowed" disabled>
                                                Unavailable
                                            </button>
                                        </c:if>
                                    </div>
                                </div>
                            </div>
                        </c:forEach>
                    </div>
                </c:when>

                <c:otherwise>
                    <div class="fm-surface p-12 text-center shadow-sm border-0">
                        <div class="mb-4">
                            <div class="bg-slate-100 rounded-circle d-inline-flex p-4 text-slate-300">
                                <i class="bi bi-search fs-1"></i>
                            </div>
                        </div>
                        <h4 class="fw-extrabold text-slate-900 mb-2">No results found</h4>
                        <p class="text-slate-500 mb-6">Try adjusting your filters or search terms.</p>
                        <a href="${pageContext.request.contextPath}/catalog" class="fm-btn fm-btn-primary px-8 py-3 shadow-lg border-0 transition hvr-boost">Reset Catalog</a>
                    </div>
                </c:otherwise>
            </c:choose>

            <!-- Pagination Area -->
            <c:if test="${totalPages gt 1}">
                <nav class="mt-12 py-8 border-top">
                    <ul class="pagination pagination-enterprise justify-content-center gap-2">
                        <c:if test="${currentPage > 1}">
                            <li class="page-item">
                                <c:url var="prevUrl" value="/catalog">
                                    <c:param name="q" value="${param.q}"/><c:param name="category" value="${param.category}"/><c:param name="stockStatus" value="${param.stockStatus}"/><c:param name="sort" value="${param.sort}"/><c:param name="page" value="${currentPage - 1}"/>
                                </c:url>
                                <a class="page-link shadow-none rounded-pill px-4" href="${prevUrl}"><i class="bi bi-arrow-left me-2"></i> Previous</a>
                            </li>
                        </c:if>
                        
                        <c:forEach begin="1" end="${totalPages}" var="i">
                            <c:if test="${i == 1 || i == totalPages || (i >= currentPage - 2 && i <= currentPage + 2)}">
                                <li class="page-item ${i == currentPage ? 'active' : ''}">
                                    <c:url var="pageUrl" value="/catalog">
                                        <c:param name="q" value="${param.q}"/><c:param name="category" value="${param.category}"/><c:param name="stockStatus" value="${param.stockStatus}"/><c:param name="sort" value="${param.sort}"/><c:param name="page" value="${i}"/>
                                    </c:url>
                                    <a class="page-link shadow-none rounded-circle d-flex align-items-center justify-content-center fw-bold" style="width: 44px; height: 44px;" href="${pageUrl}">${i}</a>
                                </li>
                            </c:if>
                            <c:if test="${(i == 2 && currentPage > 4) || (i == totalPages - 1 && currentPage < totalPages - 3)}">
                                <li class="page-item disabled"><span class="page-link border-0 text-muted opacity-50">...</span></li>
                            </c:if>
                        </c:forEach>

                        <c:if test="${currentPage < totalPages}">
                            <li class="page-item">
                                <c:url var="nextUrl" value="/catalog">
                                    <c:param name="q" value="${param.q}"/><c:param name="category" value="${param.category}"/><c:param name="stockStatus" value="${param.stockStatus}"/><c:param name="sort" value="${param.sort}"/><c:param name="page" value="${currentPage + 1}"/>
                                </c:url>
                                <a class="page-link shadow-none rounded-pill px-4" href="${nextUrl}">Next <i class="bi bi-arrow-right ms-2"></i></a>
                            </li>
                        </c:if>
                    </ul>
                </nav>
            </c:if>
        </main>
    </div>
</div>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>