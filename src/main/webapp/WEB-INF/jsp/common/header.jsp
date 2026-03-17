<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1"/>
    <title>
        <c:choose>
            <c:when test="${not empty pageTitle}">
                <c:out value="${pageTitle}"/> | FreshMart Operations
            </c:when>
            <c:otherwise>FreshMart - Enterprise Retail Platform</c:otherwise>
        </c:choose>
    </title>

    <!-- Core Assets -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet"/>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet"/>
    <link rel="preconnect" href="https://fonts.googleapis.com"/>
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin/>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&family=Outfit:wght@600;800&display=swap" rel="stylesheet"/>

    <!-- Design System -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/fm-design-system.css?v=2"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/fm-components.css?v=2"/>
    
    <style>
        .fm-nav-link {
            font-size: 0.875rem;
            font-weight: 600;
            color: var(--fm-slate-600);
            padding: 0.5rem 0.875rem;
            border-radius: var(--fm-radius-sm);
            display: flex;
            align-items: center;
            gap: 0.5rem;
            transition: all 0.2s ease;
        }
        .fm-nav-link:hover, .fm-nav-link.active {
            color: var(--fm-primary-700);
            background: var(--fm-primary-50);
        }
        .fm-navbar-brand {
            font-family: 'Outfit', sans-serif;
            font-weight: 800;
            color: var(--fm-primary-600) !important;
            letter-spacing: -0.02em;
        }
        .fm-badge-pill {
            font-size: 0.65rem;
            padding: 0.25rem 0.4rem;
            font-weight: 700;
        }
        .fm-user-avatar {
            width: 34px;
            height: 34px;
            background: var(--fm-primary-100);
            color: var(--fm-primary-700);
            font-weight: 800;
            font-size: 0.85rem;
            border: 2px solid white;
            box-shadow: 0 0 0 1px var(--fm-primary-200);
        }
    </style>
</head>

<body class="fm-app-shell">

