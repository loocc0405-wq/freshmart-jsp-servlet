<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Logout"/>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"/>

<h3>Đăng xuất</h3>
<p>Bạn chắc chắn muốn đăng xuất khỏi hệ thống?</p>

<form method="post" action="${pageContext.request.contextPath}/logout" class="d-inline">
    <input type="hidden" name="csrf_token" value="${sessionScope.CSRF_TOKEN}"/>
    <button type="submit" class="btn btn-danger">Logout</button>
</form>

<a class="btn btn-secondary ms-2" href="${pageContext.request.contextPath}/catalog">Hủy</a>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>