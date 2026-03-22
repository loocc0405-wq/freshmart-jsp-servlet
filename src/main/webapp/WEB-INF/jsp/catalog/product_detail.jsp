<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
            <%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

                <c:set var="pageTitle" value="Product Detail | FreshMart Premium" />
                <jsp:include page="/WEB-INF/jsp/common/header.jsp" />

                <c:if test="${product == null}">
                    <div class="container py-5 text-center">
                        <div class="fm-surface p-5">
                            <i class="bi bi-search fs-1 text-muted opacity-25 mb-3"></i>
                            <h2 class="fw-bold">Product Not Found</h2>
                            <p class="text-muted">The product you are looking for does not exist or has been removed.
                            </p>
                            <a href="${pageContext.request.contextPath}/catalog"
                                class="fm-btn fm-btn-primary px-4 mt-3">Back to Catalog</a>
                        </div>
                    </div>
                </c:if>

                <c:if test="${product != null}">
                    <div class="container py-4">
                        <nav aria-label="breadcrumb" class="mb-4">
                            <ol class="breadcrumb fm-caption fw-bold mb-0">
                                <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/catalog"
                                        class="text-decoration-none opacity-50">Catalog</a></li>
                                <li class="breadcrumb-item"><a
                                        href="${pageContext.request.contextPath}/catalog?category=${product.category}"
                                        class="text-decoration-none opacity-50">
                                        <c:out value="${product.category}" />
                                    </a></li>
                                <li class="breadcrumb-item active text-primary" aria-current="page">
                                    <c:out value="${product.name}" />
                                </li>
                            </ol>
                        </nav>

                        <div class="row g-5">
                            <div class="col-lg-6">
                                <div class="fm-surface p-0 overflow-hidden shadow-sm"
                                    style="position: sticky; top: 100px;">
                                    <div class="fm-product-media" style="aspect-ratio: 1/1;">
                                        <c:choose>
                                            <c:when test="${not empty product.imageUrl}">
                                                <img src="${product.imageUrl}" alt="${product.name}"
                                                    class="w-100 h-100 object-fit-cover">
                                            </c:when>
                                            <c:otherwise>
                                                <div
                                                    class="w-100 h-100 bg-light d-flex flex-column align-items-center justify-content-center text-muted">
                                                    <i class="bi bi-image fs-1 opacity-25 mb-2"></i>
                                                    <span class="small">No Media Available</span>
                                                </div>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                </div>
                            </div>

                            <div class="col-lg-6">
                                <div class="ps-lg-4">
                                    <div class="mb-4 pb-4 border-bottom">
                                        <div class="d-flex align-items-center gap-2 mb-3 flex-wrap">
                                            <span class="fm-status-badge available px-3"><i
                                                    class="bi bi-tag-fill me-1"></i>
                                                <c:out value="${product.category}" />
                                            </span>
                                            <c:choose>
                                                <c:when test="${availableQty > 0}">
                                                    <span class="small fw-bold text-success"><i
                                                            class="bi bi-check2-circle"></i> ATP: ${availableQty}
                                                        ${product.unit}</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="small fw-bold text-danger"><i
                                                            class="bi bi-x-circle"></i> Out of ATP stock</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </div>

                                        <h1 class="display-5 fw-bold mb-3" style="font-family: 'Outfit', sans-serif;">
                                            <c:out value="${product.name}" />
                                        </h1>

                                        <div class="d-flex align-items-baseline gap-2 mb-0">
                                            <div class="fm-h1 text-primary mb-0" style="font-size: 2.5rem;">
                                                <fmt:formatNumber value="${product.sellPrice}" type="number"
                                                    groupingUsed="true" maxFractionDigits="2" />
                                                <span class="fs-4 opacity-50">?</span>
                                            </div>
                                            <div class="fs-5 text-muted">/
                                                <c:out value="${product.unit}" />
                                            </div>
                                        </div>
                                    </div>

                                    <div class="alert alert-light border mb-4">
                                        <div class="fw-bold mb-2">Availability snapshot</div>
                                        <div class="small text-muted">Physical non-expired stock:
                                            <strong>${physicalQty}</strong> ${product.unit}</div>
                                        <div class="small text-muted">Reserved by pending orders: <strong
                                                class="text-warning">${reservedQty}</strong> ${product.unit}</div>
                                        <div class="small text-muted">Available to promise (ATP): <strong
                                                class="text-primary">${availableQty}</strong> ${product.unit}</div>
                                    </div>

                                    <div class="row g-4 mb-5">
                                        <div class="col-6">
                                            <div class="fm-card py-3 text-center border-0 bg-light-subtle">
                                                <i class="bi bi-shield-check text-primary fs-4 mb-2"></i>
                                                <div class="fm-caption fw-bold opacity-75">Traceable</div>
                                                <div class="small fw-semibold">FEFO Managed</div>
                                            </div>
                                        </div>
                                        <div class="col-6">
                                            <div class="fm-card py-3 text-center border-0 bg-light-subtle">
                                                <i class="bi bi-truck text-primary fs-4 mb-2"></i>
                                                <div class="fm-caption fw-bold opacity-75">Hub Delivery</div>
                                                <div class="small fw-semibold">Same-Day Ready</div>
                                            </div>
                                        </div>
                                    </div>

                                    <div class="fm-surface p-4 border-0 shadow-none bg-slate-50 mb-5">
                                        <c:choose>
                                            <c:when test="${availableQty > 0}">
                                                <form action="${pageContext.request.contextPath}/cart" method="post"
                                                    id="addToCartForm">
                                                    <input type="hidden" name="csrf_token"
                                                        value="${sessionScope.CSRF_TOKEN}">
                                                    <input type="hidden" name="action" value="add">
                                                    <input type="hidden" name="productId" value="${product.id}">

                                                    <div class="mb-4">
                                                        <label class="fm-caption fw-bold d-block mb-2">Select
                                                            Quantity</label>
                                                        <div class="input-group" style="max-width: 180px;">
                                                            <button class="btn btn-outline-secondary border-end-0 px-3"
                                                                type="button"
                                                                onclick="this.nextElementSibling.stepDown()"><i
                                                                    class="bi bi-dash"></i></button>
                                                            <input type="number" name="qty"
                                                                class="fm-form-control text-center py-2" value="1"
                                                                min="1" max="${availableQty}">
                                                            <button
                                                                class="btn btn-outline-secondary border-start-0 px-3"
                                                                type="button"
                                                                onclick="this.previousElementSibling.stepUp()"><i
                                                                    class="bi bi-plus"></i></button>
                                                        </div>
                                                        <div class="form-text">Max quantity follows ATP to avoid
                                                            oversell.</div>
                                                    </div>

                                                    <div class="d-grid gap-3">
                                                        <button type="submit" class="fm-btn fm-btn-primary py-3 fs-5">
                                                            <i class="bi bi-cart-plus-fill me-2"></i> Add to Logistics
                                                            Cart
                                                        </button>
                                                    </div>
                                                </form>
                                            </c:when>
                                            <c:otherwise>
                                                <div class="text-center py-4">
                                                    <h5 class="fw-bold opacity-50 mb-3">T?m h?t hàng kh? d?ng</h5>
                                                    <button class="fm-btn btn-light border w-100 py-3" disabled>Notify
                                                        me when back in stock</button>
                                                </div>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>

                                    <div class="mb-0">
                                        <h5 class="fm-h3 border-bottom pb-3 mb-4">Specifications & Details</h5>
                                        <div class="fm-text-secondary lh-lg mb-4">
                                            <c:out value="${product.description}"
                                                default="Premium quality fresh produce handled within a temperature-controlled environment under strict FEFO protocols." />
                                        </div>

                                        <div class="row g-3">
                                            <div class="col-md-6">
                                                <div class="d-flex justify-content-between py-2 border-bottom">
                                                    <span class="text-muted">Category</span>
                                                    <strong>
                                                        <c:out value="${product.category}" />
                                                    </strong>
                                                </div>
                                            </div>
                                            <div class="col-md-6">
                                                <div class="d-flex justify-content-between py-2 border-bottom">
                                                    <span class="text-muted">Unit</span>
                                                    <strong>
                                                        <c:out value="${product.unit}" />
                                                    </strong>
                                                </div>
                                            </div>
                                            <div class="col-md-6">
                                                <div class="d-flex justify-content-between py-2 border-bottom">
                                                    <span class="text-muted">ATP</span>
                                                    <strong>${availableQty}</strong>
                                                </div>
                                            </div>
                                            <div class="col-md-6">
                                                <div class="d-flex justify-content-between py-2 border-bottom">
                                                    <span class="text-muted">Reserved</span>
                                                    <strong>${reservedQty}</strong>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </c:if>

                <jsp:include page="/WEB-INF/jsp/common/footer.jsp" />