<header class="fm-topbar">
    <div class="container-xl">
        <div class="d-flex align-items-center justify-content-between">
            <!-- Left: Brand & Main Nav -->
            <div class="d-flex align-items-center gap-4">
                <a class="fm-navbar-brand fs-4 d-flex align-items-center" href="${pageContext.request.contextPath}/">
                    <i class="bi bi-leaf-fill me-2"></i>FreshMart
                </a>

                <nav class="d-none d-lg-flex align-items-center gap-1">
                    <a class="fm-nav-link ${pageContext.request.servletPath == '/' ? 'active' : ''}" 
                       href="${pageContext.request.contextPath}/">
                        <i class="bi bi-house"></i> Home
                    </a>
                    
                    <a class="fm-nav-link ${pageContext.request.servletPath == '/catalog' ? 'active' : ''}" 
                       href="${pageContext.request.contextPath}/catalog">
                        <i class="bi bi-grid"></i> Catalog
                    </a>

                    <c:if test="${sessionScope.authUser != null}">
                        <c:set var="dashUrl" value="${sessionScope.authUser.role == 'CUSTOMER' ? '/customer/dashboard' : '/staff'}"/>
                        <a class="fm-nav-link ${fn:startsWith(pageContext.request.servletPath, dashUrl) ? 'active' : ''}" 
                           href="${pageContext.request.contextPath}${dashUrl}">
                            <i class="bi bi-speedometer2"></i> Dashboard
                        </a>
                    </c:if>

                    <!-- Role Based View: Customer -->
                    <c:if test="${sessionScope.authUser != null && sessionScope.authUser.role == 'CUSTOMER'}">
                        <div class="dropdown">
                            <a class="fm-nav-link dropdown-toggle" href="#" data-bs-toggle="dropdown">
                                <i class="bi bi-person-badge"></i> My Account
                            </a>
                            <ul class="dropdown-menu border-0 shadow-lg mt-2 py-2" style="border-radius: 12px;">
                                <li><a class="dropdown-item py-2" href="${pageContext.request.contextPath}/customer/dashboard"><i class="bi bi-speedometer2 me-2"></i>Dashboard</a></li>
                                <li><a class="dropdown-item py-2" href="${pageContext.request.contextPath}/customer/orders"><i class="bi bi-bag me-2"></i>My Orders</a></li>
                                <li><a class="dropdown-item py-2" href="${pageContext.request.contextPath}/customer/profile"><i class="bi bi-person me-2"></i>Profile Settings</a></li>
                            </ul>
                        </div>
                    </c:if>

                    <!-- Role Based View: Operations (Staff/Admin) -->
                    <c:if test="${sessionScope.authUser != null && (sessionScope.authUser.role == 'STAFF' || sessionScope.authUser.role == 'ADMIN')}">
                        <div class="dropdown">
                            <a class="fm-nav-link dropdown-toggle active" href="#" data-bs-toggle="dropdown">
                                <i class="bi bi-briefcase"></i> Operations
                            </a>
                            <ul class="dropdown-menu border-0 shadow-lg mt-2 py-2" style="min-width: 220px; border-radius: 12px;">
                                <li><a class="dropdown-item py-2" href="${pageContext.request.contextPath}/staff/inventory"><i class="bi bi-boxes me-2 text-primary"></i>Inventory Hub</a></li>
                                <li><a class="dropdown-item py-2" href="${pageContext.request.contextPath}/staff/inventory-report"><i class="bi bi-file-earmark-bar-graph me-2 text-primary"></i>Inventory Report</a></li>
                                <li><a class="dropdown-item py-2" href="${pageContext.request.contextPath}/staff/forecast"><i class="bi bi-lightning-charge me-2 text-primary"></i>Demand Forecasting</a></li>
                                <li><hr class="dropdown-divider opacity-50"></li>
                                <li class="dropdown-header text-uppercase small fw-bold opacity-50 px-3">Logistics & Sales</li>
                                <li><a class="dropdown-item py-2" href="${pageContext.request.contextPath}/staff/import-lot"><i class="bi bi-truck me-2"></i>FEFO Import</a></li>
                                <li><a class="dropdown-item py-2" href="${pageContext.request.contextPath}/staff/orders"><i class="bi bi-cart-check me-2"></i>OMS Orders</a></li>
                                <li><a class="dropdown-item py-2" href="${pageContext.request.contextPath}/seller/pos"><i class="bi bi-shop me-2"></i>Smart POS</a></li>
                                <li><hr class="dropdown-divider opacity-50"></li>
                                <li class="dropdown-header text-uppercase small fw-bold opacity-50 px-3">Advanced Insights (Pro)</li>
                                <li><a class="dropdown-item py-2" href="${pageContext.request.contextPath}/pro/replenishment"><i class="bi bi-arrow-repeat me-2"></i>Auto-Replenish</a></li>
                                <li><a class="dropdown-item py-2" href="${pageContext.request.contextPath}/pro/seasonality"><i class="bi bi-calendar-range me-2"></i>Seasonality Audit</a></li>
                                <li><hr class="dropdown-divider opacity-50"></li>
                                <li class="dropdown-header text-uppercase small fw-bold opacity-50 px-3">Master Data</li>
                                <li><a class="dropdown-item py-2" href="${pageContext.request.contextPath}/staff/products"><i class="bi bi-tag me-2"></i>Products</a></li>
                                <li><a class="dropdown-item py-2" href="${pageContext.request.contextPath}/staff/suppliers"><i class="bi bi-building me-2"></i>Suppliers</a></li>
                            </ul>
                        </div>
                    </c:if>
                </nav>
            </div>

            <!-- Right: Actions & User -->
            <div class="d-flex align-items-center gap-2">
                <!-- Cart Button -->
                <a href="${pageContext.request.contextPath}/cart-view" class="fm-nav-link text-dark position-relative p-2">
                    <i class="bi bi-cart3 fs-5"></i>
                    <c:if test="${not empty sessionScope.cart and sessionScope.cart.itemCount > 0}">
                        <span class="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-danger fm-badge-pill">
                            ${sessionScope.cart.itemCount}
                        </span>
                    </c:if>
                </a>

                <div class="vr mx-2 opacity-10 d-none d-md-block" style="height: 24px;"></div>

                <c:choose>
                    <c:when test="${sessionScope.authUser != null}">
                        <div class="dropdown">
                            <button class="btn btn-link d-flex align-items-center gap-2 border-0 bg-transparent p-1 text-decoration-none" data-bs-toggle="dropdown">
                                <div class="fm-user-avatar rounded-circle d-flex align-items-center justify-content-center">
                                    ${fn:toUpperCase(fn:substring(sessionScope.authUser.username, 0, 1))}
                                </div>
                                <div class="text-start d-none d-md-block">
                                    <div class="fw-bold text-dark lh-1" style="font-size: 0.85rem;"><c:out value="${sessionScope.authUser.username}"/></div>
                                    <div class="text-muted small mt-1" style="font-size: 0.7rem; text-transform: uppercase; letter-spacing: 0.05em;"><c:out value="${sessionScope.authUser.role}"/></div>
                                </div>
                                <i class="bi bi-chevron-down ms-1 text-muted small"></i>
                            </button>
                            <ul class="dropdown-menu dropdown-menu-end border-0 shadow-lg mt-3 py-2" style="min-width: 240px; border-radius: 16px;">
                                <li class="px-3 py-3 border-bottom mb-2 bg-light-subtle rounded-top">
                                    <div class="fw-bold text-dark"><c:out value="${sessionScope.authUser.fullName}"/></div>
                                    <div class="small text-muted text-truncate"><c:out value="${sessionScope.authUser.email}"/></div>
                                </li>
                                <li><a class="dropdown-item py-2" href="${pageContext.request.contextPath}/customer/profile"><i class="bi bi-gear me-2 text-muted"></i>Account Settings</a></li>
                                
                                <c:if test="${sessionScope.authUser.role == 'ADMIN'}">
                                    <li><a class="dropdown-item py-2" href="${pageContext.request.contextPath}/admin"><i class="bi bi-shield-lock me-2 text-primary"></i>Admin Console</a></li>
                                </c:if>
                                
                                <li><hr class="dropdown-divider opacity-50"></li>
                                <li>
                                    <form action="${pageContext.request.contextPath}/logout" method="post" class="px-2">
                                        <input type="hidden" name="csrf_token" value="${sessionScope.CSRF_TOKEN}"/>
                                        <button type="submit" class="dropdown-item py-2 text-danger rounded" style="border: none; background: none; width: 100%; text-align: left;">
                                            <i class="bi bi-box-arrow-right me-2"></i>Sign Out
                                        </button>
                                    </form>
                                </li>
                            </ul>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="d-flex align-items-center gap-2">
                            <a href="${pageContext.request.contextPath}/login" class="fm-nav-link">Login</a>
                            <a href="${pageContext.request.contextPath}/register" class="fm-btn fm-btn-primary py-2 shadow-sm">Get Started</a>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </div>
</header>

<main class="fm-app-content flex-grow-1">