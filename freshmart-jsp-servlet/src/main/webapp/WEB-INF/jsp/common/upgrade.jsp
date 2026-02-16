<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Upgrade to PRO"/>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"/>

<div class="alert alert-warning">
    <h4>Tính năng trả phí (PRO)</h4>
    <p>Bạn chưa có PRO hoặc đã hết hạn.</p>
</div>

<c:if test="${not empty errorMessage}">
    <div class="alert alert-danger"><c:out value="${errorMessage}"/></div>
</c:if>

<form method="post" action="${pageContext.request.contextPath}/subscription/upgrade" class="card">
    <div class="card-body">
        <label class="form-label">Chọn gói (days)</label>
        <select class="form-select" name="planDays">
            <option value="30">30 days</option>
            <option value="90">90 days</option>
            <option value="365">365 days</option>
        </select>
        <button class="btn btn-primary mt-3">Fake payment & Upgrade</button>
    </div>
</form>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
