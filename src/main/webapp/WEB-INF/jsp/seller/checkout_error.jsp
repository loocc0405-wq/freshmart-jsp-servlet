<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Checkout Error"/>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"/>

<div class="alert alert-danger">
    <h4>Checkout thất bại</h4>
    <p><c:out value="${errorMessage}"/></p>
</div>

<a class="btn btn-secondary" href="${pageContext.request.contextPath}/seller/pos">Back</a>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
