<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Login"/>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"/>
<div class="row justify-content-center">
    <div class="col-md-5">
        <div class="card shadow-sm">
            <div class="card-body">
                <h3 class="card-title mb-3">Đăng nhập</h3>

                <c:if test="${not empty error}">
                    <div class="alert alert-danger"><c:out value="${error}"/></div>
                </c:if>

                <form method="post" action="${pageContext.request.contextPath}/login">
                    <input type="hidden" name="return" value="${param["return"]}">

                    <div class="mb-3">
                        <label class="form-label">Username</label>
                        <input class="form-control" name="username" required/>
                    </div>

                    <div class="mb-3">
                        <label class="form-label">Password</label>
                        <input class="form-control" type="password" name="password" required/>
                    </div>

                    <button class="btn btn-primary w-100" type="submit">Login</button>

                    <hr/>
                    <p class="text-muted mb-0">
                        Default accounts: admin/admin123, staff/staff123, seller/seller123, customer/customer123
                    </p>
                </form>
            </div>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
