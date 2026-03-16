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
                <c:out value="${pageTitle}"/> | FreshMart
            </c:when>
            <c:otherwise>FreshMart - Enterprise Grocery Operations</c:otherwise>
        </c:choose>
    </title>

    <!-- Bootstrap 5 -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet"/>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet"/>
    
    <!-- Google Fonts -->
    <link rel="preconnect" href="https://fonts.googleapis.com"/>
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin/>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&family=Outfit:wght@400;600;800&display=swap" rel="stylesheet"/>

    <!-- FM Design System -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/fm-design-system.css?v=1"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/fm-components.css?v=1"/>
    
    <!-- Legacy Support (if needed) -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/app.css?v=4"/>
    
    <style>
        /* Topbar specific overrides for enterprise feel */
        .fm-nav-link {
            font-size: 0.875rem;
            font-weight: 500;
            color: var(--fm-slate-600);
            padding: 0.5rem 1rem;
            border-radius: var(--fm-radius-control);
        }
        .fm-nav-link:hover, .fm-nav-link.active {
            color: var(--fm-primary-600);
            background: var(--fm-primary-50);
        }
        .navbar-brand {
            font-family: 'Outfit', sans-serif;
            font-weight: 800;
            color: var(--fm-primary-600) !important;
        }
    </style>
</head>

<body class="fm-app-shell">
<header class="fm-topbar">
    <div class="container-fluid px-4">
        <div class="d-flex align-items-center justify-content-between">
            <div class="d-flex align-items-center gap-4">
                <a class="navbar-brand fs-4" href="${pageContext.request.contextPath}/">
                    <i class="bi bi-leaf-fill me-2"></i>FreshMart
                </a>

                <nav class="d-none d-lg-flex align-items-center gap-1">
                    <a class="fm-nav-link ${pageContext.request.servletPath == '/' ? 'active' : ''}" 
                       href="${pageContext.request.contextPath}/">Home</a>
                    
                    <a class="fm-nav-link ${pageContext.request.servletPath == '/catalog' ? 'active' : ''}" 
                       href="${pageContext.request.contextPath}/catalog">Catalog</a>

                    <c:if test="${sessionScope.authUser != null && sessionScope.authUser.role.toString() eq 'CUSTOMER'}">
                        <div class="dropdown">
                            <a class="fm-nav-link dropdown-toggle" href="#" data-bs-toggle="dropdown">Customer</a>
                            <ul class="dropdown-menu border-0 shadow-sm">
                                <li><a class="dropdown-item" href="${pageContext.request.contextPath}/customer/dashboard">Dashboard</a></li>
                                <li><a class="dropdown-item" href="${pageContext.request.contextPath}/customer/orders">My Orders</a></li>
                                <li><a class="dropdown-item" href="${pageContext.request.contextPath}/customer/profile">My Profile</a></li>
                            </ul>
                        </div>
                    </c:if>

                    <c:if test="${sessionScope.authUser != null && (sessionScope.authUser.role.toString() eq 'STAFF' || sessionScope.authUser.role.toString() eq 'ADMIN')}">
                        <div class="dropdown">
                            <a class="fm-nav-link dropdown-toggle" href="#" data-bs-toggle="dropdown">Operations</a>
                            <ul class="dropdown-menu border-0 shadow-sm">
                                <li><a class="dropdown-item" href="${pageContext.request.contextPath}/staff">Dashboard</a></li>
                                <li><hr class="dropdown-divider"></li>
                                <li><a class="dropdown-item" href="${pageContext.request.contextPath}/staff/inventory">Real-time Inventory</a></li>
                                <li><a class="dropdown-item" href="${pageContext.request.contextPath}/staff/import-lot">Import Lot (FEFO)</a></li>
                                <li><hr class="dropdown-divider"></li>
                                <li><a class="dropdown-item" href="${pageContext.request.contextPath}/staff/products">Product Master</a></li>
                                <li><a class="dropdown-item" href="${pageContext.request.contextPath}/staff/suppliers">Supplier Management</a></li>
                            </ul>
                        </div>
                    </c:if>
                </nav>
            </div>

            <div class="d-flex align-items-center gap-3">
                <a href="${pageContext.request.contextPath}/cart-view" class="btn btn-link link-dark position-relative p-2">
                    <i class="bi bi-cart3 fs-5"></i>
                    <c:if test="${not empty sessionScope.cart and sessionScope.cart.itemCount > 0}">
                        <span class="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-danger" style="font-size: 0.65rem;">
                            ${sessionScope.cart.itemCount}
                        </span>
                    </c:if>
                </a>

                <c:choose>
                    <c:when test="${sessionScope.authUser != null}">
                        <div class="dropdown">
                            <button class="btn btn-light d-flex align-items-center gap-2 border-0 bg-transparent px-2" data-bs-toggle="dropdown">
                                <div class="bg-primary text-white rounded-circle d-flex align-items-center justify-content-center" style="width: 32px; height: 32px; font-weight: 700; font-size: 0.8rem;">
                                    ${sessionScope.authUser.username.substring(0,1).toUpperCase()}
                                </div>
                                <div class="text-start d-none d-md-block">
                                    <div class="fw-bold lh-1" style="font-size: 0.85rem;"><c:out value="${sessionScope.authUser.username}"/></div>
                                    <div class="text-muted" style="font-size: 0.75rem;"><c:out value="${sessionScope.authUser.role}"/></div>
                                </div>
                                <i class="bi bi-chevron-down ms-1 text-muted" style="font-size: 0.75rem;"></i>
                            </button>
                            <ul class="dropdown-menu dropdown-menu-end border-0 shadow-lg mt-2 py-2" style="min-width: 200px; border-radius: 12px;">
                                <li class="px-3 py-2 border-bottom mb-2">
                                    <div class="fw-bold"><c:out value="${sessionScope.authUser.fullName}"/></div>
                                    <div class="small text-muted"><c:out value="${sessionScope.authUser.email}"/></div>
                                </li>
                                <li><a class="dropdown-item" href="${pageContext.request.contextPath}/profile"><i class="bi bi-person me-2"></i>Account Settings</a></li>
                                <c:if test="${sessionScope.authUser.role.toString() eq 'ADMIN'}">
                                    <li><a class="dropdown-item" href="${pageContext.request.contextPath}/admin"><i class="bi bi-shield-check me-2"></i>Admin Console</a></li>
                                </c:if>
                                <li><hr class="dropdown-divider"></li>
                                <li>
                                    <form action="${pageContext.request.contextPath}/logout" method="post">
                                        <input type="hidden" name="csrf_token" value="${sessionScope.CSRF_TOKEN}"/>
                                        <button type="submit" class="dropdown-item text-danger">
                                            <i class="bi bi-box-arrow-right me-2"></i>Logout
                                        </button>
                                    </form>
                                </li>
                            </ul>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <a href="${pageContext.request.contextPath}/login" class="fm-btn btn-light border p-2 px-3 fs-6" style="background: white; border-radius: 8px;">Login</a>
                        <a href="${pageContext.request.contextPath}/register" class="fm-btn fm-btn-primary p-2 px-3 fs-6">Get Started</a>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </div>
</header>

<main class="fm-page-content flex-grow-1">