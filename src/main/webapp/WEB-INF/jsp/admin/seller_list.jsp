<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:set var="pageTitle" value="Merchant Hub Management | FreshMart Enterprise"/>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"/>

<div class="container-fluid px-4 py-4">
    <div class="fm-page-header mb-5 border-bottom pb-4">
        <div>
            <div class="fm-caption fw-bold text-primary mb-1 text-uppercase ls-wide">Partner Operations</div>
            <h1 class="fm-page-title">Merchant Hub Intelligence</h1>
            <p class="fm-page-subtitle">Standardized ledger for partner identity, operational status, and merchant performance metrics.</p>
        </div>

        <div class="d-flex flex-wrap gap-2">
            <a class="fm-btn fm-btn-primary" href="${pageContext.request.contextPath}/admin/sellers/add">
                <i class="bi bi-person-plus-fill me-1"></i> Register New Merchant
            </a>
        </div>
    </div>

    <!-- Merchant Data Ledger -->
    <div class="fm-surface p-0 overflow-hidden shadow-sm border-0">
        <div class="table-responsive">
            <table class="table fm-data-table align-middle mb-0">
                <thead class="bg-light">
                    <tr>
                        <th class="ps-4">Merchant ID</th>
                        <th>Strategic Identity</th>
                        <th>Communication Hub</th>
                        <th class="text-center">Lifecycle Status</th>
                        <th class="text-end pe-4">Thao tác</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="u" items="${sellers}">
                        <tr class="${!u.active ? 'opacity-75 grayscale' : ''}">
                            <td class="ps-4">
                                <span class="badge bg-slate-100 text-slate-600 font-monospace border-0">${u.id}</span>
                            </td>
                            <td>
                                <div class="fw-bold text-dark"><c:out value="${u.fullName}"/></div>
                                <div class="small font-monospace text-primary">@${u.username}</div>
                            </td>
                            <td>
                                <div class="small fw-medium"><i class="bi bi-telephone me-2 opacity-50"></i><c:out value="${u.phone}"/></div>
                            </td>
                            <td class="text-center">
                                <c:choose>
                                    <c:when test="${u.active}">
                                        <span class="fm-status-badge available px-3 py-1 small">OPERATIONAL</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="fm-status-badge disposed px-3 py-1 small">LOCKED</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td class="text-end pe-4">
                                <div class="d-flex justify-content-end gap-2">
                                    <form method="post" action="${pageContext.request.contextPath}/admin/sellers/toggle">
                                        <input type="hidden" name="id" value="${u.id}"/>
                                        <button class="fm-btn ${u.active ? 'btn-outline-danger' : 'btn-outline-success'} btn-sm px-3 fw-bold" type="submit">
                                            <i class="bi ${u.active ? 'bi-lock' : 'bi-unlock'} me-1"></i>
                                            ${u.active ? 'Terminate' : 'Activate'}
                                        </button>
                                    </form>
                                    <button class="fm-btn btn-light border btn-sm px-3 fw-bold">Profile Audit</button>
                                </div>
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty sellers}">
                        <tr>
                            <td colspan="5" class="p-5 text-center text-muted">
                                <i class="bi bi-people fs-1 opacity-10 mb-2 d-block"></i>
                                No merchant partner records initialized in the central hub.
                            </td>
                        </tr>
                    </c:if>
                </tbody>
            </table>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>