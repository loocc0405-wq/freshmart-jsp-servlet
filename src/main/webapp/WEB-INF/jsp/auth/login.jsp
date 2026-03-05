<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html>
<head>
<title>FreshMart Login</title>

<style>
body{
font-family:Arial;
background:linear-gradient(135deg,#00c853,#009688);
display:flex;
justify-content:center;
align-items:center;
height:100vh;
}

.card{
background:white;
padding:40px;
border-radius:15px;
width:350px;
box-shadow:0 15px 30px rgba(0,0,0,0.2);
}

h2{
text-align:center;
}

input{
width:100%;
padding:12px;
margin:10px 0;
border-radius:8px;
border:1px solid #ccc;
}

button{
width:100%;
padding:12px;
border:none;
border-radius:8px;
background:#00c853;
color:white;
font-weight:bold;
cursor:pointer;
}

button:hover{
background:#009688;
}

.error{
color:red;
text-align:center;
}
</style>

</head>

<body>

<div class="card">

<h2>FreshMart Login</h2>

<c:if test="${not empty error}">
<div class="error">${error}</div>
</c:if>

<form method="post" action="login">

<input type="hidden" name="csrf_token" value="${sessionScope.CSRF_TOKEN}" />

<input type="text" name="username" placeholder="Username" required>

<input type="password" name="password" placeholder="Password" required>

<button type="submit">Login</button>

</form>

</div>

</body>
</html>