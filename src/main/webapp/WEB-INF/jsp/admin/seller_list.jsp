<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Quản lý Seller"/>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"/>

<div class="container my-4">
  <div class="d-flex justify-content-between align-items-center mb-3">
    <h3 class="fm-page-title">Quản lý Seller</h3>
    <a class="btn btn-primary" href="${pageContext.request.contextPath}/admin/sellers/add">+ Thêm Seller</a>
  </div>

  <table class="table table-bordered bg-white">
    <thead>
      <tr>
        <th>ID</th><th>Username</th><th>Họ tên</th><th>SĐT</th><th>Trạng thái</th><th>Action</th>
      </tr>
    </thead>
    <tbody>
      <c:forEach var="u" items="${sellers}">
        <tr>
          <td>${u.id}</td>
          <td>${u.username}</td>
          <td><c:out value="${u.fullName}"/></td>
          <td><c:out value="${u.phone}"/></td>
          <td>
            <c:choose>
              <c:when test="${u.active}"><span class="badge text-bg-success">ACTIVE</span></c:when>
              <c:otherwise><span class="badge text-bg-secondary">LOCKED</span></c:otherwise>
            </c:choose>
          </td>
          <td>
            <form method="post" action="${pageContext.request.contextPath}/admin/sellers/toggle" style="display:inline;">
              <input type="hidden" name="id" value="${u.id}"/>
              <button class="btn btn-sm btn-outline-dark" type="submit">
                <c:choose>
                  <c:when test="${u.active}">Khóa</c:when>
                  <c:otherwise>Mở</c:otherwise>
                </c:choose>
              </button>
            </form>
          </td>
        </tr>
      </c:forEach>
    </tbody>
  </table>
</div>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>