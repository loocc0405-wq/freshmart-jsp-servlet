<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    </main><!-- end fm-page-content -->

    <footer class="fm-bg-surface border-top py-5 mt-auto">
        <div class="container">
            <div class="row g-4">
                <div class="col-lg-4">
                    <div class="fm-h3 text-primary mb-3"><i class="bi bi-leaf-fill me-2 text-success"></i>FreshMart</div>
                    <p class="fm-text-secondary small">
                        Enterprise-grade grocery operations and fulfillment platform. 
                        Reliable cold-chain logistics and fresh produce management.
                    </p>
                    <div class="d-flex gap-3 fs-5 text-muted">
                        <i class="bi bi-facebook"></i>
                        <i class="bi bi-linkedin"></i>
                        <i class="bi bi-twitter-x"></i>
                    </div>
                </div>
                <div class="col-6 col-lg-2 ms-auto">
                    <h6 class="text-uppercase fw-bold small mb-3">Operations</h6>
                    <ul class="list-unstyled small">
                        <li class="mb-2"><a href="${pageContext.request.contextPath}/staff/inventory" class="text-muted">Inventory Control</a></li>
                        <li class="mb-2"><a href="${pageContext.request.contextPath}/staff/suppliers" class="text-muted">Suppliers</a></li>
                        <li class="mb-2"><a href="${pageContext.request.contextPath}/staff/report" class="text-muted">Analytics</a></li>
                    </ul>
                </div>
                <div class="col-6 col-lg-2">
                    <h6 class="text-uppercase fw-bold small mb-3">Company</h6>
                    <ul class="list-unstyled small">
                        <li class="mb-2"><a href="#" class="text-muted">About Us</a></li>
                        <li class="mb-2"><a href="#" class="text-muted">Compliance</a></li>
                        <li class="mb-2"><a href="#" class="text-muted">Support</a></li>
                    </ul>
                </div>
            </div>
            <hr class="my-4 opacity-50">
            <div class="d-flex flex-wrap justify-content-between align-items-center gap-3">
                <p class="mb-0 small text-muted">&copy; 2026 FreshMart Global. All rights reserved.</p>
                <div class="d-flex gap-4 small text-muted">
                    <span>Privacy Policy</span>
                    <span>Terms of Service</span>
                </div>
            </div>
        </div>
    </footer>

    <!-- Bootstrap Bundle -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
    <!-- FM Core Engine -->
    <script src="${pageContext.request.contextPath}/assets/js/fm-core.js?v=1"></script>
</body>
</html>
