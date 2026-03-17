<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1"/>
    <title>Login | FreshMart Operations</title>
    
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
        body { 
            background: var(--fm-surface); 
            min-height: 100vh;
            display: flex;
            flex-direction: column;
        }
        .branding-logo {
            font-family: 'Outfit', sans-serif;
            font-weight: 800;
            color: var(--fm-primary-600);
            font-size: 1.75rem;
            text-decoration: none;
            display: flex;
            align-items: center;
            gap: 0.625rem;
            letter-spacing: -0.02em;
        }
        .auth-card-container {
            width: 100%;
            max-width: 420px;
            animation: fadeIn 0.5s ease-out;
        }
        @keyframes fadeIn {
            from { opacity: 0; transform: translateY(10px); }
            to { opacity: 1; transform: translateY(0); }
        }
        .fm-auth-footer {
            margin-top: 3rem;
            padding-top: 1.5rem;
            border-top: 1px solid var(--fm-slate-100);
            text-align: center;
            font-size: 0.875rem;
            color: var(--fm-text-muted);
        }
    </style>
</head>
<body>

<div class="fm-split-panel flex-grow-1">
    <!-- Entry Section -->
    <div class="fm-split-side-content">
        <div class="auth-card-container">
            <div class="mb-5">
                <a href="${pageContext.request.contextPath}/" class="branding-logo mb-4">
                    <i class="bi bi-leaf-fill"></i> FreshMart
                </a>
                <h1 class="fm-h1 mb-2">Operations Console</h1>
                <p class="fm-text-secondary">Please sign in with your internal credentials to resume work.</p>
            </div>

            <!-- Feedback States -->
            <c:if test="${param.registered eq '1'}">
                <div class="alert alert-success d-flex align-items-center border-0 shadow-sm mb-4 bg-success-subtle">
                    <i class="bi bi-check-circle-fill me-3 fs-5 text-success"></i>
                    <div class="small fw-semibold">Registration successful. You can now log in.</div>
                </div>
            </c:if>

            <c:if test="${not empty error}">
                <div class="alert alert-danger d-flex align-items-center border-0 shadow-sm mb-4 bg-danger-subtle">
                    <i class="bi bi-exclamation-triangle-fill me-3 fs-5 text-danger"></i>
                    <div class="small fw-semibold"><c:out value="${error}"/></div>
                </div>
            </c:if>

            <!-- Login Form -->
            <form method="post" action="${pageContext.request.contextPath}/login" class="fm-form">
                <input type="hidden" name="csrf_token" value="${sessionScope.CSRF_TOKEN}" />
                <input type="hidden" name="return" value="${returnUrl}" />

                <div class="mb-3">
                    <label class="fm-label mb-2">Username or Email</label>
                    <div class="position-relative">
                        <i class="bi bi-envelope position-absolute top-50 start-0 translate-middle-y ms-3 text-muted opacity-50"></i>
                        <input type="text" name="login" class="fm-form-control ps-5" 
                               value="${fn:escapeXml(loginValue)}"
                               placeholder="admin@freshmart.com" required autofocus>
                    </div>
                </div>

                <div class="mb-4">
                    <div class="d-flex justify-content-between align-items-center mb-2">
                        <label class="fm-label mb-0">Password</label>
                        <a href="#" class="small fw-bold text-primary text-decoration-none hvr-underline">Forgot password?</a>
                    </div>
                    <div class="position-relative">
                        <i class="bi bi-shield-lock position-absolute top-50 start-0 translate-middle-y ms-3 text-muted opacity-50"></i>
                        <input type="password" id="password" name="password" class="fm-form-control ps-5" 
                               placeholder="••••••••" required>
                        <button type="button" class="btn btn-link position-absolute top-50 end-0 translate-middle-y me-2 p-1 text-muted text-decoration-none shadow-none"
                                data-fm-toggle="password" data-fm-target="password">
                            <i class="bi bi-eye"></i>
                        </button>
                    </div>
                </div>

                <div class="mb-4 form-check d-flex align-items-center gap-2">
                    <input type="checkbox" class="form-check-input mt-0" id="rememberMe">
                    <label class="form-check-label small fw-medium text-muted" for="rememberMe">Remember my session</label>
                </div>

                <button type="submit" class="fm-btn fm-btn-primary w-100 py-3 shadow-lg border-0">
                    Sign In to Workspace <i class="bi bi-arrow-right-short ms-2 fs-5"></i>
                </button>
            </form>

            <div class="fm-auth-footer">
                New staff member? 
                <a href="${pageContext.request.contextPath}/register" class="fw-bold text-primary text-decoration-none">Join the system</a>
                <div class="mt-4 opacity-40 small">&copy; 2026 FreshMart Enterprise Edition. All rights reserved.</div>
            </div>
        </div>
    </div>

    <!-- Media Section (Hero) -->
    <div class="fm-split-side-hero d-none d-lg-block">
        <img src="${pageContext.request.contextPath}/assets/images/heroes/auth-hero-8k.png" 
             alt="FreshMart Operations Hero" class="fm-hero-media">
        <div class="fm-media-overlay">
            <div class="mb-5" style="max-width: 560px;">
                <span class="badge bg-primary-subtle text-primary border border-primary-subtle px-3 py-2 mb-4 rounded-pill fw-bold text-uppercase ls-wide" style="font-size: 0.65rem;">
                    Internal Operations Console
                </span>
                <h2 class="display-4 fw-extrabold mb-4 text-white text-shadow-sm">Optimizing the Fresh Food Supply Chain</h2>
                <p class="lead opacity-90 text-white mb-5" style="font-weight: 500; font-size: 1.15rem; line-height: 1.6;">
                    Intelligent management platform built for the FreshMart team, integrating AI demand forecasting and global FEFO standard inventory control.
                </p>
                
                <div class="row g-4 pt-2">
                    <div class="col-6">
                        <div class="d-flex align-items-center gap-3">
                            <div class="bg-white bg-opacity-10 rounded-circle p-2 border border-white border-opacity-10">
                                <i class="bi bi-lightning-charge text-primary"></i>
                            </div>
                            <span class="small fw-semibold text-white">Real-time Processing</span>
                        </div>
                    </div>
                    <div class="col-6">
                        <div class="d-flex align-items-center gap-3">
                            <div class="bg-white bg-opacity-10 rounded-circle p-2 border border-white border-opacity-10">
                                <i class="bi bi-shield-check text-primary"></i>
                            </div>
                            <span class="small fw-semibold text-white">Multi-tier Security</span>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- Scripts -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/fm-core.js?v=2"></script>

</body>
</html>