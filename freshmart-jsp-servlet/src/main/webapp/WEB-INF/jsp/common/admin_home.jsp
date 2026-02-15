<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Admin"/>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"/>

<h3>Admin Home</h3>
<p class="text-muted">Placeholder. Bạn có thể bổ sung CRUD user, gói dịch vụ, cấu hình hệ thống,...</p>

<ul>
    <li><a href="${pageContext.request.contextPath}/catalog">Catalog</a></li>
    <li><a href="${pageContext.request.contextPath}/pro/dashboard">PRO Dashboard</a></li>
</ul>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
