<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<html>
<head>
    <title>Supplier Management</title>
</head>
<body>

<h2>Supplier List</h2>

<a href="${pageContext.request.contextPath}/staff/suppliers?action=add">
    ➕ Add New Supplier
</a>

<br><br>

<table border="1" cellpadding="8">
    <tr>
        <th>ID</th>
        <th>Name</th>
        <th>Phone</th>
        <th>Email</th>
        <th>Address</th>
        <th>Action</th>
    </tr>

    <c:forEach var="s" items="${suppliers}"></c:forEach>
        <tr>
            <td>${s.id}</td>
            <td>${s.name}</td>
            <td>${s.phone}</td>
            <td>${s.email}</td>
            <td>${s.address}</td>
            <td>
                <a href="${pageContext.request.contextPath}/staff/suppliers?action=edit&id=${s.id}">
                    Edit
                </a>
                |
                <a href="${pageContext.request.contextPath}/staff/suppliers?action=delete&id=${s.id}"
                   onclick="return confirm('Are you sure?')">
                    Delete
                </a>
            </td>
        </tr>
    </c:forEach>

</table>

</body>
</html>