<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="pageTitle" value="Thêm Seller"/>

<jsp:include page="/WEB-INF/jsp/common/header.jsp"/>

<div class="container my-4">

<h3 class="fm-page-title mb-3">Thêm Seller</h3>

<c:if test="${not empty error}">
<div class="alert alert-danger">${error}</div>
</c:if>

<form method="post" class="bg-white p-3 rounded-3 border">

<input type="hidden" name="csrf_token" value="${sessionScope.CSRF_TOKEN}" />

<div class="mb-2">
<label class="form-label">Username</label>
<input class="form-control" name="username" required/>
</div>

<div class="mb-2">
<label class="form-label">Password (>= 6 ký tự)</label>
<input class="form-control" name="password" type="password" required/>
</div>

<div class="mb-2">
<label class="form-label">Họ tên</label>
<input class="form-control" name="fullName"/>
</div>

<div class="mb-2">
<label class="form-label">SĐT</label>
<input class="form-control" name="phone"/>
</div>

<div class="mb-3">
<label class="form-label">Địa chỉ</label>
<input class="form-control" name="address"/>
</div>

<button class="btn btn-primary" type="submit">Tạo Seller</button>

<a class="btn btn-secondary" href="${pageContext.request.contextPath}/admin/sellers">
Hủy
</a>

</form>

</div>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>