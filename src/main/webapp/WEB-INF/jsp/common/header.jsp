<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1"/>
    <title>
        <c:choose>
            <c:when test="${not empty pageTitle}">
                <c:out value="${pageTitle}"/>
            </c:when>
            <c:otherwise>FreshMart</c:otherwise>
        </c:choose>
    </title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet"/>
</head>

<body>
<nav class="navbar navbar-expand-lg navbar-dark bg-dark">
    <div class="container">
        <a class="navbar-brand" href="${pageContext.request.contextPath}/">FreshMart</a>

        <button class="navbar-toggler" type="button"
                data-bs-toggle="collapse" data-bs-target="#nav"
                aria-controls="nav" aria-expanded="false"
                aria-label="Toggle navigation">
            <span class="navbar-toggler-icon"></span>
        </button>

        <div class="collapse navbar-collapse" id="nav">

            <!-- LEFT MENU -->
            <ul class="navbar-nav me-auto">

                <!-- Ai cũng xem được -->
                <li class="nav-item">
                    <a class="nav-link" href="${pageContext.request.contextPath}/catalog">Catalog</a>
                </li>

                <!-- PRO dashboard: chỉ user tier PRO -->
                <c:if test="${sessionScope.authUser != null && sessionScope.authUser.tier != null && sessionScope.authUser.tier.toString() eq 'PRO'}">
                    <li class="nav-item">
                        <a class="nav-link" href="${pageContext.request.contextPath}/pro/dashboard">PRO Dashboard</a>
                    </li>
                </c:if>

                <!-- SELLER POS: chỉ role SELLER -->
                <c:if test="${sessionScope.authUser != null && sessionScope.authUser.role != null && sessionScope.authUser.role.toString() eq 'SELLER'}">
                    <li class="nav-item">
                        <a class="nav-link" href="${pageContext.request.contextPath}/seller/pos">Seller POS</a>
                    </li>
                </c:if>

                <!-- ADMIN MANAGE SELLERS: chỉ role ADMIN -->
                <c:if test="${sessionScope.authUser != null && sessionScope.authUser.role != null && sessionScope.authUser.role.toString() eq 'ADMIN'}">
                    <li class="nav-item">
                        <a class="nav-link" href="${pageContext.request.contextPath}/admin/sellers">Manage Sellers</a>
                    </li>
                </c:if>

            </ul>

            <!-- RIGHT MENU -->
            <ul class="navbar-nav">
                <c:choose>

                    <c:when test="${sessionScope.authUser != null}">
                        <li class="nav-item">
                            <span class="navbar-text me-3">
                                Xin chào,
                                <b><c:out value="${sessionScope.authUser.username}"/></b>
                                (<c:out value="${sessionScope.authUser.role}"/>)
                            </span>
                        </li>

                        <li class="nav-item">
                            <a class="nav-link" href="${pageContext.request.contextPath}/logout">Logout</a>
                        </li>
                    </c:when>

                    <c:otherwise>
                        <li class="nav-item">
                            <a class="nav-link" href="${pageContext.request.contextPath}/login">Login</a>
                        </li>
                    </c:otherwise>

                </c:choose>
            </ul>

        </div>
    </div>
</nav>

<div class="container mt-4">