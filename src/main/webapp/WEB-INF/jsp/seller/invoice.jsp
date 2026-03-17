<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<c:set var="pageTitle" value="Settlement Invoice | FreshMart Enterprise"/>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"/>

<style>
    .invoice-container {
        max-width: 900px;
        margin: 2rem auto;
        background: #fff;
        border-radius: 20px;
        overflow: hidden;
        border: 1px solid var(--fm-slate-200);
    }
    .invoice-brand {
        background: var(--fm-primary-600);
        color: #fff;
        padding: 3rem;
        display: flex;
        justify-content: space-between;
        align-items: center;
    }
    .invoice-body {
        padding: 3rem;
    }
    .invoice-meta-grid {
        display: grid;
        grid-template-columns: repeat(2, 1fr);
        gap: 2rem;
        margin-bottom: 3rem;
    }
    .invoice-line-items {
        width: 100%;
        margin-bottom: 3rem;
    }
    .invoice-line-items th {
        background: var(--fm-slate-50);
        padding: 1rem;
        font-size: 0.75rem;
        font-weight: 700;
        text-transform: uppercase;
        color: var(--fm-slate-500);
        letter-spacing: 0.05em;
        border-bottom: 2px solid var(--fm-slate-200);
    }
    .invoice-line-items td {
        padding: 1.25rem 1rem;
        border-bottom: 1px solid var(--fm-slate-100);
        color: var(--fm-slate-700);
    }
    .invoice-total-section {
        background: var(--fm-slate-50);
        padding: 2rem;
        border-radius: 16px;
        margin-left: auto;
        max-width: 350px;
    }
    @media print {
        .fm-header, .fm-footer, .invoice-actions {
            display: none !important;
        }
        .invoice-container {
            border: 0;
            margin: 0;
            max-width: 100%;
        }
    }
</style>

<div class="container py-4">
    <div class="invoice-actions d-flex justify-content-between align-items-center mb-4">
        <a class="fm-btn btn-light border" href="${pageContext.request.contextPath}/seller/pos">
            <i class="bi bi-arrow-left me-2"></i> Return to POS
        </a>
        <button class="fm-btn fm-btn-primary" onclick="window.print();">
            <i class="bi bi-printer me-2"></i> Print Documents
        </button>
    </div>

    <c:choose>
        <c:when test="${order == null}">
            <div class="fm-surface p-5 text-center shadow-sm border-0">
                <i class="bi bi-search display-1 text-muted opacity-25 mb-3"></i>
                <h4 class="text-dark">Order Document Not Located</h4>
                <p class="text-muted">The requested fulfillment record could not be found in the current session context.</p>
            </div>
        </c:when>
        <c:otherwise>
            <div class="invoice-container shadow-lg">
                <div class="invoice-brand">
                    <div>
                        <h2 class="fw-bold mb-1">FRESHMART</h2>
                        <div class="opacity-75 small">Enterprise Procurement Document</div>
                    </div>
                    <div class="text-end">
                        <div class="fm-h3 mb-0 text-white">INVOICE</div>
                        <div class="opacity-75 small">Ref: <c:out value="${order.orderCode}"/></div>
                    </div>
                </div>

                <div class="invoice-body">
                    <div class="invoice-meta-grid">
                        <div>
                            <div class="fm-caption fw-bold text-muted mb-2 text-uppercase ls-wide">Origin Terminal</div>
                            <div class="fw-bold text-dark">FreshMart Main Hub</div>
                            <div class="small text-muted">Counter Terminal ID: POS-CENTRAL-01</div>
                            <div class="small text-muted">Merchant Assigned: <c:out value="${sessionScope.USER.fullName}"/></div>
                        </div>
                        <div class="text-end">
                            <div class="fm-caption fw-bold text-muted mb-2 text-uppercase ls-wide">Transaction Details</div>
                            <div class="fw-bold text-dark">Committed: <c:out value="${order.completedAt}"/></div>
                            <div class="small text-muted">Settlement: <span class="badge bg-slate-100 text-slate-700 border-0">${order.paymentMethod}</span></div>
                            <div class="small text-muted">Status: <span class="badge bg-success-subtle text-success border-0">${order.status}</span></div>
                        </div>
                    </div>

                    <table class="invoice-line-items">
                        <thead>
                            <tr>
                                <th>Product Specifications</th>
                                <th class="text-center">Quantity</th>
                                <th class="text-end">Unit Valuation</th>
                                <th class="text-end">Total Valuation</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach items="${order.items}" var="it">
                                <tr>
                                    <td>
                                        <div class="fw-bold text-dark">${it.product.name}</div>
                                        <div class="small text-muted">SKU: FM-${it.product.id}</div>
                                    </td>
                                    <td class="text-center fw-medium">${it.quantity}</td>
                                    <td class="text-end font-monospace">₫<fmt:formatNumber value="${it.unitPrice}" groupingUsed="true"/></td>
                                    <td class="text-end font-monospace fw-bold">₫<fmt:formatNumber value="${it.lineTotal}" groupingUsed="true"/></td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>

                    <div class="invoice-total-section">
                        <div class="d-flex justify-content-between align-items-center mb-2">
                            <span class="text-muted small fw-bold">SUBTOTAL</span>
                            <span class="font-monospace fw-bold">₫<fmt:formatNumber value="${order.totalAmount}" groupingUsed="true"/></span>
                        </div>
                        <div class="d-flex justify-content-between align-items-center mb-2">
                            <span class="text-muted small fw-bold">TAX (VAT 0%)</span>
                            <span class="font-monospace fw-bold">₫0</span>
                        </div>
                        <hr class="border-secondary opacity-10 my-3">
                        <div class="d-flex justify-content-between align-items-center">
                            <span class="text-dark fw-bold">GRAND TOTAL</span>
                            <span class="fm-h3 mb-0 text-primary">₫<fmt:formatNumber value="${order.totalAmount}" groupingUsed="true"/></span>
                        </div>
                    </div>

                    <div class="mt-5 pt-5 text-center border-top">
                        <div class="fm-caption fw-bold text-muted mb-2 text-uppercase">Operational Compliance</div>
                        <p class="small text-muted mb-0">System-generated settlement document for inventory audit and revenue reconciliation. No signature required for digital terminal confirmation.</p>
                    </div>
                </div>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
