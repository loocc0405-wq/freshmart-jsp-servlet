<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:set var="pageTitle" value="Merchant Terminal | FreshMart Enterprise"/>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"/>

<style>
    .fm-pos-container {
        display: grid;
        grid-template-columns: 1fr 400px;
        gap: 1.5rem;
        min-height: calc(100vh - 200px);
    }
    .fm-catalog-grid {
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
        gap: 1rem;
    }
    .fm-pos-item-card {
        background: #fff;
        border: 1px solid var(--fm-slate-200);
        border-radius: 16px;
        padding: 1.25rem;
        transition: all 0.2s;
        cursor: pointer;
        display: flex;
        flex-direction: column;
        justify-content: space-between;
    }
    .fm-pos-item-card:hover {
        border-color: var(--fm-primary-500);
        box-shadow: 0 4px 12px rgba(0,0,0,0.05);
        transform: translateY(-2px);
    }
    .fm-pos-item-card.disabled {
        opacity: 0.6;
        cursor: not-allowed;
        background: var(--fm-slate-50);
    }
    .fm-pos-cart-panel {
        background: #fff;
        border: 1px solid var(--fm-slate-200);
        border-radius: 20px;
        display: flex;
        flex-direction: column;
        position: sticky;
        top: 1.5rem;
        height: fit-content;
        max-height: calc(100vh - 100px);
    }
    .pos-cart-items {
        flex: 1;
        overflow-y: auto;
        padding: 1.5rem;
    }
    .pos-cart-footer {
        padding: 1.5rem;
        background: var(--fm-slate-50);
        border-top: 1px solid var(--fm-slate-200);
        border-radius: 0 0 20px 20px;
    }
</style>

