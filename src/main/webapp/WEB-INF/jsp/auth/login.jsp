<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1"/>
    <title>Login | FreshMart Enterprise</title>
    
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet"/>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet"/>
    <link rel="preconnect" href="https://fonts.googleapis.com"/>
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin/>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&family=Outfit:wght@600;800&display=swap" rel="stylesheet"/>

    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/fm-design-system.css?v=1"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/fm-components.css?v=1"/>

    <style>
        body { background: var(--fm-surface); }
        .branding-logo {
            font-family: 'Outfit', sans-serif;
            font-weight: 800;
            color: var(--fm-primary-600);
            font-size: 1.5rem;
            text-decoration: none;
            display: flex;
            align-items: center;
            gap: 0.5rem;
        }
        .fm-auth-footer {
            margin-top: 2rem;
            padding-top: 1.5rem;
            border-top: 1px solid var(--fm-slate-100);
            text-align: center;
            font-size: 0.875rem;
            color: var(--fm-text-muted);
        }
    </style>
</head>
<body>

<div class="fm-split-panel">
    <!-- Left Side: Login Form -->
    <div class="fm-split-side-content">
        <div style="width: 100%; max-width: 400px;">
            <div class="mb-5">
                <a href="${pageContext.request.contextPath}/" class="branding-logo mb-4">
                    <i class="bi bi-leaf-fill"></i> FreshMart
                </a>
                <h1 class="fm-h1 mb-2">Welcome Back</h1>
                <p class="fm-text-secondary">Enter your credentials to access the operations dashboard.</p>
            </div>

            <c:if test="${param.registered eq '1'}">
                <div class="alert alert-success d-flex align-items-center border-0 bg-success-subtle mb-4">
                    <i class="bi bi-check-circle-fill me-2"></i>
                    Registration successful. You can log in now.
                </div>
            </c:if>

            <c:if test="${not empty error}">
                <div class="alert alert-danger d-flex align-items-center border-0 bg-danger-subtle mb-4">
                    <i class="bi bi-exclamation-triangle-fill me-2"></i>
                    <c:out value="${error}"/>
                </div>
            </c:if>

            <form method="post" action="${pageContext.request.contextPath}/login" class="fm-form">
                <input type="hidden" name="csrf_token" value="${sessionScope.CSRF_TOKEN}" />
                <input type="hidden" name="return" value="${returnUrl}" />

                <div class="mb-3">
                    <label class="form-label fw-bold small text-uppercase opacity-75">Username or Email</label>
                    <div class="position-relative">
                        <i class="bi bi-person position-absolute top-50 start-0 translate-middle-y ms-3 text-muted"></i>
                        <input type="text" name="login" class="fm-form-control ps-5" 
                               value="${fn:escapeXml(loginValue)}"
                               placeholder="e.g. administrator" required autofocus>
                    </div>
                </div>

                <div class="mb-4">
                    <div class="d-flex justify-content-between">
                        <label class="form-label fw-bold small text-uppercase opacity-75">Password</label>
                        <a href="#" class="small fw-semibold text-primary">Forgot?</a>
                    </div>
                    <div class="position-relative">
                        <i class="bi bi-lock position-absolute top-50 start-0 translate-middle-y ms-3 text-muted"></i>
                        <input type="password" id="password" name="password" class="fm-form-control ps-5" 
                               placeholder="••••••••" required>
                        <button type="button" class="btn btn-link position-absolute top-50 end-0 translate-middle-y me-2 p-1 text-muted"
                                data-fm-toggle="password" data-fm-target="password">
                            <i class="bi bi-eye"></i>
                        </button>
                    </div>
                </div>

                <button type="submit" class="fm-btn fm-btn-primary w-100 shadow-sm border-0">
                    Sign In to Dashboard <i class="bi bi-arrow-right ms-2"></i>
                </button>
            </form>

            <div class="fm-auth-footer">
                New to the platform? 
                <a href="${pageContext.request.contextPath}/register" class="fw-bold text-primary">Create account</a>
                <div class="mt-3 opacity-50">&copy; 2026 FreshMart. Enterprise Version v1.0</div>
            </div>
        </div>
    </div>

    <!-- Right Side: Hero Visual -->
    <div class="fm-split-side-hero d-none d-lg-block">
        <!-- FM_IMAGE_PROMPT:
        Ultra-realistic 8K fresh grocery enterprise visual, premium FreshMart brand atmosphere, modern clean retail produce arrangement, subtle warehouse operations in background, soft natural daylight, green and slate color harmony, high detail, realistic textures, cinematic but restrained, enterprise-grade, no text, no watermark, wide hero composition
        -->
        <img src="https://images.unsplash.com/photo-1542838132-92c5332c4915?q=80&w=2690&auto=format&fit=crop" 
             alt="FreshMart Operations" class="fm-hero-media">
        <div class="fm-media-overlay">
            <div class="mb-4">
                <span class="badge bg-primary px-3 py-2 mb-3">Enterprise Ready</span>
                <h2 class="display-5 fw-bold mb-3">Smart Inventory Management</h2>
                <p class="lead opacity-75">Harness the power of real-time data to optimize your grocery supply chain and reduce waste.</p>
            </div>
            <div class="d-flex gap-4 small opacity-50">
                <span><i class="bi bi-check2-all me-1"></i> FEFO Optimized</span>
                <span><i class="bi bi-check2-all me-1"></i> Quality Assurance</span>
                <span><i class="bi bi-check2-all me-1"></i> Demand Forecasting</span>
            </div>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/fm-core.js"></script>

</body>
</html>

FM_IMAGE_ASSET_SUGGESTIONS:
- File: assets/images/heroes/auth-hero-8k.webp
- Dimensions: 1920x1080 (Landscape)
- Format: WebP (High Efficiency)
- Usage: Authentication Right-Side Hero Visual (Split Panel)
- Prompt: Ultra-realistic 8K fresh grocery enterprise visual, premium FreshMart brand atmosphere, modern clean retail produce arrangement, subtle warehouse operations in background, soft natural daylight, green and slate color harmony, high detail, realistic textures, cinematic but restrained, enterprise-grade, no text, no watermark, wide hero composition