<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="form" value="${empty formData ? param : formData}" />
<c:set var="errors" value="${requestScope.errors}" />

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Create Account | FreshMart Enterprise</title>
    
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet"/>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet"/>
    <link rel="preconnect" href="https://fonts.googleapis.com"/>
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin/>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&family=Outfit:wght@600;800&display=swap" rel="stylesheet"/>

    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/fm-design-system.css?v=1"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/fm-components.css?v=1"/>

    <style>
        body { background: var(--fm-slate-50); }
        .branding-logo {
            font-family: 'Outfit', sans-serif;
            font-weight: 800;
            color: var(--fm-primary-600);
            text-decoration: none;
            display: flex;
            align-items: center;
            gap: 0.5rem;
        }
        .form-section-title {
            font-size: 0.75rem;
            font-weight: 800;
            text-transform: uppercase;
            letter-spacing: 0.1em;
            color: var(--fm-primary-600);
            margin-bottom: 1.5rem;
            display: flex;
            align-items: center;
            gap: 0.75rem;
        }
        .form-section-title::after {
            content: "";
            flex: 1;
            height: 1px;
            background: var(--fm-slate-100);
        }
        .field-error {
            font-size: 0.75rem;
            color: var(--fm-danger);
            margin-top: 0.25rem;
            font-weight: 500;
        }
        .password-strength {
            height: 4px;
            background: var(--fm-slate-100);
            border-radius: 2px;
            margin-top: 0.5rem;
            overflow: hidden;
        }
        .password-strength-bar {
            height: 100%;
            width: 0;
            transition: all 0.3s ease;
        }
    </style>
</head>
<body>