<div class="container-fluid px-4 py-4">
    <!-- Header Section -->
    <div class="fm-page-header mb-4 border-bottom pb-3">
        <div class="d-flex justify-content-between align-items-center">
            <div>
                <div class="fm-caption fw-bold text-primary mb-1 text-uppercase ls-wide">Counter Operations</div>
                <h1 class="fm-page-title mb-0">Merchant POS Terminal</h1>
            </div>
            <div class="d-flex gap-2">
                <div class="badge bg-indigo-100 text-indigo-700 p-2 px-3 rounded-pill fw-bold">
                    <i class="bi bi-clock me-1"></i> <span id="pos-clock">--:--:--</span>
                </div>
            </div>
        </div>
    </div>

    <!-- Alert Messaging -->
    <c:if test="${not empty sessionScope.sellerPosSuccessMessage}">
        <div class="alert alert-success fm-surface border-0 shadow-sm mb-4 p-3 anim-fade-in shadow-sm">
            <i class="bi bi-check-circle-fill me-2"></i> <c:out value="${sessionScope.sellerPosSuccessMessage}"/>
        </div>
        <c:remove var="sellerPosSuccessMessage" scope="session"/>
    </c:if>
    <c:if test="${not empty sessionScope.sellerPosErrorMessage}">
        <div class="alert alert-danger fm-surface border-0 shadow-sm mb-4 p-3 anim-fade-in shadow-sm">
            <i class="bi bi-exclamation-octagon-fill me-2"></i> <c:out value="${sessionScope.sellerPosErrorMessage}"/>
        </div>
        <c:remove var="sellerPosErrorMessage" scope="session"/>
    </c:if>

    <div class="fm-pos-container">
        <!-- Product Selection Area -->
        <div class="fm-pos-inventory">
            <div class="fm-surface p-3 mb-4 shadow-sm border-0 d-flex gap-3">
                <div class="flex-grow-1">
                    <div class="input-group">
                        <span class="input-group-text bg-white border-end-0"><i class="bi bi-search text-muted"></i></span>
                        <input type="text" id="posSearch" class="fm-form-control border-start-0" placeholder="Scan barcode or type SKU name..." onkeyup="filterPOS()"/>
                    </div>
                </div>
                <div class="d-none d-md-block">
                    <select class="fm-form-control">
                        <option>All Categories</option>
                    </select>
                </div>
            </div>

            <div class="fm-catalog-grid" id="posItems">
                <c:forEach items="${products}" var="p">
                    <div class="fm-pos-item-card ${availableMap[p.id] <= 0 ? 'disabled' : ''}" data-name="${p.name.toUpperCase()}">
                        <div>
                            <div class="d-flex justify-content-between align-items-start mb-2">
                                <span class="badge bg-slate-100 text-slate-500 font-monospace small">#${p.id}</span>
                                <c:choose>
                                    <c:when test="${availableMap[p.id] <= 0}">
                                        <span class="badge bg-danger">OUT</span>
                                    </c:when>
                                    <c:when test="${availableMap[p.id] < 10}">
                                        <span class="badge bg-warning text-dark">L-STOCK</span>
                                    </c:when>
                                </c:choose>
                            </div>
                            <h6 class="fw-bold text-dark mb-1 lh-sm">${p.name}</h6>
                            <div class="text-primary fw-bold font-monospace fs-5">₫<fmt:formatNumber value="${p.sellPrice}" groupingUsed="true"/></div>
                        </div>
                        
                        <div class="mt-3">
                            <div class="d-flex justify-content-between small text-muted mb-2">
                                <span>Stock: <b>${availableMap[p.id]}</b></span>
                                <span>Exp: <b>${nearestExpiryMap[p.id] != null ? nearestExpiryMap[p.id] : 'N/A'}</b></span>
                            </div>
                            <form method="post" action="${pageContext.request.contextPath}/seller/pos">
                                <input type="hidden" name="csrf_token" value="${sessionScope.CSRF_TOKEN}" />
                                <input type="hidden" name="productId" value="${p.id}"/>
                                <div class="input-group input-group-sm">
                                    <input class="form-control text-center" name="quantity" type="number" min="1" value="1" style="max-width: 60px;"/>
                                    <button class="btn btn-primary fw-bold flex-grow-1" type="submit" ${availableMap[p.id] <= 0 ? 'disabled' : ''}>
                                        <i class="bi bi-plus-lg me-1"></i> ADD
                                    </button>
                                </div>
                            </form>
                        </div>
                    </div>
                </c:forEach>
            </div>
        </div>

        <!-- Checkout Side Panel -->
        <div class="fm-pos-cart-panel shadow-lg border-0">
            <div class="p-4 border-bottom bg-white rounded-top-4">
                <h5 class="fm-h3 mb-0 d-flex justify-content-between align-items-center text-dark">
                    <span>Active Order</span>
                    <span class="badge bg-indigo-600 rounded-pill fs-6">${lines.size()}</span>
                </h5>
            </div>

            <div class="pos-cart-items">
                <c:if test="${empty lines}">
                    <div class="h-100 d-flex flex-column align-items-center justify-content-center text-center opacity-25">
                        <i class="bi bi-cart-x display-1 mb-3"></i>
                        <p class="fw-bold">Terminal Empty</p>
                    </div>
                </c:if>

                <c:if test="${not empty lines}">
                    <div class="vstack gap-3">
                        <c:forEach items="${lines}" var="l">
                            <div class="p-3 border rounded-4 bg-light position-relative">
                                <div class="d-flex justify-content-between align-items-start mb-1">
                                    <div class="fw-bold text-dark lh-sm pe-4">${l.product.name}</div>
                                    <div class="bg-indigo-100 text-indigo-700 px-2 py-1 rounded small fw-bold">₫<fmt:formatNumber value="${l.lineTotal}" groupingUsed="true"/></div>
                                </div>
                                <div class="d-flex justify-content-between align-items-center small text-muted">
                                    <span>${l.quantity} × ₫<fmt:formatNumber value="${l.product.sellPrice}" groupingUsed="true"/></span>
                                    <c:choose>
                                        <c:when test="${cartShortageMap[l.product.id] > 0}">
                                            <span class="text-danger fw-bold"><i class="bi bi-exclamation-triangle"></i> Short: ${cartShortageMap[l.product.id]}</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="text-success fw-bold"><i class="bi bi-check-circle"></i> In-Stock</span>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </div>
                        </c:forEach>
                    </div>
                </c:if>
            </div>

            <div class="pos-cart-footer">
                <div class="d-flex justify-content-between align-items-center mb-4">
                    <span class="fm-caption fw-bold text-muted text-uppercase">Total Payable</span>
                    <span class="fm-h2 mb-0 text-primary">₫<fmt:formatNumber value="${total}" groupingUsed="true"/></span>
                </div>

                <c:if test="${not empty lines}">
                    <form method="post" action="${pageContext.request.contextPath}/seller/pos/checkout">
                        <input type="hidden" name="csrf_token" value="${sessionScope.CSRF_TOKEN}" />
                        <div class="mb-3">
                            <label class="fm-caption fw-bold d-block mb-2 text-muted">Settlement Method</label>
                            <select class="fm-form-control py-3 fw-bold" name="paymentMethod">
                                <option value="CASH">💵 PHYSICAL CASH</option>
                                <option value="BANK_TRANSFER">🏦 BANK TRANSFER</option>
                                <option value="QR">📱 DIGITAL WALLET / QR</option>
                            </select>
                        </div>
                        <button class="fm-btn fm-btn-primary w-100 py-3 fw-bold shadow-lg" type="submit" ${cartHasShortage ? 'disabled' : ''}>
                            INITIALIZE SETTLEMENT
                        </button>
                    </form>

                    <form method="post" action="${pageContext.request.contextPath}/seller/pos/clear" class="mt-2">
                        <input type="hidden" name="csrf_token" value="${sessionScope.CSRF_TOKEN}" />
                        <button class="btn btn-link text-danger w-100 border-0 text-decoration-none fw-bold small" type="submit">
                            <i class="bi bi-trash3 me-1"></i> RESET TERMINAL
                        </button>
                    </form>
                </c:if>
            </div>
        </div>
    </div>
</div>

<script>
    function filterPOS() {
        let input = document.getElementById('posSearch');
        let filter = input.value.toUpperCase();
        let items = document.getElementById('posItems').getElementsByClassName('fm-pos-item-card');
        for (let i = 0; i < items.length; i++) {
            let name = items[i].getAttribute('data-name');
            items[i].style.display = name.indexOf(filter) > -1 ? "" : "none";
        }
    }

    function updateClock() {
        const now = new Date();
        const time = now.getHours().toString().padStart(2, '0') + ':' + 
                     now.getMinutes().toString().padStart(2, '0') + ':' + 
                     now.getSeconds().toString().padStart(2, '0');
        document.getElementById('pos-clock').textContent = time;
    }
    setInterval(updateClock, 1000);
    updateClock();
</script>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
