<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="pageTitle" value="Suppliers" />
<jsp:include page="/WEB-INF/jsp/common/header.jsp"/>

<!-- statistics row -->
<div class="row mb-4">
  <div class="col-md-3 mb-2">
    <div class="card text-white bg-primary h-100">
      <div class="card-body">
        <h6 class="card-title">Total suppliers</h6>
        <p class="card-text fs-2">${statsTotal}</p>
      </div>
    </div>
  </div>
  <div class="col-md-3 mb-2">
    <div class="card text-white bg-success h-100">
      <div class="card-body">
        <h6 class="card-title">With certificate</h6>
        <p class="card-text fs-2">${statsWithCert}</p>
      </div>
    </div>
  </div>
  <div class="col-md-3 mb-2">
    <div class="card text-white bg-warning h-100">
      <div class="card-body">
        <h6 class="card-title">Without certificate</h6>
        <p class="card-text fs-2">${statsWithoutCert}</p>
      </div>
    </div>
  </div>
  <div class="col-md-3 mb-2">
    <div class="card text-white bg-info h-100">
      <div class="card-body">
        <h6 class="card-title">Avg lead time (days)</h6>
        <p class="card-text fs-2">${statsAvgLead}</p>
      </div>
    </div>
  </div>
</div>

<!-- charts row -->
<div class="row mb-4">
  <!-- certificate status bar chart -->
  <div class="col-md-6 mb-3">
    <div class="card h-100">
      <div class="card-header">Certificate Status</div>
      <div class="card-body">
        <canvas id="certChart"></canvas>
      </div>
    </div>
  </div>
  <!-- top suppliers chart (only when data available) -->
  <c:if test="${not empty topSuppliers}">
    <div class="col-md-6 mb-3">
      <div class="card h-100">
        <div class="card-header">Top Suppliers (by product count)</div>
        <div class="card-body">
          <canvas id="topChart"></canvas>
        </div>
      </div>
    </div>
  </c:if>
</div>

<!-- top suppliers table (optional) -->
<c:if test="${not empty topSuppliers}">
  <div class="card mb-4">
    <div class="card-header">Top suppliers by distinct products supplied</div>
    <div class="card-body p-0">
      <table class="table table-sm mb-0">
        <thead class="table-light">
          <tr>
            <th>Supplier</th>
            <th class="text-end"># Products</th>
          </tr>
        </thead>
        <tbody>
          <c:forEach var="tp" items="${topSuppliers}">
            <tr>
              <td>${fn:escapeXml(tp.supplier.name)}</td>
              <td class="text-end">${tp.productCount}</td>
            </tr>
          </c:forEach>
        </tbody>
      </table>
    </div>
  </div>
</c:if>

<!-- Flash Messages -->
<c:if test="${not empty sessionScope.successMessage}">
  <div class="alert alert-success alert-dismissible fade show" role="alert">
    <i class="bi bi-check-circle me-2"></i>${sessionScope.successMessage}
    <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
  </div>
  <c:set var="temp" value="${sessionScope.remove('successMessage')}" />
</c:if>

<c:if test="${not empty sessionScope.errorMessage}">
  <div class="alert alert-danger alert-dismissible fade show" role="alert">
    <i class="bi bi-exclamation-circle me-2"></i>
    <span>${sessionScope.errorMessage}</span>
    <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
  </div>
  <c:set var="temp" value="${sessionScope.remove('errorMessage')}" />
</c:if>

<div class="d-flex justify-content-between align-items-center mb-3">
  <div>
    <h3 class="mb-0">Suppliers</h3>
    <div class="text-muted">Manage supplier info</div>
  </div>
  <div>
    <button class="btn btn-success me-2" data-bs-toggle="modal" data-bs-target="#importModal">
      <i class="bi bi-upload"></i> Import CSV
    </button>
    <a class="btn btn-primary" href="${pageContext.request.contextPath}/staff/suppliers?action=create">
      + Add Supplier
    </a>
  </div>
</div>

