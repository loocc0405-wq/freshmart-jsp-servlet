<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <%@ taglib prefix="c" uri="jakarta.tags.core" %>
        <%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

            <c:set var="pageTitle" value="Upgrade to PRO" />
            <jsp:include page="/WEB-INF/jsp/common/header.jsp" />

            <div class="fm-page-header">
                <div>
                    <h1 class="fm-page-title">Nâng cấp tài khoản PRO</h1>
                    <p class="fm-page-subtitle mb-0">
                        So sánh FREE và PRO, chọn gói nhanh, fake payment và theo dõi lịch sử thanh toán.
                    </p>
                </div>

                <div class="d-flex gap-2 flex-wrap">
                    <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/catalog">Về
                        Catalog</a>
                    <a class="btn btn-outline-secondary"
                        href="${pageContext.request.contextPath}/customer/dashboard">Customer Dashboard</a>

                    <c:if test="${sessionScope.authUser != null && sessionScope.authUser.proActive}">
                        <a class="btn btn-outline-primary" href="${pageContext.request.contextPath}/pro/dashboard">
                            Đi tới PRO Dashboard
                        </a>
                    </c:if>
                </div>
            </div>

            <div class="d-flex justify-content-between align-items-center mb-4 flex-wrap gap-2">
                <div>
                    <h5 class="mb-1">Trạng thái hiện tại</h5>
                    <p class="text-muted mb-0">Kiểm tra tier hiện tại và gia hạn cộng dồn khi mua thêm.</p>
                </div>

                <div>
                    <c:choose>
                        <c:when test="${not empty subStatus && subStatus.status eq 'PRO_ACTIVE'}">
                            <span class="badge text-bg-success fs-6">
                                PRO đến
                                <c:out value="${sessionScope.authUser.expiredDate}" />
                            </span>
                        </c:when>
                        <c:when test="${not empty subStatus && subStatus.status eq 'PRO_EXPIRING_SOON'}">
                            <span class="badge text-bg-warning fs-6">
                                PRO — còn
                                <c:out value="${subStatus.daysRemaining}" /> ngày
                            </span>
                        </c:when>
                        <c:when test="${not empty subStatus && subStatus.status eq 'PRO_EXPIRED_IN_GRACE'}">
                            <span class="badge text-bg-danger fs-6">PRO hết hạn — đang trong grace</span>
                        </c:when>
                        <c:when test="${not empty subStatus && subStatus.status eq 'PRO_EXPIRED'}">
                            <span class="badge text-bg-dark fs-6">PRO đã hết hạn</span>
                        </c:when>
                        <c:otherwise>
                            <span class="badge text-bg-secondary fs-6">FREE</span>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>

            <%-- Status banners based on subscription status --%>
                <c:if test="${not empty subStatus && subStatus.status eq 'PRO_EXPIRING_SOON'}">
                    <div class="alert alert-warning">
                        <i class="bi bi-exclamation-triangle me-1"></i>
                        <strong>Gói PRO của bạn sẽ hết hạn sau
                            <c:out value="${subStatus.daysRemaining}" /> ngày.
                        </strong>
                        Hãy gia hạn sớm để tiếp tục dùng tính năng PRO.
                    </div>
                </c:if>

                <c:if test="${not empty subStatus && subStatus.status eq 'PRO_EXPIRED_IN_GRACE'}">
                    <div class="alert alert-danger">
                        <i class="bi bi-clock-history me-1"></i>
                        <strong>Gói PRO của bạn đã hết hạn
                            <c:out value="${subStatus.daysExpired}" /> ngày trước.
                        </strong>
                        Bạn còn <strong>
                            <c:out value="${subStatus.graceRemaining}" /> ngày
                        </strong> grace period để gia hạn.
                        <hr class="my-2" />
                        <a class="btn btn-danger btn-sm" href="#planSection">
                            <i class="bi bi-lightning-charge me-1"></i> Gia hạn ngay
                        </a>
                    </div>
                </c:if>

                <c:if test="${not empty subStatus && subStatus.status eq 'PRO_EXPIRED'}">
                    <div class="alert alert-dark">
                        <i class="bi bi-x-circle me-1"></i>
                        <strong>Gói PRO đã hết hạn hoàn toàn.</strong>
                        Bạn cần mua gói mới để kích hoạt lại PRO.
                    </div>
                </c:if>

                <c:if test="${param.expired eq '1' && (empty subStatus || subStatus.status eq 'FREE')}">
                    <div class="alert alert-warning">
                        <i class="bi bi-exclamation-triangle me-1"></i>
                        Gói PRO của bạn đã hết hạn. Vui lòng gia hạn để tiếp tục dùng tính năng PRO.
                    </div>
                </c:if>

                <c:if test="${not empty errorMessage}">
                    <div class="alert alert-danger">
                        <i class="bi bi-exclamation-triangle me-1"></i>
                        <c:out value="${errorMessage}" />
                    </div>
                </c:if>

                <div class="row g-4 mb-4">
                    <div class="col-lg-5">
                        <div class="fm-surface padded h-100">
                            <h5 class="mb-3">Thông tin gói hiện tại</h5>

                            <c:choose>
                                <c:when test="${sessionScope.authUser != null}">
                                    <dl class="row mb-0">
                                        <dt class="col-sm-5">Username</dt>
                                        <dd class="col-sm-7">
                                            <c:out value="${sessionScope.authUser.username}" />
                                        </dd>

                                        <dt class="col-sm-5">Role</dt>
                                        <dd class="col-sm-7">
                                            <c:out value="${sessionScope.authUser.role}" />
                                        </dd>

                                        <dt class="col-sm-5">Tier</dt>
                                        <dd class="col-sm-7">
                                            <c:choose>
                                                <c:when test="${sessionScope.authUser.tier.toString() eq 'PRO'}">
                                                    <span class="badge text-bg-success">PRO</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge text-bg-secondary">FREE</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </dd>

                                        <dt class="col-sm-5">Ngày hết hạn</dt>
                                        <dd class="col-sm-7">
                                            <c:choose>
                                                <c:when test="${not empty sessionScope.authUser.expiredDate}">
                                                    <c:out value="${sessionScope.authUser.expiredDate}" />
                                                </c:when>
                                                <c:otherwise>-</c:otherwise>
                                            </c:choose>
                                        </dd>

                                        <dt class="col-sm-5">Trạng thái</dt>
                                        <dd class="col-sm-7">
                                            <c:choose>
                                                <c:when
                                                    test="${not empty subStatus && subStatus.status eq 'PRO_ACTIVE'}">
                                                    <span class="text-success fw-semibold">
                                                        Đang hoạt động (còn
                                                        <c:out value="${subStatus.daysRemaining}" /> ngày)
                                                    </span>
                                                </c:when>
                                                <c:when
                                                    test="${not empty subStatus && subStatus.status eq 'PRO_EXPIRING_SOON'}">
                                                    <span class="text-warning fw-semibold">
                                                        Sắp hết hạn (còn
                                                        <c:out value="${subStatus.daysRemaining}" /> ngày)
                                                    </span>
                                                </c:when>
                                                <c:when
                                                    test="${not empty subStatus && subStatus.status eq 'PRO_EXPIRED_IN_GRACE'}">
                                                    <span class="text-danger fw-semibold">
                                                        Hết hạn — grace còn
                                                        <c:out value="${subStatus.graceRemaining}" /> ngày
                                                    </span>
                                                </c:when>
                                                <c:when
                                                    test="${not empty subStatus && subStatus.status eq 'PRO_EXPIRED'}">
                                                    <span class="text-danger fw-semibold">Đã hết hạn hoàn toàn</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="text-muted">Chưa nâng cấp</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </dd>
                                    </dl>

                                    <hr class="fm-divider" />

                                    <c:choose>
                                        <c:when
                                            test="${not empty subStatus && (subStatus.status eq 'PRO_ACTIVE' || subStatus.status eq 'PRO_EXPIRING_SOON')}">
                                            <div class="alert alert-success mb-0">
                                                <i class="bi bi-stars me-1"></i>
                                                Tài khoản của bạn đang dùng PRO. Mua thêm sẽ được cộng dồn thời hạn.
                                            </div>
                                        </c:when>
                                        <c:when
                                            test="${not empty subStatus && subStatus.status eq 'PRO_EXPIRED_IN_GRACE'}">
                                            <div class="alert alert-danger mb-0">
                                                <i class="bi bi-clock-history me-1"></i>
                                                PRO đã hết hạn nhưng còn grace period. Gia hạn ngay để không mất quyền
                                                truy cập!
                                            </div>
                                        </c:when>
                                        <c:otherwise>
                                            <div class="alert alert-warning mb-0">
                                                <i class="bi bi-unlock me-1"></i>
                                                Bạn đang ở FREE hoặc PRO đã hết hạn. Hãy chọn gói để kích hoạt lại.
                                            </div>
                                        </c:otherwise>
                                    </c:choose>
                                </c:when>

                                <c:otherwise>
                                    <div class="alert alert-warning mb-0">
                                        Bạn cần đăng nhập để nâng cấp tài khoản.
                                    </div>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>

                    <div class="col-lg-7">
                        <div class="fm-surface padded h-100">
                            <h5 class="mb-3">So sánh quyền lợi FREE và PRO</h5>

                            <div class="row g-3">
                                <div class="col-md-6">
                                    <div class="card h-100 shadow-sm">
                                        <div class="card-header bg-light">
                                            <h5 class="mb-0">FREE</h5>
                                        </div>
                                        <div class="card-body">
                                            <ul class="mb-0">
                                                <li>Xem catalog, mua hàng cơ bản</li>
                                                <li>Không truy cập module PRO</li>
                                                <li>Không có phân tích nâng cao</li>
                                                <li>Không có gợi ý replenishment PRO</li>
                                            </ul>
                                        </div>
                                    </div>
                                </div>

                                <div class="col-md-6">
                                    <div class="card h-100 shadow-sm border-success">
                                        <div class="card-header bg-success text-white">
                                            <h5 class="mb-0">PRO</h5>
                                        </div>
                                        <div class="card-body">
                                            <ul class="mb-0">
                                                <li>Truy cập đầy đủ module PRO</li>
                                                <li>Dashboard / seasonality / replenishment</li>
                                                <li>Dự báo tồn kho nâng cao</li>
                                                <li>Gia hạn cộng dồn nếu gói cũ còn hạn</li>
                                            </ul>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <div class="alert alert-info mt-3 mb-0">
                                Đây là màn hình fake payment để phục vụ demo chức năng subscription / upgrade / payment
                                history.
                            </div>
                        </div>
                    </div>
                </div>

                <div id="planSection" class="fm-surface padded mb-4">
                    <div class="d-flex justify-content-between align-items-center mb-3 flex-wrap gap-2">
                        <div>
                            <h5 class="mb-1">Chọn gói PRO</h5>
                            <p class="text-muted mb-0">Chọn phương thức thanh toán rồi bấm gói muốn mua.</p>
                        </div>
                    </div>

                    <c:choose>
                        <c:when test="${sessionScope.authUser == null}">
                            <div class="alert alert-warning mb-0">
                                Bạn cần đăng nhập để thực hiện nâng cấp tài khoản.
                            </div>
                        </c:when>

                        <c:otherwise>
                            <form method="post" action="${pageContext.request.contextPath}/subscription/upgrade">
                                <input type="hidden" name="csrf_token" value="${sessionScope.CSRF_TOKEN}" />

                                <div class="row g-3 mb-4">
                                    <div class="col-md-4">
                                        <label class="form-label">Phương thức thanh toán</label>
                                        <select class="form-select" name="paymentMethod" required>
                                            <option value="FAKE_CARD">Fake Card</option>
                                            <option value="BANK_TRANSFER">Bank Transfer</option>
                                            <option value="MOMO">MoMo</option>
                                            <option value="CASH">Cash</option>
                                        </select>
                                    </div>
                                </div>

                                <div class="row g-4">
                                    <c:forEach items="${planPrices}" var="plan">
                                        <div class="col-md-4">
                                            <div class="card h-100 shadow-sm
                                <c:if test='${plan.key == 90}'>border-primary</c:if>
                                <c:if test='${plan.key == 365}'>border-dark</c:if>
                            ">
                                                <div class="card-body d-flex flex-column">
                                                    <c:choose>
                                                        <c:when test="${plan.key == 30}">
                                                            <h5>PRO 30 ngày</h5>
                                                        </c:when>
                                                        <c:when test="${plan.key == 90}">
                                                            <h5>PRO 90 ngày</h5>
                                                            <div class="mb-2">
                                                                <span class="badge text-bg-success">Phổ biến</span>
                                                            </div>
                                                        </c:when>
                                                        <c:when test="${plan.key == 365}">
                                                            <h5>PRO 365 ngày</h5>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <h5>PRO
                                                                <c:out value="${plan.key}" /> ngày
                                                            </h5>
                                                        </c:otherwise>
                                                    </c:choose>

                                                    <div class="display-6 mb-3">
                                                        <fmt:formatNumber value="${plan.value}" type="number"
                                                            maxFractionDigits="0" />đ
                                                    </div>

                                                    <div class="text-muted small mb-3">
                                                        Gia hạn sẽ được cộng dồn nếu tài khoản PRO hiện vẫn còn hiệu
                                                        lực.
                                                    </div>

                                                    <button class="btn
                                            <c:choose>
                                                <c:when test='${plan.key == 90}'>btn-success</c:when>
                                                <c:when test='${plan.key == 365}'>btn-dark</c:when>
                                                <c:otherwise>btn-primary</c:otherwise>
                                            </c:choose>
                                            w-100 mt-auto" type="submit" name="planDays" value="${plan.key}">
                                                        Chọn gói
                                                    </button>
                                                </div>
                                            </div>
                                        </div>
                                    </c:forEach>
                                </div>
                            </form>
                        </c:otherwise>
                    </c:choose>
                </div>

                <%--========Payment History========--%>
                    <div class="fm-surface padded mb-4">
                        <div class="d-flex justify-content-between align-items-center mb-3 flex-wrap gap-2">
                            <h5 class="mb-0">Lịch sử subscription payment</h5>
                            <span class="text-muted small">
                                <c:choose>
                                    <c:when test="${empty paymentHistory}">0 giao dịch</c:when>
                                    <c:otherwise>
                                        <c:out value="${paymentHistory.size()}" /> giao dịch
                                    </c:otherwise>
                                </c:choose>
                            </span>
                        </div>

                        <c:choose>
                            <c:when test="${empty paymentHistory}">
                                <div class="alert alert-light border mb-0">
                                    Chưa có payment history cho tài khoản này.
                                </div>
                            </c:when>

                            <c:otherwise>
                                <div class="table-responsive">
                                    <table class="table fm-table table-striped align-middle">
                                        <thead>
                                            <tr>
                                                <th>Mã payment</th>
                                                <th>Gói</th>
                                                <th>Số ngày</th>
                                                <th>Số tiền</th>
                                                <th>Phương thức</th>
                                                <th>Trạng thái</th>
                                                <th>Bắt đầu</th>
                                                <th>Kết thúc</th>
                                                <th>Thời gian tạo</th>
                                                <th>Ghi chú</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <c:forEach items="${paymentHistory}" var="p">
                                                <tr>
                                                    <td>
                                                        <span class="fw-semibold">
                                                            <c:out value="${p.paymentCode}" />
                                                        </span>
                                                    </td>
                                                    <td>
                                                        <c:out value="${p.planName}" />
                                                    </td>
                                                    <td>
                                                        <c:out value="${p.planDays}" />
                                                    </td>
                                                    <td>
                                                        <fmt:formatNumber value="${p.amount}" type="number"
                                                            maxFractionDigits="0" /> đ
                                                    </td>
                                                    <td>
                                                        <c:out value="${p.paymentMethod}" />
                                                    </td>
                                                    <td>
                                                        <c:choose>
                                                            <c:when test="${p.paymentStatus eq 'SUCCESS'}">
                                                                <span class="badge text-bg-success">SUCCESS</span>
                                                            </c:when>
                                                            <c:when test="${p.paymentStatus eq 'PENDING'}">
                                                                <span class="badge text-bg-warning">PENDING</span>
                                                            </c:when>
                                                            <c:otherwise>
                                                                <span class="badge text-bg-secondary">
                                                                    <c:out value="${p.paymentStatus}" />
                                                                </span>
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </td>
                                                    <td>
                                                        <c:choose>
                                                            <c:when test="${not empty p.startDate}">
                                                                <c:out value="${p.startDate}" />
                                                            </c:when>
                                                            <c:otherwise>-</c:otherwise>
                                                        </c:choose>
                                                    </td>
                                                    <td>
                                                        <c:choose>
                                                            <c:when test="${not empty p.endDate}">
                                                                <c:out value="${p.endDate}" />
                                                            </c:when>
                                                            <c:otherwise>-</c:otherwise>
                                                        </c:choose>
                                                    </td>
                                                    <td>
                                                        <c:choose>
                                                            <c:when test="${not empty p.createdAt}">
                                                                <c:out value="${p.createdAt}" />
                                                            </c:when>
                                                            <c:otherwise>-</c:otherwise>
                                                        </c:choose>
                                                    </td>
                                                    <td>
                                                        <c:choose>
                                                            <c:when test="${not empty p.note}">
                                                                <c:out value="${p.note}" />
                                                            </c:when>
                                                            <c:otherwise>-</c:otherwise>
                                                        </c:choose>
                                                    </td>
                                                </tr>
                                            </c:forEach>
                                        </tbody>
                                    </table>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>

                    <%--========Tier Change History (NEW)========--%>
                        <div class="fm-surface padded">
                            <div class="d-flex justify-content-between align-items-center mb-3 flex-wrap gap-2">
                                <h5 class="mb-0">
                                    <i class="bi bi-clock-history me-1"></i>
                                    Lịch sử thay đổi tier
                                </h5>
                                <span class="text-muted small">
                                    <c:choose>
                                        <c:when test="${empty tierHistory}">0 thay đổi</c:when>
                                        <c:otherwise>
                                            <c:out value="${tierHistory.size()}" /> thay đổi
                                        </c:otherwise>
                                    </c:choose>
                                </span>
                            </div>

                            <c:choose>
                                <c:when test="${empty tierHistory}">
                                    <div class="alert alert-light border mb-0">
                                        Chưa có lịch sử thay đổi tier cho tài khoản này.
                                    </div>
                                </c:when>

                                <c:otherwise>
                                    <div class="table-responsive">
                                        <table class="table fm-table table-striped align-middle">
                                            <thead>
                                                <tr>
                                                    <th>Thời gian</th>
                                                    <th>Tier cũ</th>
                                                    <th></th>
                                                    <th>Tier mới</th>
                                                    <th>Hạn cũ</th>
                                                    <th>Hạn mới</th>
                                                    <th>Loại thay đổi</th>
                                                    <th>Ghi chú</th>
                                                </tr>
                                            </thead>
                                            <tbody>
                                                <c:forEach items="${tierHistory}" var="h">
                                                    <tr>
                                                        <td>
                                                            <c:out value="${h.createdAt}" />
                                                        </td>
                                                        <td>
                                                            <c:choose>
                                                                <c:when test="${h.oldTier.toString() eq 'PRO'}">
                                                                    <span class="badge text-bg-success">PRO</span>
                                                                </c:when>
                                                                <c:otherwise>
                                                                    <span class="badge text-bg-secondary">FREE</span>
                                                                </c:otherwise>
                                                            </c:choose>
                                                        </td>
                                                        <td><i class="bi bi-arrow-right"></i></td>
                                                        <td>
                                                            <c:choose>
                                                                <c:when test="${h.newTier.toString() eq 'PRO'}">
                                                                    <span class="badge text-bg-success">PRO</span>
                                                                </c:when>
                                                                <c:otherwise>
                                                                    <span class="badge text-bg-secondary">FREE</span>
                                                                </c:otherwise>
                                                            </c:choose>
                                                        </td>
                                                        <td>
                                                            <c:choose>
                                                                <c:when test="${not empty h.oldExpiredDate}">
                                                                    <c:out value="${h.oldExpiredDate}" />
                                                                </c:when>
                                                                <c:otherwise>-</c:otherwise>
                                                            </c:choose>
                                                        </td>
                                                        <td>
                                                            <c:choose>
                                                                <c:when test="${not empty h.newExpiredDate}">
                                                                    <c:out value="${h.newExpiredDate}" />
                                                                </c:when>
                                                                <c:otherwise>-</c:otherwise>
                                                            </c:choose>
                                                        </td>
                                                        <td>
                                                            <c:choose>
                                                                <c:when test="${h.changeType eq 'UPGRADE'}">
                                                                    <span class="badge text-bg-primary">UPGRADE</span>
                                                                </c:when>
                                                                <c:when test="${h.changeType eq 'RENEW'}">
                                                                    <span class="badge text-bg-info">RENEW</span>
                                                                </c:when>
                                                                <c:when test="${h.changeType eq 'EXPIRE'}">
                                                                    <span class="badge text-bg-danger">EXPIRE</span>
                                                                </c:when>
                                                                <c:when test="${h.changeType eq 'ADMIN_GRANT'}">
                                                                    <span class="badge text-bg-warning">ADMIN
                                                                        GRANT</span>
                                                                </c:when>
                                                                <c:when test="${h.changeType eq 'ADMIN_REVOKE'}">
                                                                    <span class="badge text-bg-dark">ADMIN REVOKE</span>
                                                                </c:when>
                                                                <c:otherwise>
                                                                    <span class="badge text-bg-secondary">
                                                                        <c:out value="${h.changeType}" />
                                                                    </span>
                                                                </c:otherwise>
                                                            </c:choose>
                                                        </td>
                                                        <td>
                                                            <c:choose>
                                                                <c:when test="${not empty h.note}">
                                                                    <c:out value="${h.note}" />
                                                                </c:when>
                                                                <c:otherwise>-</c:otherwise>
                                                            </c:choose>
                                                        </td>
                                                    </tr>
                                                </c:forEach>
                                            </tbody>
                                        </table>
                                    </div>
                                </c:otherwise>
                            </c:choose>
                        </div>

                        <jsp:include page="/WEB-INF/jsp/common/footer.jsp" />