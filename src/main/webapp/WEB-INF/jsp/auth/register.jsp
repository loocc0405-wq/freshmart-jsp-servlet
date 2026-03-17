<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="form" value="${empty formData ? param : formData}" />
<c:set var="errors" value="${requestScope.errors}" />

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1"/>
    <title>Create Account | FreshMart Enterprise</title>
    
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
        .form-section-header {
            font-size: 0.75rem;
            font-weight: 800;
            text-transform: uppercase;
            letter-spacing: 0.1em;
            color: var(--fm-primary-600);
            margin-bottom: 1.5rem;
            display: flex;
            align-items: center;
            gap: 0.75rem;
            opacity: 0.9;
        }
        .form-section-header::after {
            content: "";
            flex: 1;
            height: 1px;
            background: var(--fm-slate-100);
        }
        .field-error {
            font-size: 0.75rem;
            color: var(--fm-danger);
            margin-top: 0.4rem;
            font-weight: 600;
            display: flex;
            align-items: center;
            gap: 0.25rem;
        }
        .password-strength {
            height: 4px;
            background: var(--fm-slate-100);
            border-radius: 2px;
            margin-top: 0.75rem;
            overflow: hidden;
            width: 100%;
        }
        .password-strength-bar {
            height: 100%;
            width: 0;
            transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
        }
        .register-card-container {
            width: 100%;
            max-width: 640px;
            animation: fadeIn 0.6s ease-out;
        }
        @keyframes fadeIn {
            from { opacity: 0; transform: translateY(15px); }
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
    <!-- Media Section (Hero) -->
    <div class="fm-split-side-hero d-none d-lg-block">
        <img src="${pageContext.request.contextPath}/assets/images/heroes/register-hero-8k.png" 
             alt="FreshMart Onboarding Hero" class="fm-hero-media">
        <div class="fm-media-overlay" style="background: linear-gradient(to bottom, rgba(15, 23, 42, 0.8), rgba(15, 23, 42, 0.4));">
            <div class="mb-5" style="max-width: 500px;">
                <a href="${pageContext.request.contextPath}/" class="branding-logo text-white mb-5 shadow-sm">
                    <i class="bi bi-leaf-fill text-primary"></i> FreshMart
                </a>
                <h2 class="display-4 fw-extrabold mb-4 text-white">Start Your Smart Logistics Journey</h2>
                <p class="lead text-white opacity-90 mb-5" style="font-weight: 500;">
                    Join the leading food supply network. Experience an optimized and transparent warehousing management system.
                </p>
                
                <div class="d-grid gap-4">
                    <div class="d-flex align-items-center gap-3">
                        <div class="bg-primary rounded-circle p-2 shadow-sm">
                            <i class="bi bi-check-lg text-white"></i>
                        </div>
                        <span class="fw-semibold text-white">Advanced Analytics Reporting</span>
                    </div>
                    <div class="d-flex align-items-center gap-3">
                        <div class="bg-primary rounded-circle p-2 shadow-sm">
                            <i class="bi bi-check-lg text-white"></i>
                        </div>
                        <span class="fw-semibold text-white">Intelligent FEFO Inventory Alerts</span>
                    </div>
                    <div class="d-flex align-items-center gap-3">
                        <div class="bg-primary rounded-circle p-2 shadow-sm">
                            <i class="bi bi-check-lg text-white"></i>
                        </div>
                        <span class="fw-semibold text-white">Supply Chain Automation</span>
                    </div>
                </div>
            </div>
            <div class="mt-auto opacity-50 small text-white fw-medium">
                &copy; 2026 FreshMart Global Operations Control.
            </div>
        </div>
    </div>

    <!-- Registration Section -->
    <div class="fm-split-side-content">
        <div class="register-card-container">
            <div class="d-flex justify-content-between align-items-start mb-5">
                <div>
                    <h1 class="fm-h1 mb-2">Create Account</h1>
                    <p class="fm-text-secondary">Provide your information to join the FreshMart operations network.</p>
                </div>
                <a href="${pageContext.request.contextPath}/login" class="fm-btn btn-light border small text-decoration-none">
                    Already a member? Sign In
                </a>
            </div>

            <c:if test="${not empty errors.general}">
                <div class="alert alert-danger d-flex align-items-center border-0 shadow-sm mb-4 bg-danger-subtle">
                    <i class="bi bi-exclamation-octagon-fill me-3 fs-5 text-danger"></i>
                    <div class="small fw-bold">${fn:escapeXml(errors.general)}</div>
                </div>
            </c:if>

            <form method="post" action="${pageContext.request.contextPath}/register" id="registerForm" novalidate class="fm-form">
                <input type="hidden" name="csrf_token" value="${sessionScope.CSRF_TOKEN}" />
                
                <!-- Anti-spam Honeypot -->
                <div style="display:none;" aria-hidden="true">
                    <input type="text" name="website" tabindex="-1" autocomplete="off" />
                </div>

                <!-- Section: Identity -->
                <div class="form-section-header">Identity Credentials</div>
                <div class="row g-4 mb-5">
                    <div class="col-md-6">
                        <label class="fm-label">Full Legal Name</label>
                        <input type="text" class="fm-form-control ${not empty errors.fullName ? 'is-invalid' : ''}" 
                               name="fullName" value="${fn:escapeXml(form.fullName)}" placeholder="e.g. John Doe" required />
                        <c:if test="${not empty errors.fullName}">
                            <div class="field-error"><i class="bi bi-info-circle me-1"></i> ${fn:escapeXml(errors.fullName)}</div>
                        </c:if>
                    </div>
                    <div class="col-md-6">
                        <label class="fm-label">Requested Username</label>
                        <input type="text" class="fm-form-control ${not empty errors.username ? 'is-invalid' : ''}" 
                               name="username" value="${fn:escapeXml(form.username)}" placeholder="e.g. johndoe_fm" required />
                        <c:if test="${not empty errors.username}">
                            <div class="field-error"><i class="bi bi-info-circle me-1"></i> ${fn:escapeXml(errors.username)}</div>
                        </c:if>
                    </div>
                    <div class="col-md-6">
                        <label class="fm-label">Work Email Address</label>
                        <input type="email" class="fm-form-control ${not empty errors.email ? 'is-invalid' : ''}" 
                               name="email" value="${fn:escapeXml(form.email)}" placeholder="email@company.com" required />
                        <c:if test="${not empty errors.email}">
                            <div class="field-error"><i class="bi bi-info-circle me-1"></i> ${fn:escapeXml(errors.email)}</div>
                        </c:if>
                    </div>
                    <div class="col-md-6">
                        <label class="fm-label">Contact Number</label>
                        <input type="tel" class="fm-form-control ${not empty errors.phone ? 'is-invalid' : ''}" 
                               name="phone" value="${fn:escapeXml(form.phone)}" placeholder="+84 xxx xxx xxx" required />
                        <c:if test="${not empty errors.phone}">
                            <div class="field-error"><i class="bi bi-info-circle me-1"></i> ${fn:escapeXml(errors.phone)}</div>
                        </c:if>
                    </div>
                </div>

                <!-- Section: Security -->
                <div class="form-section-header">Security Architecture</div>
                <div class="row g-4 mb-5">
                    <div class="col-md-6">
                        <label class="fm-label">Access Password</label>
                        <div class="position-relative">
                            <input type="password" id="password" name="password" 
                                   class="fm-form-control pe-5 ${not empty errors.password ? 'is-invalid' : ''}" placeholder="••••••••" required />
                            <button type="button" class="btn btn-link position-absolute top-50 end-0 translate-middle-y me-1 p-1 text-muted text-decoration-none shadow-none"
                                    data-fm-toggle="password" data-fm-target="password">
                                <i class="bi bi-eye"></i>
                            </button>
                        </div>
                        <div class="password-strength"><div id="pwStrengthBar" class="password-strength-bar"></div></div>
                        <c:if test="${not empty errors.password}">
                            <div class="field-error"><i class="bi bi-info-circle me-1"></i> ${fn:escapeXml(errors.password)}</div>
                        </c:if>
                    </div>
                    <div class="col-md-6">
                        <label class="fm-label">Verify Password</label>
                        <div class="position-relative">
                            <input type="password" id="confirmPassword" name="confirmPassword" 
                                   class="fm-form-control pe-5 ${not empty errors.confirmPassword ? 'is-invalid' : ''}" placeholder="••••••••" required />
                            <button type="button" class="btn btn-link position-absolute top-50 end-0 translate-middle-y me-1 p-1 text-muted text-decoration-none shadow-none"
                                    data-fm-toggle="password" data-fm-target="confirmPassword">
                                <i class="bi bi-eye"></i>
                            </button>
                        </div>
                        <c:if test="${not empty errors.confirmPassword}">
                            <div class="field-error"><i class="bi bi-info-circle me-1"></i> ${fn:escapeXml(errors.confirmPassword)}</div>
                        </c:if>
                    </div>
                </div>

                <!-- Section: Optional Context -->
                <div class="form-section-header">Operational Context (Optional)</div>
                <div class="row g-4 mb-5">
                    <div class="col-md-6">
                        <label class="fm-label">Primary Hub / Warehouse Address</label>
                        <input type="text" class="fm-form-control" name="address" value="${fn:escapeXml(form.address)}" placeholder="e.g. District 1, HCMC" />
                    </div>
                    <div class="col-md-6">
                        <label class="fm-label">Date of Birth</label>
                        <input type="date" class="fm-form-control" name="dob" value="${fn:escapeXml(form.dob)}" />
                    </div>
                </div>

                <div class="mb-5">
                    <div class="form-check p-0 d-flex gap-3">
                        <input type="checkbox" name="agreeTerms" value="1" 
                               ${form.agreeTerms eq '1' ? 'checked' : ''} class="form-check-input mt-1 shadow-none" id="agreeTerms">
                        <label class="form-check-label small text-muted lh-base" for="agreeTerms" style="cursor:pointer;">
                            I certify that all information provided is accurate and agree to adhere to the 
                            <a href="#" class="fw-bold text-primary text-decoration-none">Compliance & Security Protocols</a> of the FreshMart network.
                        </label>
                    </div>
                    <c:if test="${not empty errors.agreeTerms}">
                        <div class="field-error mt-2"><i class="bi bi-info-circle me-1"></i> ${fn:escapeXml(errors.agreeTerms)}</div>
                    </c:if>
                </div>

                <button type="submit" class="fm-btn fm-btn-primary w-100 py-3 shadow-lg border-0">
                    Initialize Account Profile <i class="bi bi-arrow-right-short ms-2 fs-5"></i>
                </button>
            </form>

            <div class="fm-auth-footer">
                &copy; 2026 FreshMart Enterprise Edition. Digital Supply Chain Management Platform.
            </div>
        </div>
    </div>
</div>

<!-- Scripts -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/fm-core.js?v=2"></script>

<script>
    // Password Strength Meter Implementation
    const pwInput = document.getElementById('password');
    const bar = document.getElementById('pwStrengthBar');
    if (pwInput && bar) {
        pwInput.addEventListener('input', function() {
            const val = this.value;
            let score = 0;
            if (val.length >= 8) score++;
            if (/[A-Z]/.test(val)) score++;
            if (/\d/.test(val)) score++;
            if (/[^A-Za-z0-9]/.test(val)) score++;
            
            const colors = ['#f1f5f9', '#ef4444', '#f59e0b', '#22c55e', '#16a34a'];
            const widths = ['0%', '25%', '50%', '75%', '100%'];
            
            bar.style.width = widths[score];
            bar.style.backgroundColor = colors[score];
        });
    }
</script>

</body>
</html>