<!-- Import Modal -->
<div class="modal fade" id="importModal" tabindex="-1" aria-labelledby="importModalLabel" aria-hidden="true">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title" id="importModalLabel">Import Suppliers from CSV</h5>
        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
      </div>
      <form method="post" action="${pageContext.request.contextPath}/staff/suppliers" enctype="multipart/form-data">
        <input type="hidden" name="csrf_token" value="${sessionScope.CSRF_TOKEN}" />
        <input type="hidden" name="action" value="import" />
        <div class="modal-body">
          <div class="mb-3">
            <label for="csvFile" class="form-label">Select CSV File (UTF-8)</label>
            <input type="file" class="form-control" id="csvFile" name="csvFile" accept=".csv" required />
            <div class="form-text">
              CSV format: name,email,phone,address,certificate,leadTimeDays,note<br/>
              Example: "ABC Supplier,abc@example.com,0123456789,123 Street,ISO9001,3,Good supplier"
            </div>
          </div>
          <div class="alert alert-info mb-0">
            <small>
              <strong>Notes:</strong><br/>
              - First line is header (will be skipped)<br/>
              - Email is used to detect duplicates (update if exists)<br/>
              - Required fields: name, email, phone<br/>
              - leadTimeDays must be positive number (default: 1)
            </small>
          </div>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
          <button type="submit" class="btn btn-success">Upload & Import</button>
        </div>
      </form>
    </div>
  </div>
</div>

<!-- search / filter form -->
<form method="get" action="" class="row g-2 mb-3">
  <div class="col-md-3">
    <input type="text" class="form-control" name="q" placeholder="Search name, email or phone"
           value="${fn:escapeXml(search)}" />
  </div>
  <div class="col-md-2">
    <input type="text" class="form-control" name="certificate" placeholder="Certificate"
           value="${fn:escapeXml(certificateFilter)}" />
  </div>
  <div class="col-md-2">
    <input type="date" class="form-control" name="fromDate" placeholder="From date"
           value="${fn:escapeXml(fromDate)}" />
  </div>
  <div class="col-md-2">
    <input type="date" class="form-control" name="toDate" placeholder="To date"
           value="${fn:escapeXml(toDate)}" />
  </div>
  <div class="col-auto">
    <button class="btn btn-outline-secondary" type="submit">Search</button>
    <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/staff/suppliers">Reset</a>
  </div>
</form>

