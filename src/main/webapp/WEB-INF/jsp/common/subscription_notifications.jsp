<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:set var="pageTitle" value="Thông báo subscription" />
<jsp:include page="/WEB-INF/jsp/common/header.jsp" />

<div class="fm-page-header">
    <div>
        <h1 class="fm-page-title">Thông báo subscription</h1>
        <p class="fm-page-subtitle mb-0">Các thông báo tự động do job nền tạo khi PRO sắp hết hạn hoặc đã hết hạn.</p>
    </div>

    <div class="d-flex gap-2 flex-wrap">
        <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/subscription/upgrade">Quay lại Upgrade</a>
        <form action="${pageContext.request.contextPath}/subscription/notifications" method="post" class="d-inline">
            <input type="hidden" name="csrf_token" value="${sessionScope.CSRF_TOKEN}" />
            <button type="submit" class="btn btn-primary">
                <i class="bi bi-check2-all me-1"></i>Đánh dấu đã đọc
            </button>
        </form>
    </div>
</div>

<c:if test="${param.read eq '1'}">
    <div class="alert alert-success">
        <i class="bi bi-check-circle me-1"></i>
        Đã đánh dấu toàn bộ thông báo subscription là đã đọc.
    </div>
</c:if>

<div class="fm-surface padded">
    <div class="d-flex justify-content-between align-items-center mb-3 flex-wrap gap-2">
        <h5 class="mb-0">Danh sách thông báo</h5>
        <span class="badge text-bg-dark">Unread: ${subscriptionUnreadCount}</span>
    </div>

    <c:choose>
        <c:when test="${empty notifications}">
            <div class="alert alert-light border mb-0">Chưa có thông báo subscription nào.</div>
        </c:when>
        <c:otherwise>
            <div class="list-group list-group-flush">
                <c:forEach items="${notifications}" var="n">
                    <div class="list-group-item px-0 py-3 border-bottom">
                        <div class="d-flex justify-content-between align-items-start gap-3 flex-wrap">
                            <div>
                                <div class="d-flex align-items-center gap-2 mb-1">
                                    <strong><c:out value="${n.title}" /></strong>
                                    <c:choose>
                                        <c:when test="${n.read}">
                                            <span class="badge text-bg-secondary">Đã đọc</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge text-bg-danger">Chưa đọc</span>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                                <div class="text-muted small mb-1">
                                    <c:out value="${n.notificationType}" />
                                    <c:if test="${not empty n.eventDate}">
                                        • Event date: <c:out value="${n.eventDate}" />
                                    </c:if>
                                </div>
                                <div><c:out value="${n.message}" /></div>
                            </div>
                            <div class="text-muted small text-nowrap">
                                <c:out value="${n.createdAt}" />
                            </div>
                        </div>
                    </div>
                </c:forEach>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp" />
