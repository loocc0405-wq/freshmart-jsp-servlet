<%@ page contentType="text/html;charset=UTF-8" %>

<html>
<head>
    <title>Supplier Form</title>
</head>
<body>

<h2>
    ${supplier == null ? "Add New Supplier" : "Edit Supplier"}
</h2>

<form action="${pageContext.request.contextPath}/staff/suppliers" method="post">

    <input type="hidden" name="id" value="${supplier.id}" />

    <label>Name:</label><br>
    <input type="text" name="name" value="${supplier.name}" required />
    <br><br>

    <label>Phone:</label><br>
    <input type="text" name="phone" value="${supplier.phone}" required />
    <br><br>

    <label>Email:</label><br>
    <input type="email" name="email" value="${supplier.email}" required />
    <br><br>

    <label>Address:</label><br>
    <input type="text" name="address" value="${supplier.address}" required />
    <br><br>

    <button type="submit">Save</button>

</form>

<br>
<a href="${pageContext.request.contextPath}/staff/suppliers">
    Back to List
</a>

</body>
</html>