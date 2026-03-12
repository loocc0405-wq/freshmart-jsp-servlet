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
    <title>Đăng ký tài khoản - FreshMart</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet"/>
    <link rel="preconnect" href="https://fonts.googleapis.com"/>
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin/>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet"/>

    <style>
        body {
            font-family: Inter, Arial, sans-serif;
            background: linear-gradient(135deg, #eefbf2, #f7fff9 45%, #edf7ff);
            min-height: 100vh;
        }

        .register-shell {
            min-height: 100vh;
            display: flex;
            align-items: center;
            padding: 32px 0;
        }

        .register-card {
            border: 0;
            border-radius: 24px;
            overflow: hidden;
            box-shadow: 0 20px 60px rgba(15, 23, 42, 0.12);
        }

        .register-hero {
            background: linear-gradient(160deg, #14532d, #16a34a 55%, #4ade80);
            color: #fff;
            padding: 40px 32px;
            height: 100%;
        }

        .register-form {
            background: #fff;
            padding: 32px;
        }

        .brand-badge {
            display: inline-flex;
            align-items: center;
            gap: 8px;
            background: rgba(255,255,255,0.14);
            border: 1px solid rgba(255,255,255,0.2);
            border-radius: 999px;
            padding: 8px 14px;
            font-weight: 600;
            margin-bottom: 20px;
        }

        .hero-list li { margin-bottom: 12px; }

        .form-control, .form-select { min-height: 48px; border-radius: 14px; }
        textarea.form-control { min-height: 108px; }

        .btn-register {
            min-height: 50px;
            border-radius: 14px;
            font-weight: 700;
            background: linear-gradient(90deg, #16a34a, #22c55e);
            border: none;
        }

        .password-hint { font-size: 12px; color: #64748b; }

        .field-error { color: #dc2626; font-size: 13px; margin-top: 6px; }

        .section-title {
            font-size: 13px;
            text-transform: uppercase;
            letter-spacing: .08em;
            color: #16a34a;
            font-weight: 800;
            margin-bottom: 12px;
        }

        /* honeypot */
        .hp-field {
            position: absolute;
            left: -9999px;
            width: 1px;
            height: 1px;
            overflow: hidden;
        }

        /* password show/hide */
        .password-wrap { position: relative; }
        .password-wrap .form-control { padding-right: 72px; }
        .toggle-password {
            position: absolute;
            right: 10px;
            top: 50%;
            transform: translateY(-50%);
            border: 0;
            background: transparent;
            color: #166534;
            font-weight: 700;
            font-size: 13px;
            cursor: pointer;
        }

        /* password strength meter */
        .password-meter {
            height: 6px;
            background: #e5e7eb;
            border-radius: 999px;
            overflow: hidden;
            margin-top: 6px;
        }
        .password-meter-bar {
            height: 100%;
            width: 0;
            transition: width .2s ease, background .2s ease;
            background: #ef4444;
        }

        .submit-loading { opacity: .8; pointer-events: none; }
    </style>
</head>
<body>
<div class="container register-shell">
    <div class="row justify-content-center w-100">
        <div class="col-12 col-xl-11">
            <div class="card register-card">
                <div class="row g-0">
                    <div class="col-lg-5 d-none d-lg-block">
                        <div class="register-hero">
                            <div class="brand-badge">FreshMart • Smart Grocery Account</div>
                            <h1 class="fw-bold mb-3">Tạo tài khoản khách hàng thật chuyên nghiệp</h1>
                            <p class="mb-4 opacity-75">
                                Đăng ký tài khoản để theo dõi đơn hàng, lưu thông tin cá nhân, nhận ưu đãi và nâng cấp PRO bất cứ lúc nào.
                            </p>
                            <ul class="hero-list ps-3">
                                <li>Mua hàng nhanh hơn với hồ sơ đã lưu</li>
                                <li>Theo dõi lịch sử đơn hàng và trạng thái giao hàng</li>
                                <li>Nâng cấp PRO sau khi đăng ký để dùng tính năng cao cấp</li>
                            </ul>
                        </div>
                    </div>

                    <div class="col-lg-7">
                        <div class="register-form">
                            <div class="d-flex justify-content-between align-items-start flex-wrap gap-2 mb-3">
                                <div>
                                    <div class="section-title">Create account</div>
                                    <h2 class="fw-bold mb-1">Đăng ký tài khoản</h2>
                                    <p class="text-secondary mb-0">
                                        Tài khoản mới sẽ được tạo dưới dạng <b>CUSTOMER FREE</b>.
                                    </p>
                                </div>
                                <a href="${pageContext.request.contextPath}/login"
                                   class="btn btn-outline-secondary rounded-pill px-3">
                                    Đã có tài khoản?
                                </a>
                            </div>

                            <c:if test="${not empty errors.general}">
                                <div class="alert alert-danger">${fn:escapeXml(errors.general)}</div>
                            </c:if>

                            <form method="post" action="${pageContext.request.contextPath}/register"
                                  id="registerForm" novalidate>
                                <input type="hidden" name="csrf_token" value="${sessionScope.CSRF_TOKEN}" />

                                <%-- honeypot field – invisible to humans, filled by bots --%>
                                <div class="hp-field" aria-hidden="true">
                                    <label for="website">Website</label>
                                    <input type="text" id="website" name="website" tabindex="-1" autocomplete="off" />
                                </div>

                                <div class="section-title mt-4">Thông tin cơ bản</div>
                                <div class="row g-3">
                                    <div class="col-md-6">
                                        <label class="form-label">Họ và tên *</label>
                                        <input type="text" class="form-control" name="fullName"
                                               value="${fn:escapeXml(form.fullName)}"
                                               placeholder="Nguyễn Văn A" maxlength="100"
                                               autocomplete="name" required />
                                        <c:if test="${not empty errors.fullName}">
                                            <div class="field-error">${fn:escapeXml(errors.fullName)}</div>
                                        </c:if>
                                    </div>

                                    <div class="col-md-6">
                                        <label class="form-label">Username *</label>
                                        <input type="text" class="form-control" name="username"
                                               value="${fn:escapeXml(form.username)}"
                                               placeholder="nhu.dev" maxlength="30"
                                               autocomplete="username" required />
                                        <c:if test="${not empty errors.username}">
                                            <div class="field-error">${fn:escapeXml(errors.username)}</div>
                                        </c:if>
                                    </div>

                                    <div class="col-md-6">
                                        <label class="form-label">Email *</label>
                                        <input type="email" class="form-control" name="email"
                                               value="${fn:escapeXml(form.email)}"
                                               placeholder="ban@example.com" maxlength="120"
                                               autocomplete="email" required />
                                        <c:if test="${not empty errors.email}">
                                            <div class="field-error">${fn:escapeXml(errors.email)}</div>
                                        </c:if>
                                    </div>

                                    <div class="col-md-6">
                                        <label class="form-label">Số điện thoại *</label>
                                        <input type="text" class="form-control" name="phone"
                                               value="${fn:escapeXml(form.phone)}"
                                               placeholder="0987654321" maxlength="20"
                                               inputmode="tel" autocomplete="tel" required />
                                        <c:if test="${not empty errors.phone}">
                                            <div class="field-error">${fn:escapeXml(errors.phone)}</div>
                                        </c:if>
                                    </div>
                                </div>

                                <div class="section-title mt-4">Bảo mật</div>
                                <div class="row g-3">
                                    <div class="col-md-6">
                                        <label class="form-label">Mật khẩu *</label>
                                        <div class="password-wrap">
                                            <input type="password" class="form-control" id="password" name="password"
                                                   placeholder="••••••••" minlength="8" maxlength="72"
                                                   autocomplete="new-password" required />
                                            <button type="button" class="toggle-password" data-target="password">Hiện</button>
                                        </div>
                                        <div class="password-meter">
                                            <div class="password-meter-bar" id="passwordMeterBar"></div>
                                        </div>
                                        <div class="password-hint" id="passwordMeterText">
                                            Ít nhất 8 ký tự, có chữ hoa, chữ thường, số và ký tự đặc biệt.
                                        </div>
                                        <c:if test="${not empty errors.password}">
                                            <div class="field-error">${fn:escapeXml(errors.password)}</div>
                                        </c:if>
                                    </div>

                                    <div class="col-md-6">
                                        <label class="form-label">Xác nhận mật khẩu *</label>
                                        <div class="password-wrap">
                                            <input type="password" class="form-control" id="confirmPassword" name="confirmPassword"
                                                   placeholder="Nhập lại mật khẩu" minlength="8" maxlength="72"
                                                   autocomplete="new-password" required />
                                            <button type="button" class="toggle-password" data-target="confirmPassword">Hiện</button>
                                        </div>
                                        <c:if test="${not empty errors.confirmPassword}">
                                            <div class="field-error">${fn:escapeXml(errors.confirmPassword)}</div>
                                        </c:if>
                                    </div>
                                </div>

                                <div class="section-title mt-4">Thông tin bổ sung</div>
                                <div class="row g-3">
                                    <div class="col-md-6">
                                        <label class="form-label">Giới tính</label>
                                        <select class="form-select" name="gender">
                                            <option value="">Chọn giới tính</option>
                                            <option value="MALE"   ${form.gender eq 'MALE'   ? 'selected' : ''}>Nam</option>
                                            <option value="FEMALE" ${form.gender eq 'FEMALE' ? 'selected' : ''}>Nữ</option>
                                            <option value="OTHER"  ${form.gender eq 'OTHER'  ? 'selected' : ''}>Khác</option>
                                        </select>
                                        <c:if test="${not empty errors.gender}">
                                            <div class="field-error">${fn:escapeXml(errors.gender)}</div>
                                        </c:if>
                                    </div>

                                    <div class="col-md-6">
                                        <label class="form-label">Ngày sinh</label>
                                        <input type="date" class="form-control" name="dob"
                                               value="${fn:escapeXml(form.dob)}" />
                                        <c:if test="${not empty errors.dob}">
                                            <div class="field-error">${fn:escapeXml(errors.dob)}</div>
                                        </c:if>
                                    </div>

                                    <div class="col-12">
                                        <label class="form-label">Địa chỉ</label>
                                        <textarea class="form-control" name="address"
                                                  maxlength="255"
                                                  placeholder="Số nhà, đường, phường/xã, quận/huyện, tỉnh/thành">${fn:escapeXml(form.address)}</textarea>
                                        <c:if test="${not empty errors.address}">
                                            <div class="field-error">${fn:escapeXml(errors.address)}</div>
                                        </c:if>
                                    </div>
                                </div>

                                <div class="form-check mt-4">
                                    <input class="form-check-input" type="checkbox" value="1"
                                           id="agreeTerms" name="agreeTerms"
                                           ${form.agreeTerms eq '1' ? 'checked' : ''}>
                                    <label class="form-check-label" for="agreeTerms">
                                        Tôi đồng ý với điều khoản sử dụng và chính sách bảo mật của FreshMart.
                                    </label>
                                </div>
                                <c:if test="${not empty errors.agreeTerms}">
                                    <div class="field-error">${fn:escapeXml(errors.agreeTerms)}</div>
                                </c:if>

                                <button type="submit" id="registerSubmitBtn"
                                        class="btn btn-success btn-register w-100 mt-4">
                                    Tạo tài khoản
                                </button>
                            </form>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<script>
(function () {
    var form       = document.getElementById('registerForm');
    var password   = document.getElementById('password');
    var confirmPw  = document.getElementById('confirmPassword');
    var meterBar   = document.getElementById('passwordMeterBar');
    var meterText  = document.getElementById('passwordMeterText');
    var submitBtn  = document.getElementById('registerSubmitBtn');

    // show / hide password buttons
    document.querySelectorAll('.toggle-password').forEach(function (btn) {
        btn.addEventListener('click', function () {
            var target = document.getElementById(this.dataset.target);
            var isHidden = target.type === 'password';
            target.type = isHidden ? 'text' : 'password';
            this.textContent = isHidden ? 'Ẩn' : 'Hiện';
        });
    });

    // auto-lowercase username and email on blur
    ['username', 'email'].forEach(function (name) {
        var el = document.querySelector('input[name="' + name + '"]');
        if (el) el.addEventListener('blur', function () { this.value = this.value.trim().toLowerCase(); });
    });

    // password strength meter
    function scorePassword(val) {
        if (!val) return 0;
        var score = 0;
        if (val.length >= 8) score++;
        if (/[A-Z]/.test(val)) score++;
        if (/[a-z]/.test(val)) score++;
        if (/\d/.test(val)) score++;
        if (/[^A-Za-z0-9]/.test(val)) score++;
        return score;
    }

    function renderMeter() {
        if (!password || !meterBar || !meterText) return;
        var score = scorePassword(password.value);
        if (score <= 1) {
            meterBar.style.width = '20%'; meterBar.style.background = '#ef4444';
            meterText.textContent = 'Mật khẩu yếu';
        } else if (score <= 3) {
            meterBar.style.width = '60%'; meterBar.style.background = '#f59e0b';
            meterText.textContent = 'Mật khẩu trung bình';
        } else {
            meterBar.style.width = '100%'; meterBar.style.background = '#22c55e';
            meterText.textContent = 'Mật khẩu mạnh';
        }
    }

    if (password) { password.addEventListener('input', renderMeter); renderMeter(); }

    // submit guard
    if (form) {
        form.addEventListener('submit', function (e) {
            if (password && confirmPw && password.value !== confirmPw.value) {
                e.preventDefault();
                confirmPw.focus();
                alert('Xác nhận mật khẩu chưa khớp.');
                return;
            }
            if (submitBtn) {
                submitBtn.disabled = true;
                submitBtn.classList.add('submit-loading');
                submitBtn.textContent = 'Đang tạo tài khoản...';
            }
        });
    }
})();
</script>
</body>
</html>
