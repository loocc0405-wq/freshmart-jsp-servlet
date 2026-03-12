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

    <link rel="preconnect" href="https://fonts.googleapis.com"/>
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin/>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet"/>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet"/>

    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/app.css?v=3"/>
</head>

<body class="app-body">
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

            <ul class="navbar-nav me-auto">

                <li class="nav-item">
                    <a class="nav-link ${pageContext.request.servletPath == '/' || pageContext.request.servletPath == '/home' ? 'active' : ''}"
                       href="${pageContext.request.contextPath}/">
                        Home
                    </a>
                </li>

                <li class="nav-item">
                    <a class="nav-link ${pageContext.request.servletPath == '/catalog' || pageContext.request.servletPath == '/product' ? 'active' : ''}"
                       href="${pageContext.request.contextPath}/catalog">
                        Catalog
                    </a>
                </li>

                <c:if test="${sessionScope.authUser != null}">
                    <li class="nav-item">
                        <a class="nav-link ${pageContext.request.servletPath == '/cart-view' ? 'active' : ''}"
                           href="${pageContext.request.contextPath}/cart-view">
                            Cart
                        </a>
                    </li>
                </c:if>

                <c:if test="${sessionScope.authUser != null && sessionScope.authUser.role.toString() eq 'CUSTOMER'}">
                    <li class="nav-item dropdown">
                        <a class="nav-link dropdown-toggle ${pageContext.request.servletPath.startsWith('/customer') ? 'active' : ''}"
                           href="#" role="button" data-bs-toggle="dropdown">
                            Customer
                        </a>
                        <ul class="dropdown-menu">
                            <li><a class="dropdown-item" href="${pageContext.request.contextPath}/customer/dashboard">Dashboard</a></li>
                            <li><a class="dropdown-item" href="${pageContext.request.contextPath}/customer/orders">My Orders</a></li>
                            <li><a class="dropdown-item" href="${pageContext.request.contextPath}/customer/profile">My Profile</a></li>
                        </ul>
                    </li>
                </c:if>

                <c:if test="${sessionScope.authUser != null 
                             && (sessionScope.authUser.role.toString() eq 'CUSTOMER' 
                                 || sessionScope.authUser.role.toString() eq 'ADMIN')}">
                    <li class="nav-item">
                        <a class="nav-link ${pageContext.request.servletPath.startsWith('/subscription') ? 'active' : ''}"
                           href="${pageContext.request.contextPath}/subscription/upgrade">
                            Upgrade
                        </a>
                    </li>
                </c:if>

                <c:if test="${sessionScope.authUser != null 
                             && (sessionScope.authUser.tier.toString() eq 'PRO'
                                 || sessionScope.authUser.role.toString() eq 'STAFF'
                                 || sessionScope.authUser.role.toString() eq 'ADMIN')}">
                    <li class="nav-item dropdown">
                        <a class="nav-link dropdown-toggle ${pageContext.request.servletPath.startsWith('/pro') ? 'active' : ''}"
                           href="#" role="button" data-bs-toggle="dropdown">
                            PRO
                        </a>
                        <ul class="dropdown-menu">
                            <li><a class="dropdown-item" href="${pageContext.request.contextPath}/pro/dashboard">Dashboard</a></li>
                            <li><a class="dropdown-item" href="${pageContext.request.contextPath}/pro/seasonality">Seasonality</a></li>
                            <li><a class="dropdown-item" href="${pageContext.request.contextPath}/pro/replenishment">Replenishment</a></li>
                        </ul>
                    </li>
                </c:if>

                <c:if test="${sessionScope.authUser != null 
                             && (sessionScope.authUser.role.toString() eq 'SELLER'
                                 || sessionScope.authUser.role.toString() eq 'ADMIN')}">
                    <li class="nav-item dropdown">
                        <a class="nav-link dropdown-toggle ${pageContext.request.servletPath.startsWith('/seller') ? 'active' : ''}"
                           href="#" role="button" data-bs-toggle="dropdown">
                            Seller
                        </a>
                        <ul class="dropdown-menu">
                            <li><a class="dropdown-item" href="${pageContext.request.contextPath}/seller/pos">POS</a></li>
                        </ul>
                    </li>
                </c:if>

                <c:if test="${sessionScope.authUser != null 
                             && (sessionScope.authUser.role.toString() eq 'STAFF'
                                 || sessionScope.authUser.role.toString() eq 'ADMIN')}">
                    <li class="nav-item dropdown">
                        <a class="nav-link dropdown-toggle ${pageContext.request.servletPath.startsWith('/staff') ? 'active' : ''}"
                           href="#" role="button" data-bs-toggle="dropdown">
                            Staff
                        </a>
                        <ul class="dropdown-menu">
                            <li><a class="dropdown-item" href="${pageContext.request.contextPath}/staff">Staff Home</a></li>
                            <li><hr class="dropdown-divider"/></li>
                            <li><a class="dropdown-item" href="${pageContext.request.contextPath}/staff/suppliers">Suppliers</a></li>
                            <li><a class="dropdown-item" href="${pageContext.request.contextPath}/staff/products">Products</a></li>
                            <li><hr class="dropdown-divider"/></li>
                            <li><a class="dropdown-item" href="${pageContext.request.contextPath}/staff/inventory">Inventory (FEFO)</a></li>
                            <li><a class="dropdown-item" href="${pageContext.request.contextPath}/staff/import-lot">Import lot</a></li>
                            <li><a class="dropdown-item" href="${pageContext.request.contextPath}/staff/inventory-report">Inventory report</a></li>
                            <li><a class="dropdown-item" href="${pageContext.request.contextPath}/staff/forecast">Forecast</a></li>
                        </ul>
                    </li>
                </c:if>

                <c:if test="${sessionScope.authUser != null && sessionScope.authUser.role.toString() eq 'ADMIN'}">
                    <li class="nav-item dropdown">
                        <a class="nav-link dropdown-toggle ${pageContext.request.servletPath.startsWith('/admin') ? 'active' : ''}"
                           href="#" role="button" data-bs-toggle="dropdown">
                            Admin
                        </a>
                        <ul class="dropdown-menu">
                            <li><a class="dropdown-item" href="${pageContext.request.contextPath}/admin">Dashboard</a></li>
                            <li><a class="dropdown-item" href="${pageContext.request.contextPath}/admin/sellers">Manage sellers</a></li>
                            <li><a class="dropdown-item" href="${pageContext.request.contextPath}/admin/products">Manage products</a></li>
                            <li><a class="dropdown-item" href="${pageContext.request.contextPath}/admin/add-product">Add product</a></li>
                            <li><hr class="dropdown-divider"/></li>
                            <li><a class="dropdown-item" href="${pageContext.request.contextPath}/admin/subscriptions">Subscriptions</a></li>
                        </ul>
                    </li>
                </c:if>

            </ul>

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
                            <form action="${pageContext.request.contextPath}/logout" method="post" class="d-inline">
                                <input type="hidden" name="csrf_token" value="${sessionScope.CSRF_TOKEN}"/>
                                <button type="submit" class="nav-link btn btn-link p-0" style="text-decoration:none;">
                                    Logout
                                </button>
                            </form>
                        </li>
                    </c:when>
                    <c:otherwise>
                        <li class="nav-item">
                            <a class="nav-link" href="${pageContext.request.contextPath}/login">Login</a>
                        </li>
                        <li class="nav-item">
                            <a class="nav-link" href="${pageContext.request.contextPath}/register">Register</a>
                        </li>
                    </c:otherwise>
                </c:choose>
            </ul>

        </div>
    </div>
</nav>

<main class="container py-4 fm-page">