<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Staff"/>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"/>

<h3>Staff Home</h3>
<p class="text-muted">Placeholder. Bạn có thể bổ sung quản lý nhà cung cấp, sản phẩm, lô hàng, OMS,...</p>

<ul>
    <li><a href="${pageContext.request.contextPath}/seller/pos">Seller POS (demo)</a></li>
    <li><a href="${pageContext.request.contextPath}/pro/dashboard">Dashboard doanh thu</a></li>
</ul>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
