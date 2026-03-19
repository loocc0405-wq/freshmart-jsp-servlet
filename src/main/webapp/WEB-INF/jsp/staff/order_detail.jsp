<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="pageTitle" value="Staff OMS Detail"/>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"/>

<h3>Chi tiết đơn hàng cho Staff</h3>

<c:if test="${not empty successMessage}">
    <div class="alert alert-success"><c:out value="${successMessage}"/></div>
</c:if>

<c:if test="${not empty errorMessage}">
    <div class="alert alert-danger"><c:out value="${errorMessage}"/></div>
</c:if>

<c:if test="${detailView != null}">
    <c:set var="order" value="${detailView.order}"/>

    <div class="row g-3 mb-3">
        <div class="col-md-3">
            <div class="card">
                <div class="card-body">
                    <div class="text-muted">Mã đơn</div>
                    <div><strong><c:out value="${order.orderCode}"/></strong></div>
                </div>
            </div>
        </div>

        <div class="col-md-3">
            <div class="card">
                <div class="card-body">
                    <div class="text-muted">Trạng thái</div>
                    <div>
                        <c:choose>
                            <c:when test="${order.status == 'PENDING'}">
                                <span class="badge bg-secondary">PENDING</span>
                            </c:when>
                            <c:when test="${order.status == 'PROCESSING'}">
                                <span class="badge bg-primary">PROCESSING</span>
                            </c:when>
                            <c:when test="${order.status == 'SHIPPING'}">
                                <span class="badge bg-info text-dark">SHIPPING</span>
                            </c:when>
                            <c:when test="${order.status == 'COMPLETED'}">
                                <span class="badge bg-success">COMPLETED</span>
                            </c:when>
                            <c:when test="${order.status == 'CANCELED'}">
                                <span class="badge bg-danger">CANCELED</span>
                            </c:when>
                            <c:otherwise>
                                <strong><c:out value="${order.status}"/></strong>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </div>
        </div>

        <div class="col-md-3">
            <div class="card">
                <div class="card-body">
                    <div class="text-muted">Loại đơn</div>
                    <div><strong><c:out value="${order.type}"/></strong></div>
                </div>
            </div>
        </div>

        <div class="col-md-3">
            <div class="card">
                <div class="card-body">
                    <div class="text-muted">Tổng tiền</div>
                    <div><strong><c:out value="${order.totalAmount}"/></strong></div>
                </div>
            </div>
        </div>
    </div>

    <div class="alert ${detailView.allFulfillable ? 'alert-success' : 'alert-warning'}">
        <strong>OMS FEFO check:</strong>
        <c:choose>
            <c:when test="${detailView.allFulfillable}">
                Hiện tại đơn đủ tồn để hoàn tất theo FEFO.
            </c:when>
            <c:otherwise>
                Hiện tại có mặt hàng thiếu tồn nếu hoàn tất theo FEFO ngay bây giờ.
            </c:otherwise>
        </c:choose>
        Có <strong><c:out value="${detailView.riskyItemCount}"/></strong> dòng có shortage hoặc dùng lot near-expiry trong ${detailView.nearExpiryWindowDays} ngày.
    </div>

    <c:if test="${order.status != 'PENDING' && order.status != 'COMPLETED'}">
        <div class="alert alert-info">
            Bảng FEFO bên dưới vẫn là <strong>preview theo tồn hiện tại</strong>.
            Khi đơn hoàn tất, hệ thống sẽ lưu trace thật theo từng lot đã xuất.
        </div>
    </c:if>

    <c:if test="${order.status == 'COMPLETED'}">
        <div class="alert alert-success">
            Đơn này đã có <strong>trace lot-level thực tế</strong>. Bảng nào có lịch sử cấp phát sẽ hiển thị đúng lot đã xuất,
            số lượng đã lấy ở từng lot và không còn là preview.
        </div>
    </c:if>

    <!-- ===== ADDED: STATUS WORKFLOW ACTIONS ===== -->
    <div class="card mb-3 border-0 shadow-sm">
        <div class="card-body">
            <h5 class="mb-3">Cập nhật workflow đơn hàng</h5>

            <div class="d-flex flex-wrap gap-2">

                <c:if test="${order.status == 'PENDING'}">
                    <form method="post" action="${pageContext.request.contextPath}/staff/orders/update-status" class="d-inline">
                        <input type="hidden" name="csrf_token" value="${sessionScope.CSRF_TOKEN}" />
                        <input type="hidden" name="id" value="${order.id}" />
                        <input type="hidden" name="status" value="PROCESSING" />
                        <button class="btn btn-primary" type="submit">
                            Chuyển sang PROCESSING
                        </button>
                    </form>

                    <form method="post" action="${pageContext.request.contextPath}/staff/orders/update-status" class="d-inline">
                        <input type="hidden" name="csrf_token" value="${sessionScope.CSRF_TOKEN}" />
                        <input type="hidden" name="id" value="${order.id}" />
                        <input type="hidden" name="status" value="CANCELED" />
                        <button class="btn btn-outline-danger" type="submit">
                            Hủy đơn
                        </button>
                    </form>

                    <form method="post" action="${pageContext.request.contextPath}/staff/orders/complete" class="d-inline">
                        <input type="hidden" name="csrf_token" value="${sessionScope.CSRF_TOKEN}" />
                        <input type="hidden" name="id" value="${order.id}" />
                        <button class="btn btn-success" type="submit"
                                ${!detailView.allFulfillable ? 'disabled="disabled"' : ''}>
                            Hoàn tất đơn theo FEFO
                        </button>
                    </form>
                </c:if>

                <c:if test="${order.status == 'PROCESSING'}">
                    <form method="post" action="${pageContext.request.contextPath}/staff/orders/update-status" class="d-inline">
                        <input type="hidden" name="csrf_token" value="${sessionScope.CSRF_TOKEN}" />
                        <input type="hidden" name="id" value="${order.id}" />
                        <input type="hidden" name="status" value="SHIPPING" />
                        <button class="btn btn-info text-dark" type="submit">
                            Chuyển sang SHIPPING
                        </button>
                    </form>

                    <form method="post" action="${pageContext.request.contextPath}/staff/orders/update-status" class="d-inline">
                        <input type="hidden" name="csrf_token" value="${sessionScope.CSRF_TOKEN}" />
                        <input type="hidden" name="id" value="${order.id}" />
                        <input type="hidden" name="status" value="CANCELED" />
                        <button class="btn btn-outline-danger" type="submit">
                            Hủy đơn
                        </button>
                    </form>
                </c:if>

                <c:if test="${order.status == 'SHIPPING'}">
                    <form method="post" action="${pageContext.request.contextPath}/staff/orders/complete" class="d-inline">
                        <input type="hidden" name="csrf_token" value="${sessionScope.CSRF_TOKEN}" />
                        <input type="hidden" name="id" value="${order.id}" />
                        <button class="btn btn-success" type="submit"
                                ${!detailView.allFulfillable ? 'disabled="disabled"' : ''}>
                            Hoàn tất đơn theo FEFO
                        </button>
                    </form>

                    <form method="post" action="${pageContext.request.contextPath}/staff/orders/update-status" class="d-inline">
                        <input type="hidden" name="csrf_token" value="${sessionScope.CSRF_TOKEN}" />
                        <input type="hidden" name="id" value="${order.id}" />
                        <input type="hidden" name="status" value="CANCELED" />
                        <button class="btn btn-outline-danger" type="submit">
                            Hủy đơn
                        </button>
                    </form>
                </c:if>

                <c:if test="${order.status == 'COMPLETED'}">
                    <span class="badge bg-success fs-6">Đơn đã hoàn tất</span>
                </c:if>

                <c:if test="${order.status == 'CANCELED'}">
                    <span class="badge bg-danger fs-6">Đơn đã bị hủy</span>
                </c:if>

            </div>

            <div class="small text-muted mt-3">
                Workflow: PENDING → PROCESSING → SHIPPING → COMPLETED. Có thể hủy tại các bước mở.
            </div>
        </div>
    </div>
    <!-- ===== END ADDED ===== -->

    <div class="card mb-3">
        <div class="card-header">Đánh giá từng dòng hàng</div>
        <div class="card-body">
            <c:forEach items="${detailView.itemAssessments}" var="line">
                <div class="border rounded p-3 mb-3">
                    <div class="d-flex justify-content-between flex-wrap gap-2 mb-2">
                        <div>
                            <strong><c:out value="${line.productName}"/></strong>
                            <span class="text-muted">(ID: ${line.productId})</span>
                        </div>
                        <div>
                            <span class="badge bg-info text-dark">Yêu cầu: ${line.requestedQty}</span>
                            <span class="badge ${line.enoughStock ? 'bg-success' : 'bg-danger'}">Khả dụng: ${line.availableQty}</span>
                            <span class="badge ${line.usesNearExpiryLots ? 'bg-warning text-dark' : 'bg-secondary'}">Near-expiry: ${line.nearExpiryQty}</span>
                        </div>
                    </div>

                    <div class="small text-muted mb-2">
                        HSD gần nhất: <c:out value="${line.nearestExpiry}"/> |
                        Thiếu tồn: <c:out value="${line.shortageQty}"/>
                    </div>

                    <c:if test="${line.hasActualAllocations}">
                        <div class="alert alert-light border small mb-3">
                            Đây là lịch sử xuất kho thực tế của order item #${line.orderItemId}. Tổng đã cấp phát: <strong>${line.actualAllocatedQty}</strong>.
                        </div>
                        <table class="table table-sm align-middle mb-0">
                            <thead>
                            <tr>
                                <th>Lot ID</th>
                                <th>Ngày nhập</th>
                                <th>HSD</th>
                                <th>Đã lấy thực tế</th>
                                <th>Ghi nhận lúc</th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:forEach items="${line.actualAllocations}" var="alloc">
                                <tr>
                                    <td><c:out value="${alloc.productLot.id}"/></td>
                                    <td><c:out value="${alloc.productLot.importDate}"/></td>
                                    <td><c:out value="${alloc.productLot.expiryDate}"/></td>
                                    <td><strong><c:out value="${alloc.allocatedQty}"/></strong></td>
                                    <td><c:out value="${alloc.createdAt}"/></td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </c:if>

                    <c:if test="${not line.hasActualAllocations and empty line.plan.allocations}">
                        <div class="text-muted">Không có lot khả dụng để phân bổ FEFO.</div>
                    </c:if>

                    <c:if test="${not line.hasActualAllocations and not empty line.plan.allocations}">
                        <div class="small text-muted mb-2">Đây là preview FEFO theo tồn hiện tại, chưa phải trace thực tế.</div>
                        <table class="table table-sm align-middle mb-0">
                            <thead>
                            <tr>
                                <th>Lot ID</th>
                                <th>Ngày nhập</th>
                                <th>HSD</th>
                                <th>Còn trước cấp phát</th>
                                <th>FEFO dự kiến lấy</th>
                                <th>Cảnh báo</th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:forEach items="${line.plan.allocations}" var="alloc">
                                <tr class="${alloc.nearExpiry ? 'table-warning' : ''}">
                                    <td><c:out value="${alloc.lotId}"/></td>
                                    <td><c:out value="${alloc.importDate}"/></td>
                                    <td><c:out value="${alloc.expiryDate}"/></td>
                                    <td><c:out value="${alloc.qtyLeftBefore}"/></td>
                                    <td><strong><c:out value="${alloc.allocatedQty}"/></strong></td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${alloc.nearExpiry}">
                                                <span class="badge bg-warning text-dark">
                                                    Ưu tiên bán (${alloc.daysUntilExpiry} ngày)
                                                </span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge bg-secondary">Bình thường</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </c:if>
                </div>
            </c:forEach>
        </div>
    </div>

    <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/staff/orders">
        Quay lại danh sách đơn
    </a>
</c:if>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>