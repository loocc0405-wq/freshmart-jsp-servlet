<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="403 Forbidden"/>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"/>

<div class="alert alert-danger">
    <h4 class="alert-heading">403 - Forbidden</h4>
    <p><c:choose><c:when test="${not empty errorMessage}"><c:out value="${errorMessage}"/></c:when><c:otherwise>Bạn không có quyền truy cập.</c:otherwise></c:choose></p>
</div>

<a class="btn btn-secondary" href="${pageContext.request.contextPath}/">Về trang chủ</a>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