<div class="card">
  <div class="card-body">
    <div class="table-responsive">
      <table class="table table-hover align-middle">
        <thead class="table-light">
          <tr>
            <th>ID</th>
            <th>Name</th>
            <th>Phone</th>
            <th>Email</th>
            <th>Address</th>
            <th class="text-end">Action</th>
          </tr>
        </thead>
        <tbody>
        <c:forEach var="s" items="${suppliers}">
          <tr>
            <td class="text-muted">${s.id}</td>
            <td class="fw-semibold">${s.name}</td>
            <td>${empty s.phone ? "-" : s.phone}</td>
            <td>${empty s.email ? "-" : s.email}</td>
            <td>${empty s.address ? "-" : s.address}</td>
            <td class="text-end">
              <a class="btn btn-sm btn-outline-secondary"
                 href="${pageContext.request.contextPath}/staff/suppliers?action=edit&id=${s.id}">
                Edit
              </a>

              <!-- Delete bằng POST cho chuyên nghiệp -->
              <form class="d-inline"
                    action="${pageContext.request.contextPath}/staff/suppliers"
                    method="post"
                    onsubmit="return confirm('Delete this supplier?');">
                <!-- csrf token required for POST -->
                <input type="hidden" name="csrf_token" value="${sessionScope.CSRF_TOKEN}" />
                <input type="hidden" name="action" value="delete">
                <input type="hidden" name="id" value="${s.id}">
                <button class="btn btn-sm btn-outline-danger" type="submit">Delete</button>
              </form>
            </td>
          </tr>
        </c:forEach>

        <c:if test="${empty suppliers}">
          <tr>
            <td colspan="6" class="text-center text-muted py-4">No suppliers found.</td>
          </tr>
        </c:if>
        </tbody>
      </table>
    </div>

    <!-- pagination -->
    <c:if test="${totalPages > 1}">
      <nav aria-label="Page navigation">
        <ul class="pagination justify-content-center mt-3">
          <li class="page-item ${currentPage == 1 ? 'disabled' : ''}">
            <c:url var="prevUrl" value="/staff/suppliers">
              <c:param name="page" value="${currentPage - 1}" />
              <c:if test="${not empty search}"><c:param name="q" value="${search}"/></c:if>
              <c:if test="${not empty certificateFilter}"><c:param name="certificate" value="${certificateFilter}"/></c:if>
              <c:if test="${not empty fromDate}"><c:param name="fromDate" value="${fromDate}"/></c:if>
              <c:if test="${not empty toDate}"><c:param name="toDate" value="${toDate}"/></c:if>
            </c:url>
            <a class="page-link" href="${prevUrl}" aria-label="Previous">
              <span aria-hidden="true">&laquo;</span>
            </a>
          </li>
          <c:forEach begin="1" end="${totalPages}" var="i">
            <li class="page-item ${i == currentPage ? 'active' : ''}">
              <c:url var="linkUrl" value="/staff/suppliers">
                <c:param name="page" value="${i}" />
                <c:if test="${not empty search}"><c:param name="q" value="${search}"/></c:if>
                <c:if test="${not empty certificateFilter}"><c:param name="certificate" value="${certificateFilter}"/></c:if>
                <c:if test="${not empty fromDate}"><c:param name="fromDate" value="${fromDate}"/></c:if>
                <c:if test="${not empty toDate}"><c:param name="toDate" value="${toDate}"/></c:if>
              </c:url>
              <a class="page-link" href="${linkUrl}">${i}</a>
            </li>
          </c:forEach>
          <li class="page-item ${currentPage == totalPages ? 'disabled' : ''}">
            <c:url var="nextUrl" value="/staff/suppliers">
              <c:param name="page" value="${currentPage + 1}" />
              <c:if test="${not empty search}"><c:param name="q" value="${search}"/></c:if>
              <c:if test="${not empty certificateFilter}"><c:param name="certificate" value="${certificateFilter}"/></c:if>
              <c:if test="${not empty fromDate}"><c:param name="fromDate" value="${fromDate}"/></c:if>
              <c:if test="${not empty toDate}"><c:param name="toDate" value="${toDate}"/></c:if>
            </c:url>
            <a class="page-link" href="${nextUrl}" aria-label="Next">
              <span aria-hidden="true">&raquo;</span>
            </a>
          </li>
        </ul>
      </nav>
    </c:if>

  </div>
</div>
</div>


<!-- load Chart.js only on this page -->
<script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
<script>
  // certificate status chart
  (function() {
    var ctx = document.getElementById('certChart');
    if (ctx) {
      new Chart(ctx, {
        type: 'bar',
        data: {
          labels: ['With certificate', 'Without certificate'],
          datasets: [{
            label: 'Suppliers',
            data: [${statsWithCert}, ${statsWithoutCert}],
            backgroundColor: ['#198754', '#ffc107']
          }]
        },
        options: {
          responsive: true,
          scales: {
            y: { beginAtZero: true, precision:0 }
          }
        }
      });
    }
  })();

  // top suppliers bar chart
  (function() {
    var ctx = document.getElementById('topChart');
    if (ctx) {
      var labels = [
        <c:forEach var="tp" items="${topSuppliers}" varStatus="loop">
          "${fn:escapeXml(tp.supplier.name)}"<c:if test="${!loop.last}">,</c:if>
        </c:forEach>
      ];
      var data = [
        <c:forEach var="tp" items="${topSuppliers}" varStatus="loop">
          ${tp.productCount}<c:if test="${!loop.last}">,</c:if>
        </c:forEach>
      ];
      new Chart(ctx, {
        type: 'bar',
        data: {
          labels: labels,
          datasets: [{
            label: '# Products',
            data: data,
            backgroundColor: '#0d6efd'
          }]
        },
        options: {
          responsive: true,
          scales: {
            y: { beginAtZero: true, precision:0 }
          }
        }
      });
    }
  })();
</script>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>