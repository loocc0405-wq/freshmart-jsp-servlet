<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <%@ taglib prefix="c" uri="jakarta.tags.core" %>
        <%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

            <c:set var="pageTitle" value="Admin - Subscriptions" />
            <jsp:include page="/WEB-INF/jsp/common/header.jsp" />

            <div class="fm-page-header">
                <div>
                    <h1 class="fm-page-title">Admin Subscription Management</h1>
                    <p class="fm-page-subtitle">
                        Quản lý user FREE/PRO, cấp/thu hồi thủ công, xem payment history, tier history và cấu hình hệ
                        thống.
                    </p>
                </div>
            </div>

            <c:if test="${not empty successMessage}">
                <div class="alert alert-success">
                    <i class="bi bi-check-circle me-1"></i>
                    <c:out value="${successMessage}" />
                </div>
            </c:if>

            <c:if test="${not empty errorMessage}">
                <div class="alert alert-danger">
                    <i class="bi bi-exclamation-triangle me-1"></i>
                    <c:out value="${errorMessage}" />
                </div>
            </c:if>

            <%--========Grant / Revoke PRO + System Config========--%>
                <div class="row g-4 mb-4">
                    <div class="col-lg-5">
                        <div class="fm-surface padded h-100">
                            <h5 class="mb-3">Cấp / gia hạn PRO cho user</h5>

                            <form method="post" action="${pageContext.request.contextPath}/admin/subscriptions"
                                class="row g-3">
                                <input type="hidden" name="csrf_token" value="${sessionScope.CSRF_TOKEN}" />
                                <input type="hidden" name="action" value="grantPro" />

                                <div class="col-12">
                                    <label class="form-label">Chọn customer</label>
                                    <select class="form-select" name="userId" required>
                                        <option value="">-- Chọn user --</option>
                                        <c:forEach items="${users}" var="u">
                                            <option value="${u.id}">
                                                <c:out value="${u.username}" />
                                                <c:if test="${not empty u.fullName}"> -
                                                    <c:out value="${u.fullName}" />
                                                </c:if>
                                                [
                                                <c:out value="${u.tier}" />]
                                                <c:if test="${not empty u.expiredDate}"> - exp:
                                                    <c:out value="${u.expiredDate}" />
                                                </c:if>
                                            </option>
                                        </c:forEach>
                                    </select>
                                </div>

                                <div class="col-md-6">
                                    <label class="form-label">Số ngày cộng thêm</label>
                                    <input class="form-control" type="number" name="days" min="1" value="30" required />
                                </div>

                                <div class="col-md-6">
                                    <label class="form-label">Gói tham chiếu</label>
                                    <select class="form-select" onchange="this.form.days.value=this.value;">
                                        <c:forEach items="${planPrices}" var="plan">
                                            <option value="${plan.key}">
                                                ${plan.key} ngày -
                                                <fmt:formatNumber value="${plan.value}" type="number"
                                                    maxFractionDigits="0" /> đ
                                            </option>
                                        </c:forEach>
                                    </select>
                                </div>

                                <div class="col-12">
                                    <label class="form-label">Ghi chú</label>
                                    <textarea class="form-control" name="note" rows="3"
                                        placeholder="Ví dụ: Admin grant PRO để demo hoặc hỗ trợ khách hàng"></textarea>
                                </div>

                                <div class="col-12">
                                    <button class="btn btn-primary" type="submit">
                                        <i class="bi bi-lightning-charge me-1"></i>
                                        Cấp / gia hạn PRO
                                    </button>
                                </div>
                            </form>

                            <hr class="fm-divider" />

                            <%-- Revoke PRO --%>
                                <h5 class="mb-3">Thu hồi PRO</h5>
                                <form method="post" action="${pageContext.request.contextPath}/admin/subscriptions"
                                    class="row g-3">
                                    <input type="hidden" name="csrf_token" value="${sessionScope.CSRF_TOKEN}" />
                                    <input type="hidden" name="action" value="revokePro" />

                                    <div class="col-12">
                                        <label class="form-label">Chọn user PRO cần thu hồi</label>
                                        <select class="form-select" name="userId" required>
                                            <option value="">-- Chọn user --</option>
                                            <c:forEach items="${users}" var="u">
                                                <c:if test="${u.tier.toString() eq 'PRO'}">
                                                    <option value="${u.id}">
                                                        <c:out value="${u.username}" />
                                                        <c:if test="${not empty u.fullName}"> -
                                                            <c:out value="${u.fullName}" />
                                                        </c:if>
                                                        [PRO - exp:
                                                        <c:out value="${u.expiredDate}" />]
                                                    </option>
                                                </c:if>
                                            </c:forEach>
                                        </select>
                                    </div>

                                    <div class="col-12">
                                        <label class="form-label">Lý do thu hồi</label>
                                        <input class="form-control" type="text" name="note"
                                            placeholder="Ví dụ: Vi phạm chính sách" />
                                    </div>

                                    <div class="col-12">
                                        <button class="btn btn-danger" type="submit"
                                            onclick="return confirm('Bạn có chắc muốn thu hồi PRO không?');">
                                            <i class="bi bi-x-circle me-1"></i>
                                            Thu hồi PRO
                                        </button>
                                    </div>
                                </form>

                                <hr class="fm-divider" />

                                <h6 class="mb-2">Bảng giá tham chiếu</h6>
                                <div class="row g-2">
                                    <c:forEach items="${planPrices}" var="plan">
                                        <div class="col-md-4">
                                            <div class="border rounded-3 p-3 text-center bg-light h-100">
                                                <div class="fw-bold">${plan.key} ngày</div>
                                                <div class="text-success fw-semibold">
                                                    <fmt:formatNumber value="${plan.value}" type="number"
                                                        maxFractionDigits="0" /> đ
                                                </div>
                                            </div>
                                        </div>
                                    </c:forEach>
                                </div>
                        </div>
                    </div>

                    <div class="col-lg-7">
                        <div class="fm-surface padded h-100">
                            <h5 class="mb-3">Cấu hình hệ thống</h5>

                            <form method="post" action="${pageContext.request.contextPath}/admin/subscriptions"
                                class="row g-3">
                                <input type="hidden" name="csrf_token" value="${sessionScope.CSRF_TOKEN}" />
                                <input type="hidden" name="action" value="saveSettings" />

                                <div class="col-12">
                                    <h6 class="text-muted mb-0">Inventory</h6>
                                </div>

                                <div class="col-md-6">
                                    <label class="form-label">Low stock threshold</label>
                                    <input class="form-control" type="number" min="0" name="lowStockThreshold"
                                        value="${settings['low_stock_threshold']}" required />
                                    <div class="form-text">Ngưỡng cảnh báo tồn kho thấp.</div>
                                </div>

                                <div class="col-md-6">
                                    <label class="form-label">Upcoming expiry days</label>
                                    <input class="form-control" type="number" min="0" name="upcomingExpiryDays"
                                        value="${settings['upcoming_expiry_days']}" required />
                                    <div class="form-text">Số ngày để cảnh báo cận hạn.</div>
                                </div>

                                <div class="col-md-4">
                                    <label class="form-label">History days</label>
                                    <input class="form-control" type="number" min="1" name="replenishHistoryDays"
                                        value="${settings['replenish_history_days']}" required />
                                </div>

                                <div class="col-md-4">
                                    <label class="form-label">Lead days</label>
                                    <input class="form-control" type="number" min="0" name="replenishLeadDays"
                                        value="${settings['replenish_lead_days']}" required />
                                </div>

                                <div class="col-md-4">
                                    <label class="form-label">Buffer days</label>
                                    <input class="form-control" type="number" min="0" name="replenishBufferDays"
                                        value="${settings['replenish_buffer_days']}" required />
                                </div>

                                <div class="col-md-4">
                                    <label class="form-label">Safety days</label>
                                    <input class="form-control" type="number" min="0" name="replenishSafetyDays"
                                        value="${settings['replenish_safety_days']}" required />
                                </div>

                                <div class="col-12">
                                    <hr class="fm-divider" />
                                    <h6 class="text-muted mb-0">Subscription</h6>
                                </div>

                                <div class="col-md-6">
                                    <label class="form-label">Cảnh báo trước hết hạn (ngày)</label>
                                    <input class="form-control" type="number" min="0" name="subNotifyDays"
                                        value="${settings['subscription_notify_days']}" required />
                                    <div class="form-text">Hiển thị cảnh báo khi gói PRO sắp hết hạn.</div>
                                </div>

                                <div class="col-md-6">
                                    <label class="form-label">Grace period (ngày)</label>
                                    <input class="form-control" type="number" min="0" name="subGracePeriodDays"
                                        value="${settings['subscription_grace_period_days']}" required />
                                    <div class="form-text">Số ngày gia hạn thêm sau khi hết hạn.</div>
                                </div>

                                <div class="col-12">
                                    <button class="btn btn-primary" type="submit">
                                        <i class="bi bi-save me-1"></i>
                                        Lưu cấu hình
                                    </button>
                                </div>
                            </form>

                            <hr class="fm-divider" />

                            <div class="alert alert-info mb-0">
                                Các cấu hình này được dùng cho:
                                <b>Inventory report</b> (ngưỡng tồn kho thấp, cảnh báo cận hạn),
                                <b>PRO replenishment</b> (history / lead / buffer / safety)
                                và <b>Subscription</b> (cảnh báo hết hạn, grace period).
                            </div>
                        </div>
                    </div>
                </div>

                <%--========User List with Search========--%>
                    <div class="fm-surface padded mb-4">
                        <div class="d-flex justify-content-between align-items-center mb-3">
                            <h5 class="mb-0">Danh sách customer subscription</h5>
                            <div class="d-flex gap-2 align-items-center">
                                <input type="text" id="userSearchInput" class="form-control form-control-sm"
                                    style="width: 220px;" placeholder="Tìm username / tên..."
                                    onkeyup="filterUserTable()" />
                                <span class="text-muted small">
                                    <c:choose>
                                        <c:when test="${empty users}">0 users</c:when>
                                        <c:otherwise>${users.size()} users</c:otherwise>
                                    </c:choose>
                                </span>
                            </div>
                        </div>

                        <c:choose>
                            <c:when test="${empty users}">
                                <div class="alert alert-light border mb-0">Chưa có customer nào.</div>
                            </c:when>
                            <c:otherwise>
                                <div class="table-responsive">
                                    <table class="table fm-table table-striped align-middle" id="userTable">
                                        <thead>
                                            <tr>
                                                <th>ID</th>
                                                <th>Username</th>
                                                <th>Full name</th>
                                                <th>Tier</th>
                                                <th>Expired date</th>
                                                <th>Remaining days</th>
                                                <th>Status</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <c:forEach items="${users}" var="u">
                                                <tr>
                                                    <td>
                                                        <c:out value="${u.id}" />
                                                    </td>
                                                    <td>
                                                        <c:out value="${u.username}" />
                                                    </td>
                                                    <td>
                                                        <c:choose>
                                                            <c:when test="${not empty u.fullName}">
                                                                <c:out value="${u.fullName}" />
                                                            </c:when>
                                                            <c:otherwise>-</c:otherwise>
                                                        </c:choose>
                                                    </td>
                                                    <td>
                                                        <c:choose>
                                                            <c:when test="${u.tier.toString() eq 'PRO'}">
                                                                <span class="badge text-bg-success">PRO</span>
                                                            </c:when>
                                                            <c:otherwise>
                                                                <span class="badge text-bg-secondary">FREE</span>
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </td>
                                                    <td>
                                                        <c:choose>
                                                            <c:when test="${not empty u.expiredDate}">
                                                                <c:out value="${u.expiredDate}" />
                                                            </c:when>
                                                            <c:otherwise>-</c:otherwise>
                                                        </c:choose>
                                                    </td>
                                                    <td>
                                                        <c:choose>
                                                            <c:when test="${u.proActive}">
                                                                ${u.remainingProDays}
                                                            </c:when>
                                                            <c:otherwise>0</c:otherwise>
                                                        </c:choose>
                                                    </td>
                                                    <td>
                                                        <c:choose>
                                                            <c:when test="${u.proActive}">
                                                                <span class="text-success fw-semibold">Active</span>
                                                            </c:when>
                                                            <c:when test="${u.tier.toString() eq 'PRO'}">
                                                                <span class="text-danger fw-semibold">Expired</span>
                                                            </c:when>
                                                            <c:otherwise>
                                                                <span class="text-muted">FREE</span>
                                                            </c:otherwise>
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

                    <%--========Payment History========--%>
                        <div class="fm-surface padded mb-4">
                            <div class="d-flex justify-content-between align-items-center mb-3">
                                <h5 class="mb-0">Subscription payment history</h5>
                                <span class="text-muted small">
                                    <c:choose>
                                        <c:when test="${empty payments}">0 payments</c:when>
                                        <c:otherwise>${payments.size()} payments</c:otherwise>
                                    </c:choose>
                                </span>
                            </div>

                            <c:choose>
                                <c:when test="${empty payments}">
                                    <div class="alert alert-light border mb-0">Chưa có payment history.</div>
                                </c:when>
                                <c:otherwise>
                                    <div class="table-responsive">
                                        <table class="table fm-table table-striped align-middle">
                                            <thead>
                                                <tr>
                                                    <th>ID</th>
                                                    <th>Payment code</th>
                                                    <th>User</th>
                                                    <th>Plan</th>
                                                    <th>Days</th>
                                                    <th>Amount</th>
                                                    <th>Method</th>
                                                    <th>Status</th>
                                                    <th>Start</th>
                                                    <th>End</th>
                                                    <th>Created at</th>
                                                    <th>Note</th>
                                                </tr>
                                            </thead>
                                            <tbody>
                                                <c:forEach items="${payments}" var="p">
                                                    <tr>
                                                        <td>
                                                            <c:out value="${p.id}" />
                                                        </td>
                                                        <td><span class="fw-semibold">
                                                                <c:out value="${p.paymentCode}" />
                                                            </span></td>
                                                        <td>
                                                            <c:out value="${p.user.username}" />
                                                            <c:if test="${not empty p.user.fullName}">
                                                                <br />
                                                                <span class="text-muted small">
                                                                    <c:out value="${p.user.fullName}" />
                                                                </span>
                                                            </c:if>
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
                                                                <c:otherwise>
                                                                    <span class="badge text-bg-secondary">
                                                                        <c:out value="${p.paymentStatus}" />
                                                                    </span>
                                                                </c:otherwise>
                                                            </c:choose>
                                                        </td>
                                                        <td>
                                                            <c:out value="${p.startDate}" />
                                                        </td>
                                                        <td>
                                                            <c:out value="${p.endDate}" />
                                                        </td>
                                                        <td>
                                                            <c:out value="${p.createdAt}" />
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
                                <div class="d-flex justify-content-between align-items-center mb-3">
                                    <h5 class="mb-0">
                                        <i class="bi bi-clock-history me-1"></i>
                                        Lịch sử thay đổi tier (tất cả user)
                                    </h5>
                                    <span class="text-muted small">
                                        <c:choose>
                                            <c:when test="${empty tierHistory}">0 thay đổi</c:when>
                                            <c:otherwise>${tierHistory.size()} thay đổi</c:otherwise>
                                        </c:choose>
                                    </span>
                                </div>

                                <c:choose>
                                    <c:when test="${empty tierHistory}">
                                        <div class="alert alert-light border mb-0">Chưa có tier history.</div>
                                    </c:when>
                                    <c:otherwise>
                                        <div class="table-responsive">
                                            <table class="table fm-table table-striped align-middle">
                                                <thead>
                                                    <tr>
                                                        <th>ID</th>
                                                        <th>User</th>
                                                        <th>Tier cũ</th>
                                                        <th></th>
                                                        <th>Tier mới</th>
                                                        <th>Hạn cũ</th>
                                                        <th>Hạn mới</th>
                                                        <th>Loại</th>
                                                        <th>Ghi chú</th>
                                                        <th>Thời gian</th>
                                                    </tr>
                                                </thead>
                                                <tbody>
                                                    <c:forEach items="${tierHistory}" var="h">
                                                        <tr>
                                                            <td>
                                                                <c:out value="${h.id}" />
                                                            </td>
                                                            <td>
                                                                <c:out value="${h.user.username}" />
                                                                <c:if test="${not empty h.user.fullName}">
                                                                    <br />
                                                                    <span class="text-muted small">
                                                                        <c:out value="${h.user.fullName}" />
                                                                    </span>
                                                                </c:if>
                                                            </td>
                                                            <td>
                                                                <c:choose>
                                                                    <c:when test="${h.oldTier.toString() eq 'PRO'}">
                                                                        <span class="badge text-bg-success">PRO</span>
                                                                    </c:when>
                                                                    <c:otherwise>
                                                                        <span
                                                                            class="badge text-bg-secondary">FREE</span>
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
                                                                        <span
                                                                            class="badge text-bg-secondary">FREE</span>
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
                                                                        <span
                                                                            class="badge text-bg-primary">UPGRADE</span>
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
                                                                        <span class="badge text-bg-dark">ADMIN
                                                                            REVOKE</span>
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
                                                            <td>
                                                                <c:out value="${h.createdAt}" />
                                                            </td>
                                                        </tr>
                                                    </c:forEach>
                                                </tbody>
                                            </table>
                                        </div>
                                    </c:otherwise>
                                </c:choose>
                            </div>

                            <%--========Search JS========--%>
                                <script>
                                    function filterUserTable() {
                                        var input = document.getElementById('userSearchInput');
                                        if (!input) return;
                                        var filter = input.value.toUpperCase();
                                        var table = document.getElementById('userTable');
                                        if (!table) return;
                                        var rows = table.getElementsByTagName('tbody')[0].getElementsByTagName('tr');
                                        for (var i = 0; i < rows.length; i++) {
                                            var cells = rows[i].getElementsByTagName('td');
                                            var match = false;
                                            for (var j = 0; j < cells.length; j++) {
                                                if (cells[j].textContent.toUpperCase().indexOf(filter) > -1) {
                                                    match = true;
                                                    break;
                                                }
                                            }
                                            rows[i].style.display = match ? '' : 'none';
                                        }
                                    }
                                </script>

                                <jsp:include page="/WEB-INF/jsp/common/footer.jsp" />