<div class="container py-5">
    <div class="row justify-content-center">
        <div class="col-lg-10 col-xl-9">
            <div class="row g-0 fm-surface shadow-lg overflow-hidden">
                <!-- Left: Info Hero (Condensed for Enterprise) -->
                <div class="col-lg-4 bg-dark text-white p-5 d-none d-lg-flex flex-column justify-content-between position-relative">
                    <!-- FM_IMAGE_PROMPT: 
                    Ultra-realistic 8K minimal premium grocery operations visual, clean empty storage section with elegant composition, soft light, organized shelves, premium modern enterprise atmosphere, subtle green accents, no text, no watermark
                    -->
                    <div style="z-index: 2;">
                        <a href="${pageContext.request.contextPath}/" class="branding-logo text-white mb-5">
                            <i class="bi bi-leaf-fill"></i> FreshMart
                        </a>
                        <h2 class="fm-h2 mb-4">Enterprise Onboarding</h2>
                        <p class="opacity-75 fs-6 mb-5">Join the most advanced grocery fulfillment network. Gain access to real-time inventory tracking and smart logistics.</p>
                        
                        <ul class="list-unstyled opacity-75 small">
                            <li class="mb-3 d-flex gap-2"><i class="bi bi-check-circle-fill text-success"></i> Comprehensive Dashboard</li>
                            <li class="mb-3 d-flex gap-2"><i class="bi bi-check-circle-fill text-success"></i> Smart Inventory Alerts</li>
                            <li class="mb-3 d-flex gap-2"><i class="bi bi-check-circle-fill text-success"></i> Analytics & Forecasting</li>
                        </ul>
                    </div>
                    <div class="mt-auto opacity-50 small" style="z-index: 2;">
                        &copy; 2026 FreshMart Global Operations.
                    </div>
                    <!-- Subtle pattern overlay -->
                    <div class="position-absolute top-0 start-0 w-100 h-100 opacity-10" style="background-image: radial-gradient(var(--fm-primary-500) 1px, transparent 0); background-size: 24px 24px;"></div>
                </div>

                <!-- Right: Registration Form -->
                <div class="col-lg-8 bg-white p-5">
                    <div class="d-flex justify-content-between align-items-start mb-4">
                        <div>
                            <h1 class="fm-h1 mb-1">Create Account</h1>
                            <p class="fm-text-secondary">Fill in the details to register as a Customer.</p>
                        </div>
                        <a href="${pageContext.request.contextPath}/login" class="btn btn-link link-primary fw-bold text-decoration-none small">
                            Existing User? Login
                        </a>
                    </div>

                    <c:if test="${not empty errors.general}">
                        <div class="alert alert-danger border-0 bg-danger-subtle mb-4">
                            <i class="bi bi-exclamation-octagon-fill me-2"></i> ${fn:escapeXml(errors.general)}
                        </div>
                    </c:if>

                    <form method="post" action="${pageContext.request.contextPath}/register" id="registerForm" novalidate>
                        <input type="hidden" name="csrf_token" value="${sessionScope.CSRF_TOKEN}" />
                        
                        <!-- Honeypot -->
                        <div style="display:none;" aria-hidden="true">
                            <input type="text" name="website" tabindex="-1" autocomplete="off" />
                        </div>

                        <div class="form-section-title">Core Identity</div>
                        <div class="row g-4 mb-5">
                            <div class="col-md-6">
                                <label class="form-label fw-bold small">Full Legal Name</label>
                                <input type="text" class="fm-form-control" name="fullName"
                                       value="${fn:escapeXml(form.fullName)}" placeholder="e.g. Huy Anh" required />
                                <c:if test="${not empty errors.fullName}"><div class="field-error">${fn:escapeXml(errors.fullName)}</div></c:if>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label fw-bold small">Preferred Username</label>
                                <input type="text" class="fm-form-control" name="username"
                                       value="${fn:escapeXml(form.username)}" placeholder="e.g. huyanh_dev" required />
                                <c:if test="${not empty errors.username}"><div class="field-error">${fn:escapeXml(errors.username)}</div></c:if>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label fw-bold small">Primary Email Address</label>
                                <input type="email" class="fm-form-control" name="email"
                                       value="${fn:escapeXml(form.email)}" placeholder="work@example.com" required />
                                <c:if test="${not empty errors.email}"><div class="field-error">${fn:escapeXml(errors.email)}</div></c:if>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label fw-bold small">Contact Number</label>
                                <input type="tel" class="fm-form-control" name="phone"
                                       value="${fn:escapeXml(form.phone)}" placeholder="+84 000 000 000" required />
                                <c:if test="${not empty errors.phone}"><div class="field-error">${fn:escapeXml(errors.phone)}</div></c:if>
                            </div>
                        </div>

                        <div class="form-section-title">Security Credentials</div>
                        <div class="row g-4 mb-5">
                            <div class="col-md-6">
                                <label class="form-label fw-bold small">Access Password</label>
                                <div class="position-relative">
                                    <input type="password" id="password" name="password" class="fm-form-control pe-5" placeholder="••••••••" required />
                                    <button type="button" class="btn btn-link position-absolute top-50 end-0 translate-middle-y me-1 p-1 text-muted"
                                            data-fm-toggle="password" data-fm-target="password">
                                        <i class="bi bi-eye"></i>
                                    </button>
                                </div>
                                <div class="password-strength"><div id="pwStrengthBar" class="password-strength-bar"></div></div>
                                <c:if test="${not empty errors.password}"><div class="field-error">${fn:escapeXml(errors.password)}</div></c:if>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label fw-bold small">Confirm Credentials</label>
                                <div class="position-relative">
                                    <input type="password" id="confirmPassword" name="confirmPassword" class="fm-form-control pe-5" placeholder="••••••••" required />
                                    <button type="button" class="btn btn-link position-absolute top-50 end-0 translate-middle-y me-1 p-1 text-muted"
                                            data-fm-toggle="password" data-fm-target="confirmPassword">
                                        <i class="bi bi-eye"></i>
                                    </button>
                                </div>
                                <c:if test="${not empty errors.confirmPassword}"><div class="field-error">${fn:escapeXml(errors.confirmPassword)}</div></c:if>
                            </div>
                        </div>

                        <div class="form-section-title">Operational Context (Optional)</div>
                        <div class="row g-4 mb-5">
                            <div class="col-md-6">
                                <label class="form-label fw-bold small">Address / Warehouse Hub</label>
                                <input type="text" class="fm-form-control" name="address" value="${fn:escapeXml(form.address)}" placeholder="Hub location..." />
                            </div>
                            <div class="col-md-6">
                                <label class="form-label fw-bold small">Date of Birth</label>
                                <input type="date" class="fm-form-control" name="dob" value="${fn:escapeXml(form.dob)}" />
                            </div>
                        </div>

                        <div class="mb-4">
                            <div class="form-check p-0">
                                <label class="d-flex gap-2" style="cursor:pointer;">
                                    <input type="checkbox" name="agreeTerms" value="1" 
                                           ${form.agreeTerms eq '1' ? 'checked' : ''} class="mt-1">
                                    <span class="small text-muted">I certify that all information provided is accurate and I agree to the <a href="#" class="fw-bold">Service Compliance Protocols</a>.</span>
                                </label>
                                <c:if test="${not empty errors.agreeTerms}"><div class="field-error">${fn:escapeXml(errors.agreeTerms)}</div></c:if>
                            </div>
                        </div>

                        <button type="submit" class="fm-btn fm-btn-primary w-100 py-3 shadow-sm border-0">
                            Initialize Account Profile <i class="bi bi-plus-circle ms-2"></i>
                        </button>
                    </form>
                </div>
            </div>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/fm-core.js"></script>

<script>
    // Password Strength Meter
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
            
            const colors = ['#f8fafc', '#ef4444', '#f59e0b', '#22c55e', '#16a34a'];
            const widths = ['0%', '25%', '50%', '75%', '100%'];
            
            bar.style.width = widths[score];
            bar.style.backgroundColor = colors[score];
        });
    }
</script>

</body>
</html>

FM_IMAGE_ASSET_SUGGESTIONS:
- File: assets/images/heroes/register-side-8k.webp
- Dimensions: 800x1200 (Portrait)
- Format: WebP
- Usage: Registration Left-Side Support Visual
- Prompt: Ultra-realistic 8K minimal premium grocery operations visual, clean empty storage section with elegant composition, soft light, organized shelves, premium modern enterprise atmosphere, subtle green accents, no text, no watermark
