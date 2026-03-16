<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    </main><!-- end fm-app-content/fm-page-content -->

    <footer class="fm-bg-surface border-top py-5 mt-auto">
        <div class="container container-xl">
            <div class="row g-4 mb-5">
                <div class="col-lg-4">
                    <div class="fm-navbar-brand fs-4 mb-3 d-inline-block">
                        <i class="bi bi-leaf-fill me-2"></i>FreshMart
                    </div>
                    <p class="fm-text-secondary small pe-lg-5 mb-4">
                        Enterprise grocery operations platform specializing in cold-chain logistics, 
                        real-time inventory management, and source-to-door traceability.
                    </p>
                    <div class="d-flex gap-3">
                        <a href="#" class="btn btn-light btn-sm rounded-circle shadow-sm" style="width: 36px; height: 36px; display: flex; align-items: center; justify-content: center;"><i class="bi bi-facebook opacity-75"></i></a>
                        <a href="#" class="btn btn-light btn-sm rounded-circle shadow-sm" style="width: 36px; height: 36px; display: flex; align-items: center; justify-content: center;"><i class="bi bi-linkedin opacity-75"></i></a>
                        <a href="#" class="btn btn-light btn-sm rounded-circle shadow-sm" style="width: 36px; height: 36px; display: flex; align-items: center; justify-content: center;"><i class="bi bi-twitter-x opacity-75"></i></a>
                    </div>
                </div>
                <div class="col-6 col-md-4 col-lg-2 ms-auto">
                    <h6 class="text-dark fw-bold small text-uppercase ls-wide mb-4">Operations</h6>
                    <ul class="list-unstyled mb-0">
                        <li class="mb-2 small"><a href="${pageContext.request.contextPath}/staff/inventory" class="text-muted text-decoration-none hover-primary">Inventory Hub</a></li>
                        <li class="mb-2 small"><a href="${pageContext.request.contextPath}/staff/suppliers" class="text-muted text-decoration-none hover-primary">Supplier Master</a></li>
                        <li class="mb-2 small"><a href="${pageContext.request.contextPath}/staff/products" class="text-muted text-decoration-none hover-primary">Product Data</a></li>
                        <li class="mb-2 small"><a href="${pageContext.request.contextPath}/staff" class="text-muted text-decoration-none hover-primary">Command Center</a></li>
                    </ul>
                </div>
                <div class="col-6 col-md-4 col-lg-2">
                    <h6 class="text-dark fw-bold small text-uppercase ls-wide mb-4">Platform</h6>
                    <ul class="list-unstyled mb-0">
                        <li class="mb-2 small"><a href="#" class="text-muted text-decoration-none hover-primary">Documentation</a></li>
                        <li class="mb-2 small"><a href="#" class="text-muted text-decoration-none hover-primary">Audit Log</a></li>
                        <li class="mb-2 small"><a href="#" class="text-muted text-decoration-none hover-primary">System Status</a></li>
                        <li class="mb-2 small"><a href="#" class="text-muted text-decoration-none hover-primary">API Console</a></li>
                    </ul>
                </div>
                <div class="col-6 col-md-4 col-lg-2">
                    <h6 class="text-dark fw-bold small text-uppercase ls-wide mb-4">Security</h6>
                    <ul class="list-unstyled mb-0">
                        <li class="mb-2 small"><a href="#" class="text-muted text-decoration-none hover-primary">Privacy Policy</a></li>
                        <li class="mb-2 small"><a href="#" class="text-muted text-decoration-none hover-primary">Terms of Use</a></li>
                        <li class="mb-2 small"><a href="#" class="text-muted text-decoration-none hover-primary">Compliance</a></li>
                    </ul>
                </div>
            </div>
            
            <div class="pt-4 border-top d-flex flex-column flex-md-row justify-content-between align-items-center gap-3">
                <div class="small text-muted">
                    &copy; 2026 FreshMart Global Operations. <span class="d-none d-sm-inline opacity-50 mx-2">|</span> Enterprise Version 4.2.0-LTS
                </div>
                <div class="d-flex align-items-center gap-3">
                    <span class="badge bg-success-subtle text-success small border-0 py-2 px-3 fw-bold"><i class="bi bi-shield-check me-1"></i> SSL SECURED</span>
                    <span class="badge bg-primary-subtle text-primary small border-0 py-2 px-3 fw-bold uppercase">v.2026</span>
                </div>
            </div>
        </div>
    </footer>

    <!-- Core Scripts -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/fm-core.js?v=2"></script>
    <!-- Chatbot Integration -->
    <%@ include file="/WEB-INF/jsp/common/chatbot.jspf" %>
</body>
</html>
