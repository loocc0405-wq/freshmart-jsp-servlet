<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Product Detail"/>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"/>

<c:if test="${product == null}">
    <div class="alert alert-warning">Không tìm thấy sản phẩm.</div>
</c:if>

<c:if test="${product != null}">
    <h3><c:out value="${product.name}"/></h3>

    <p>
        Category: <b><c:out value="${product.category}"/></b><br/>
        Giá bán: <b><c:out value="${product.sellPrice}"/></b> / <c:out value="${product.unit}"/><br/>
        Tồn kho khả dụng (chưa hết hạn): <b><c:out value="${availableQty}"/></b>
    </p>

    <c:if test="${not empty product.description}">
        <div class="card">
            <div class="card-body">
                <c:out value="${product.description}"/>
            </div>
        </div>
    </c:if>
</c:if>

<a class="btn btn-secondary mt-3" href="${pageContext.request.contextPath}/catalog">Back</a>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
