<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html>
<head>
<title>FreshMart Login</title>

<link rel="stylesheet"
href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">

<style>

*{
box-sizing:border-box;
margin:0;
padding:0;
font-family:Arial, Helvetica, sans-serif;
}

body{
height:100vh;
background:linear-gradient(135deg,#00c853,#00bfa5,#009688);
display:flex;
justify-content:center;
align-items:center;
}

.container{
display:flex;
justify-content:center;
align-items:center;
width:100%;
}

.card{

background:white;
padding:40px 35px;
border-radius:16px;
width:360px;

box-shadow:
0 15px 40px rgba(0,0,0,0.25);

animation:fadeIn 0.6s ease;

}

@keyframes fadeIn{
from{
opacity:0;
transform:translateY(20px);
}
to{
opacity:1;
transform:translateY(0);
}
}

.logo{
text-align:center;
margin-bottom:10px;
font-size:26px;
font-weight:bold;
color:#009688;
}

.subtitle{
text-align:center;
color:#777;
margin-bottom:25px;
font-size:14px;
}

.input-group{
position:relative;
margin-bottom:18px;
}

.input-group i{
position:absolute;
top:50%;
left:12px;
transform:translateY(-50%);
color:#777;
}

input{
width:100%;
padding:12px 12px 12px 38px;
border-radius:8px;
border:1px solid #ccc;
font-size:14px;
transition:0.2s;
}

input:focus{
outline:none;
border-color:#00c853;
box-shadow:0 0 0 2px rgba(0,200,83,0.15);
}

button{
width:100%;
padding:12px;
border:none;
border-radius:8px;
background:linear-gradient(90deg,#00c853,#00bfa5);
color:white;
font-weight:bold;
font-size:15px;
cursor:pointer;
transition:0.2s;
}

button:hover{
opacity:0.9;
transform:translateY(-1px);
}

.error{
background:#ffe5e5;
color:#d50000;
padding:10px;
border-radius:6px;
margin-bottom:15px;
text-align:center;
font-size:14px;
}

.footer{
text-align:center;
margin-top:18px;
font-size:13px;
color:#888;
}

.footer span{
color:#009688;
font-weight:bold;
}

</style>

</head>

<body>

<div class="container">

<div class="card">

<div class="logo">
<i class="fa-solid fa-leaf"></i> FreshMart
</div>

<div class="subtitle">
Inventory & POS System
</div>

<c:if test="${not empty error}">
<div class="error">${error}</div>
</c:if>

<form method="post" action="login">

<input type="hidden" name="csrf_token" value="${sessionScope.CSRF_TOKEN}" />

<div class="input-group">
<i class="fa fa-user"></i>
<input type="text" name="username" placeholder="Username" required>
</div>

<div class="input-group">
<i class="fa fa-lock"></i>
<input type="password" name="password" placeholder="Password" required>
</div>

<button type="submit">
<i class="fa fa-right-to-bracket"></i> Login
</button>

</form>

<div class="footer">
FreshMart © 2026
</div>

</div>

</div>

</body>
</html>