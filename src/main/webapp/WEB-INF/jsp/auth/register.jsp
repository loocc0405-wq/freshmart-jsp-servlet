<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

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

        .hero-list li {
            margin-bottom: 12px;
        }

        .form-control, .form-select {
            min-height: 48px;
            border-radius: 14px;
        }

        textarea.form-control {
            min-height: 108px;
        }

        .btn-register {
            min-height: 50px;
            border-radius: 14px;
            font-weight: 700;
            background: linear-gradient(90deg, #16a34a, #22c55e);
            border: none;
        }

        .password-hint {
            font-size: 12px;
            color: #64748b;
        }

        .field-error {
            color: #dc2626;
            font-size: 13px;
            margin-top: 6px;
        }

        .section-title {
            font-size: 13px;
            text-transform: uppercase;
            letter-spacing: .08em;
            color: #16a34a;
            font-weight: 800;
            margin-bottom: 12px;
        }
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
                                <div class="alert alert-danger">${errors.general}</div>
                            </c:if>

                            <form method="post" action="${pageContext.request.contextPath}/register" novalidate>
                                <input type="hidden" name="csrf_token" value="${sessionScope.CSRF_TOKEN}" />

                                <div class="section-title mt-4">Thông tin cơ bản</div>
                                <div class="row g-3">
                                    <div class="col-md-6">
                                        <label class="form-label">Họ và tên *</label>
                                        <input type="text" class="form-control" name="fullName"
                                               value="${form.fullName}" placeholder="Nguyễn Văn A" required />
                                        <c:if test="${not empty errors.fullName}">
                                            <div class="field-error">${errors.fullName}</div>
                                        </c:if>
                                    </div>

                                    <div class="col-md-6">
                                        <label class="form-label">Username *</label>
                                        <input type="text" class="form-control" name="username"
                                               value="${form.username}" placeholder="nhu.dev" required />
                                        <c:if test="${not empty errors.username}">
                                            <div class="field-error">${errors.username}</div>
                                        </c:if>
                                    </div>

                                    <div class="col-md-6">
                                        <label class="form-label">Email *</label>
                                        <input type="email" class="form-control" name="email"
                                               value="${form.email}" placeholder="ban@example.com" required />
                                        <c:if test="${not empty errors.email}">
                                            <div class="field-error">${errors.email}</div>
                                        </c:if>
                                    </div>

                                    <div class="col-md-6">
                                        <label class="form-label">Số điện thoại *</label>
                                        <input type="text" class="form-control" name="phone"
                                               value="${form.phone}" placeholder="0987654321" required />
                                        <c:if test="${not empty errors.phone}">
                                            <div class="field-error">${errors.phone}</div>
                                        </c:if>
                                    </div>
                                </div>

                                <div class="section-title mt-4">Bảo mật</div>
                                <div class="row g-3">
                                    <div class="col-md-6">
                                        <label class="form-label">Mật khẩu *</label>
                                        <input type="password" class="form-control" name="password"
                                               placeholder="••••••••" required />
                                        <div class="password-hint">
                                            Ít nhất 8 ký tự, có chữ hoa, chữ thường, số và ký tự đặc biệt.
                                        </div>
                                        <c:if test="${not empty errors.password}">
                                            <div class="field-error">${errors.password}</div>
                                        </c:if>
                                    </div>

                                    <div class="col-md-6">
                                        <label class="form-label">Xác nhận mật khẩu *</label>
                                        <input type="password" class="form-control" name="confirmPassword"
                                               placeholder="Nhập lại mật khẩu" required />
                                        <c:if test="${not empty errors.confirmPassword}">
                                            <div class="field-error">${errors.confirmPassword}</div>
                                        </c:if>
                                    </div>
                                </div>

                                <div class="section-title mt-4">Thông tin bổ sung</div>
                                <div class="row g-3">
                                    <div class="col-md-6">
                                        <label class="form-label">Giới tính</label>
                                        <select class="form-select" name="gender">
                                            <option value="">Chọn giới tính</option>
                                            <option value="MALE" ${form.gender eq 'MALE' ? 'selected' : ''}>Nam</option>
                                            <option value="FEMALE" ${form.gender eq 'FEMALE' ? 'selected' : ''}>Nữ</option>
                                            <option value="OTHER" ${form.gender eq 'OTHER' ? 'selected' : ''}>Khác</option>
                                        </select>
                                        <c:if test="${not empty errors.gender}">
                                            <div class="field-error">${errors.gender}</div>
                                        </c:if>
                                    </div>

                                    <div class="col-md-6">
                                        <label class="form-label">Ngày sinh</label>
                                        <input type="date" class="form-control" name="dob" value="${form.dob}" />
                                        <c:if test="${not empty errors.dob}">
                                            <div class="field-error">${errors.dob}</div>
                                        </c:if>
                                    </div>

                                    <div class="col-12">
                                        <label class="form-label">Địa chỉ</label>
                                        <textarea class="form-control" name="address"
                                                  placeholder="Số nhà, đường, phường/xã, quận/huyện, tỉnh/thành">${form.address}</textarea>
                                        <c:if test="${not empty errors.address}">
                                            <div class="field-error">${errors.address}</div>
                                        </c:if>
                                    </div>
                                </div>

                                <div class="form-check mt-4">
                                    <input class="form-check-input" type="checkbox" value="1"
                                           id="agreeTerms" name="agreeTerms">
                                    <label class="form-check-label" for="agreeTerms">
                                        Tôi đồng ý với điều khoản sử dụng và chính sách bảo mật của FreshMart.
                                    </label>
                                </div>
                                <c:if test="${not empty errors.agreeTerms}">
                                    <div class="field-error">${errors.agreeTerms}</div>
                                </c:if>

                                <button type="submit" class="btn btn-success btn-register w-100 mt-4">
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
</body>
</html>
