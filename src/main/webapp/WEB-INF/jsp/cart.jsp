<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<c:set var="pageTitle" value="Logistics Cart | FreshMart Enterprise"/>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"/>

<div class="container py-5">
    <div class="row g-5">
        <!-- Main Cart Segment -->
        <div class="col-xl-8">
            <div class="d-flex align-items-center justify-content-between mb-4">
                <div>
                    <h1 class="fm-h1 mb-1">Your Fulfillment Cart</h1>
                    <p class="fm-text-secondary m-0">Review items before initializing procurement.</p>
                </div>
                <div class="text-end">
                    <span class="fm-caption fw-bold opacity-50">INTERNAL AUDIT</span>
                    <div class="badge bg-primary-subtle text-primary border-0 rounded-pill px-3 py-2 mt-1">
                        <i class="bi bi-shield-lock me-1"></i> Secure Checkout
                    </div>
                </div>
            </div>

            <c:if test="${empty sessionScope.authUser}">
                <div class="alert alert-warning border-0 bg-warning-subtle d-flex align-items-center gap-3 mb-4 p-4 shadow-none">
                    <i class="bi bi-person-exclamation fs-3"></i>
                    <div>
                        <div class="fw-bold">Guest Session Detected</div>
                        <div class="small">Your session is temporary. <a href="${pageContext.request.contextPath}/login" class="fw-bold">Log in</a> to persist your item preferences across devices.</div>
                    </div>
                </div>
            </c:if>

            <c:if test="${not empty error}">
                <div class="alert alert-danger border-0 bg-danger-subtle mb-4 shadow-none"><i class="bi bi-exclamation-triangle-fill me-2"></i> ${error}</div>
            </c:if>

            <!-- ✅ THÊM (KHÔNG ẢNH HƯỞNG CODE CŨ) -->
            <c:set var="stockError" value="${sessionScope.cartError}" />
            <c:set var="errorProductId" value="${sessionScope.errorProductId}" />

            <c:choose>
                <c:when test="${empty items}">
                    <div class="fm-surface p-5 text-center shadow-none border-dashed" style="border: 2px dashed var(--fm-slate-200);">
                        <i class="bi bi-cart-x fs-1 text-muted opacity-25 mb-3"></i>
                        <h3 class="fw-bold">Cart is currently empty</h3>
                        <p class="text-muted mb-4">No fulfillment requests detected. Explore our catalog to find fresh produce.</p>
                        <a href="${pageContext.request.contextPath}/catalog" class="fm-btn fm-btn-primary px-4">Browse Catalog</a>
                    </div>
                </c:when>
                <c:otherwise>
                    <c:set var="cartTotal" value="${0}"/>
                    <div class="fm-surface p-0 overflow-hidden shadow-none border-0 mb-4">
                        <div class="table-responsive">
                            <table class="table fm-data-table mb-0 align-middle">
                                <thead class="bg-light">
                                    <tr>
                                        <th>Product SKU & Attribution</th>
                                        <th class="text-end">Unit Price</th>
                                        <th style="width: 180px;">Quantity</th>
                                        <th class="text-end">Subtotal</th>
                                        <th class="text-end"></th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="item" items="${items}">
                                        <c:set var="lineSubtotal" value="${item.lineTotal}"/>
                                        <c:set var="cartTotal" value="${cartTotal + lineSubtotal}"/>
                                        <tr>
                                            <td>
                                                <div class="d-flex align-items-center gap-3">
                                                    <div class="fm-product-media" style="width: 64px; height: 64px; border-radius: 12px; flex-shrink: 0;">
                                                        <c:choose>
                                                            <c:when test="${not empty item.product.imageUrl}">
                                                                <img src="${item.product.imageUrl}" alt="${item.product.name}" class="w-100 h-100 object-fit-cover">
                                                            </c:when>
                                                            <c:otherwise>
                                                                <div class="w-100 h-100 bg-light d-flex align-items-center justify-content-center text-muted small">No Media</div>
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </div>
                                                    <div>
                                                        <h4 class="fs-6 fw-bold mb-1"><c:out value="${item.product.name}"/></h4>

                                                        <!-- ✅ THÊM DUY NHẤT CHỖ NÀY -->
                                                        <c:if test="${errorProductId == item.product.id}">
                                                            <div class="alert alert-warning mt-2 py-1 px-2 small">
                                                                ${stockError}
                                                            </div>
                                                        </c:if>

                                                        <div class="fm-caption text-uppercase opacity-50 fw-bold">SKU-${item.product.id} • <c:out value="${item.product.unit}"/></div>
                                                    </div>
                                                </div>
                                            </td>
                                            <td class="text-end fw-semibold">
                                                <fmt:formatNumber value="${item.product.sellPrice}" type="number" groupingUsed="true"/>
                                                <span class="small opacity-50">₫</span>
                                            </td>
                                            <td>
                                                <form action="${pageContext.request.contextPath}/cart" method="post" class="d-flex gap-2">
                                                    <input type="hidden" name="csrf_token" value="${sessionScope.CSRF_TOKEN}">
                                                    <input type="hidden" name="action" value="update">
                                                    <input type="hidden" name="productId" value="${item.product.id}">
                                                    <div class="input-group input-group-sm">
                                                        <input type="number" name="qty" class="fm-form-control text-center py-1" value="${item.quantity}" min="1" max="999">
                                                        <button class="btn btn-outline-primary border-start-0" type="submit" title="Update"><i class="bi bi-arrow-repeat"></i></button>
                                                    </div>
                                                </form>
                                            </td>
                                            <td class="text-end fw-bold text-primary">
                                                <fmt:formatNumber value="${lineSubtotal}" type="number" groupingUsed="true"/>
                                                <span class="small opacity-50">₫</span>
                                            </td>
                                            <td class="text-end">
                                                <form action="${pageContext.request.contextPath}/cart" method="post">
                                                    <input type="hidden" name="csrf_token" value="${sessionScope.CSRF_TOKEN}">
                                                    <input type="hidden" name="action" value="remove">
                                                    <input type="hidden" name="productId" value="${item.product.id}">
                                                    <button type="submit" class="btn btn-link link-danger p-0" title="Remove Entry"><i class="bi bi-trash"></i></button>
                                                </form>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </c:otherwise>
            </c:choose>
            
            <a href="${pageContext.request.contextPath}/catalog" class="fm-btn btn-light border py-2 px-4 shadow-none"><i class="bi bi-arrow-left me-2"></i> Continue Selection</a>
        </div>

        <!-- Sidebar Summary Segment -->
        <div class="col-xl-4 col-lg-6 mx-auto">
            <div class="sticky-top" style="top: 100px;">
                <div class="fm-surface p-4 shadow-sm border-0">
                    <h5 class="fm-h3 border-bottom pb-3 mb-4">Financial Summary</h5>
                    
                    <div class="d-flex justify-content-between mb-3">
                        <span class="text-muted small fw-bold text-uppercase">Subtotal Intake</span>
                        <span class="fw-bold">
                            <fmt:formatNumber value="${cartTotal}" type="number" groupingUsed="true"/>
                            <span class="small opacity-50">₫</span>
                        </span>
                    </div>
                    
                    <div class="d-flex justify-content-between mb-3">
                        <span class="text-muted small fw-bold text-uppercase">logistics fee</span>
                        <span class="text-success small fw-bold">FREE DELIVERY</span>
                    </div>

                    <div class="d-flex justify-content-between mb-4">
                        <span class="text-muted small fw-bold text-uppercase">est. tax</span>
                        <span class="fw-bold">0 <span class="small opacity-50">₫</span></span>
                    </div>

                    <hr class="fm-divider mb-4">

                    <div class="d-flex justify-content-between align-items-end mb-5">
                        <span class="fm-h3 mb-0">Grand Total</span>
                        <div class="text-end font-monospace">
                            <div class="fm-h1 text-primary mb-0" style="font-size: 2.25rem;">
                                <fmt:formatNumber value="${cartTotal}" type="number" groupingUsed="true"/>
                                <span class="fs-5 opacity-50">₫</span>
                            </div>
                        </div>
                    </div>

                    <c:choose>
                        <c:when test="${not empty items}">
                            <c:choose>
                                <c:when test="${empty sessionScope.authUser}">
                                    <div class="d-grid gap-2">
                                        <a href="${pageContext.request.contextPath}/login" class="fm-btn btn-warning py-3 fw-bold text-white shadow-sm">
                                            Log in to Finalize Order
                                        </a>
                                        <div class="fm-caption text-center mt-1">Authentication required for checkout logic.</div>
                                    </div>
                                </c:when>
                                <c:otherwise>
                                    <form action="${pageContext.request.contextPath}/checkout-review" method="get">
                                        <button type="submit" class="fm-btn fm-btn-primary w-100 py-3 fs-5 shadow-sm">
                                            Proceed to Fulfillment <i class="bi bi-chevron-right ms-2"></i>
                                        </button>
                                    </form>
                                </c:otherwise>
                            </c:choose>
                        </c:when>
                        <c:otherwise>
                            <button class="fm-btn btn-light border w-100 py-3 opacity-50" disabled>Select Items First</button>
                        </c:otherwise>
                    </c:choose>

                    <div class="mt-5 pt-4 border-top">
                        <div class="d-flex align-items-center gap-3 mb-3">
                            <i class="bi bi-shield-check text-success fs-4"></i>
                            <div class="small fw-semibold">Quality & Freshness Compliance</div>
                        </div>
                        <div class="d-flex align-items-center gap-3 mb-0">
                            <i class="bi bi-clock-history text-primary fs-4"></i>
                            <div class="small fw-semibold">Optimized FEFO Delivery Logic</div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- ✅ THÊM (KHÔNG ẢNH HƯỞNG GÌ) -->
<c:remove var="cartError" scope="session"/>
<c:remove var="errorProductId" scope="session"/>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>