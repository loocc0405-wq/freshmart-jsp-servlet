<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Upgrade to PRO"/>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"/>

<div class="alert alert-warning">
    <h4>Tính năng trả phí (PRO)</h4>
    <p>
        Tài khoản của bạn chưa có gói <b>PRO</b> hoặc đã hết hạn, nên không truy cập được Module dự báo.
    </p>
    <p class="mb-0">
        (Trong bài PRJ, bạn có thể làm module "fake payment" để gia hạn <code>expired_date</code>.)
    </p>
</div>

<a class="btn btn-primary" href="${pageContext.request.contextPath}/catalog">Về Catalog</a>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
