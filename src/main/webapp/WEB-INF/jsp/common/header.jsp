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

    <!-- Typography + Icons -->
    <link rel="preconnect" href="https://fonts.googleapis.com"/>
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin/>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet"/>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet"/>

    <!-- App theme -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/app.css"/>
</head>

<body class="app-body">
<nav class="navbar navbar-expand-lg navbar-dark fm-navbar sticky-top">
    <div class="container">
        <a class="navbar-brand" href="${pageContext.request.contextPath}/">
            <i class="bi bi-basket2-fill me-2 fm-icon"></i>FreshMart
        </a>

        <button class="navbar-toggler" type="button"
                data-bs-toggle="collapse" data-bs-target="#nav"
                aria-controls="nav" aria-expanded="false"
                aria-label="Toggle navigation">
            <span class="navbar-toggler-icon"></span>
        </button>

        <div class="collapse navbar-collapse" id="nav">

            <!-- ✅ Lấy user từ session (tên phổ biến nhất là authUser) -->
            <c:set var="auth" value="${sessionScope.authUser}" />

            <!-- LEFT MENU -->
            <ul class="navbar-nav me-auto">

                <!-- Ai cũng xem được -->
                <li class="nav-item">
                    <a class="nav-link" href="${pageContext.request.contextPath}/catalog">Catalog</a>
                </li>

                <!-- ✅ PRO: chỉ user tier PRO -->
                <c:if test="${auth != null && auth.tier != null && auth.tier.toString() eq 'PRO'}">
                    <li class="nav-item">
                        <a class="nav-link" href="${pageContext.request.contextPath}/pro/dashboard">PRO Dashboard</a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" href="${pageContext.request.contextPath}/pro/seasonality">PRO Seasonality</a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" href="${pageContext.request.contextPath}/pro/replenishment">PRO Restock</a>
                    </li>
                </c:if>

                <!-- ✅ SELLER POS: SELLER hoặc ADMIN (admin có thể test) -->
                <c:if test="${auth != null && auth.role != null && (auth.role.toString() eq 'SELLER' || auth.role.toString() eq 'ADMIN')}">
                    <li class="nav-item">
                        <a class="nav-link" href="${pageContext.request.contextPath}/seller/pos">Seller POS</a>
                    </li>
                </c:if>

                <!-- ✅ STAFF -->
                <c:if test="${auth != null && auth.role != null && (auth.role.toString() eq 'STAFF' || auth.role.toString() eq 'ADMIN')}">
                    <li class="nav-item">
                        <a class="nav-link" href="${pageContext.request.contextPath}/staff/forecast">Staff Forecast</a>
                    </li>
                </c:if>

                <!-- ✅ ADMIN -->
                <c:if test="${auth != null && auth.role != null && auth.role.toString() eq 'ADMIN'}">
                    <li class="nav-item">
                        <a class="nav-link" href="${pageContext.request.contextPath}/admin/sellers">Manage Sellers</a>
                    </li>
                </c:if>

            </ul>

            <!-- RIGHT MENU -->
            <ul class="navbar-nav">
                <c:choose>

                    <c:when test="${auth != null}">

                        <li class="nav-item">
                            <a class="nav-link" href="${pageContext.request.contextPath}/cart-view" title="Giỏ hàng">
                                <i class="bi bi-cart3"></i>
                            </a>
                        </li>

                        <li class="nav-item">
                            <span class="navbar-text me-3">
                                Xin chào,
                                <b><c:out value="${auth.username}"/></b>
                                (<c:out value="${auth.role}"/>)
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

<main class="container py-4 fm-page">