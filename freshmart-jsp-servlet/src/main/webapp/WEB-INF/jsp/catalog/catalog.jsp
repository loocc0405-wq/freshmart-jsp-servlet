<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Catalog"/>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"/>

<h3>Danh sách sản phẩm</h3>

<form class="row g-2 mb-3" method="get" action="${pageContext.request.contextPath}/catalog">
    <div class="col-md-5">
        <input class="form-control" name="q" placeholder="Tìm theo tên..." value="${param.q}"/>
    </div>
    <div class="col-md-3">
        <input class="form-control" name="category" placeholder="Category..." value="${param.category}"/>
    </div>
    <div class="col-md-2">
        <button class="btn btn-outline-primary w-100">Search</button>
    </div>
    <div class="col-md-2">
        <a class="btn btn-outline-secondary w-100" href="${pageContext.request.contextPath}/catalog">Reset</a>
    </div>
</form>

<div class="row">
    <c:forEach items="${products}" var="p">
        <div class="col-md-4 mb-3">
            <div class="card h-100">
                <c:if test="${not empty p.imageUrl}">
                    <img class="card-img-top" src="${p.imageUrl}" alt="image"/>
                </c:if>
                <div class="card-body">
                    <h5 class="card-title"><c:out value="${p.name}"/></h5>
                    <p class="card-text">
                        Category: <c:out value="${p.category}"/><br/>
                        Giá: <b><c:out value="${p.sellPrice}"/></b> / <c:out value="${p.unit}"/>
                    </p>
                    <a class="btn btn-sm btn-primary" href="${pageContext.request.contextPath}/product?id=${p.id}">Chi tiết</a>
                </div>
            </div>
        </div>
    </c:forEach>
</div>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
