<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<c:set var="pageTitle" value="SKU Master Ledger | FreshMart Enterprise"/>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"/>

<div class="container-fluid px-4 py-4">
    <div class="fm-page-header mb-5 border-bottom pb-4">
        <div>
            <div class="fm-caption fw-bold text-primary mb-1 text-uppercase ls-wide">Master Data Management</div>
            <h1 class="fm-page-title">SKU & Inventory Master Ledger</h1>
            <p class="fm-page-subtitle">Centralized control for entire product ecosystem, pricing distribution, and lifecycle status.</p>
        </div>

        <div class="d-flex flex-wrap gap-2">
            <a class="fm-btn fm-btn-primary" href="${pageContext.request.contextPath}/admin/add-product">
                <i class="bi bi-plus-lg me-1"></i> Initialize New SKU
            </a>
        </div>
    </div>

    <!-- Alert Messaging -->
    <c:if test="${not empty sessionScope.productActionSuccess}">
        <div class="alert alert-success fm-surface border-0 shadow-sm mb-4 p-3 anim-fade-in shadow-sm">
            <i class="bi bi-check-circle-fill me-2"></i> <c:out value="${sessionScope.productActionSuccess}"/>
        </div>
        <c:remove var="productActionSuccess" scope="session"/>
    </c:if>

    <!-- Advanced SKU Query Engine -->
    <div class="fm-surface p-4 mb-5 border-0 shadow-sm bg-slate-50">
        <form class="row g-4 align-items-end" method="get" action="${pageContext.request.contextPath}/admin/products">
            <div class="col-12 col-md-4 col-xl-3">
                <label class="fm-caption fw-bold d-block mb-2">Search Logic</label>
                <div class="position-relative">
                    <i class="bi bi-search position-absolute top-50 start-0 translate-middle-y ms-3 opacity-50"></i>
                    <input type="text" name="keyword" class="fm-form-control ps-5" value="${keyword}" placeholder="SKU Name or Fragment...">
                </div>
            </div>
            
            <div class="col-12 col-md-3 col-xl-2">
                <label class="fm-caption fw-bold d-block mb-2">Department</label>
                <input type="text" name="category" class="fm-form-control" value="${category}" placeholder="e.g. Seafood">
            </div>

            <div class="col-12 col-md-3 col-xl-2">
                <label class="fm-caption fw-bold d-block mb-3">Audit Flags</label>
                <div class="form-check form-switch p-0 pt-1">
                    <label class="form-check-label small fw-bold text-muted ps-5" for="showInactiveSwitch">Include Inactive SKUs</label>
                    <input class="form-check-input ms-0" type="checkbox" name="showInactive" id="showInactiveSwitch" ${showInactive ? 'checked' : ''} style="width: 40px; height: 20px;">
                </div>
            </div>

            <div class="col-12 col-md-2 d-flex gap-2">
                <button type="submit" class="fm-btn fm-btn-primary flex-grow-1">Execute Query</button>
                <a href="${pageContext.request.contextPath}/admin/products" class="fm-btn btn-light border" title="Reset Filters"><i class="bi bi-arrow-counterclockwise"></i></a>
            </div>
        </form>
        
        <c:if test="${totalItems > 0}">
            <div class="mt-4 small fw-bold text-muted d-flex align-items-center gap-2">
                <i class="bi bi-info-circle"></i>
                Displaying indexed records ${(currentPage - 1) * pageSize + 1} to ${currentPage * pageSize > totalItems ? totalItems : currentPage * pageSize} of ${totalItems} SKUs
            </div>
        </c:if>
    </div>

    <!-- Master SKU Ledger Table -->
    <div class="fm-surface p-0 overflow-hidden shadow-sm border-0">
        <div class="table-responsive">
            <table class="table fm-data-table align-middle mb-0">
                <thead class="bg-light">
                    <tr>
                        <th class="ps-4">Internal ID</th>
                        <th>Product Attribution & Identity</th>
                        <th>Department</th>
                        <th class="text-end">Standard Price</th>
                        <th class="text-center">Operational Status</th>
                        <th class="text-end pe-4">Thao tác</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="p" items="${products}">
                        <tr class="${!p.active ? 'opacity-75 grayscale' : ''}">
                            <td class="ps-4"><span class="badge bg-slate-100 text-slate-600 font-monospace border-0">${p.id}</span></td>
                            <td>
                                <div class="fw-bold text-dark"><c:out value="${p.name}"/></div>
                                <div class="fm-caption opacity-50 small">Managed Service Unit</div>
                            </td>
                            <td><span class="small fw-bold text-uppercase opacity-75">${p.category}</span></td>
                            <td class="text-end fw-bold font-monospace">
                                <fmt:formatNumber value="${p.sellPrice}" type="number" groupingUsed="true"/> ₫
                            </td>
                            <td class="text-center">
                                <c:choose>
                                    <c:when test="${p.active}">
                                        <span class="fm-status-badge available px-3 py-1 small">ACTIVE</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="fm-status-badge disposed px-3 py-1 small">INACTIVE</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td class="text-end pe-4">
                                <div class="d-flex justify-content-end gap-2">
                                    <form method="post" action="${pageContext.request.contextPath}/admin/delete-product" onsubmit="return confirm('You are about to purge this SKU metadata. Proceed?');">
                                        <input type="hidden" name="csrf_token" value="${sessionScope.CSRF_TOKEN}" />
                                        <input type="hidden" name="id" value="${p.id}" />
                                        <button type="submit" class="btn btn-sm btn-outline-danger rounded-pill px-3 py-1 small fw-bold">Purge</button>
                                    </form>
                                </div>
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty products}">
                        <tr>
                            <td colspan="6" class="p-5 text-center fm-text-secondary">
                                <i class="bi bi-inbox fs-2 opacity-25 d-block mb-2"></i>
                                No SKU records matched the query parameters in the master data hub.
                            </td>
                        </tr>
                    </c:if>
                </tbody>
            </table>
        </div>

        <!-- Master Pagination -->
        <c:if test="${totalPages > 1}">
            <div class="p-4 bg-light border-top">
                <nav>
                    <ul class="pagination pagination-enterprise mb-0 justify-content-center gap-2">
                        <!-- Simplified pagination logic for redesign consistency -->
                        <c:if test="${currentPage > 1}">
                            <li class="page-item"><a class="page-link rounded" href="${pageContext.request.contextPath}/admin/products?page=${currentPage - 1}&keyword=${keyword}&category=${category}&showInactive=${showInactive}"><i class="bi bi-chevron-left"></i> Previous</a></li>
                        </c:if>
                        
                        <c:set var="startPage" value="${currentPage - 2 > 0 ? currentPage - 2 : 1}"/>
                        <c:set var="endPage" value="${startPage + 4 < totalPages ? startPage + 4 : totalPages}"/>
                        
                        <c:forEach var="i" begin="${startPage}" end="${endPage}">
                            <li class="page-item ${i == currentPage ? 'active' : ''}">
                                <a class="page-link rounded fw-bold" href="${pageContext.request.contextPath}/admin/products?page=${i}&keyword=${keyword}&category=${category}&showInactive=${showInactive}">${i}</a>
                            </li>
                        </c:forEach>

                        <c:if test="${currentPage < totalPages}">
                            <li class="page-item"><a class="page-link rounded" href="${pageContext.request.contextPath}/admin/products?page=${currentPage + 1}&keyword=${keyword}&category=${category}&showInactive=${showInactive}">Next <i class="bi bi-chevron-right"></i></a></li>
                        </c:if>
                    </ul>
                </nav>
            </div>
        </c:if>
    </div>
</div>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>