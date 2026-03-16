/**
 * FM Core JS - Lightweight UI enhancements for FreshMart
 * Purpose: Enterprise interactions without client-side rendering
 */
(function() {
    'use strict';

    // Password Visibility Toggle
    const initPasswordToggles = () => {
        document.querySelectorAll('[data-fm-toggle="password"]').forEach(btn => {
            btn.addEventListener('click', function() {
                const targetId = this.dataset.fmTarget;
                const target = document.getElementById(targetId);
                if (!target) return;

                const isPassword = target.type === 'password';
                target.type = isPassword ? 'text' : 'password';
                
                const icon = this.querySelector('i');
                if (icon) {
                    icon.className = isPassword ? 'bi bi-eye-slash-fill' : 'bi bi-eye-fill';
                }
            });
        });
    };

    // Sidebar & Topbar Toggle (Mobile Navigation)
    const initNavToggles = () => {
        const sidebarToggle = document.querySelector('[data-fm-toggle="sidebar"]');
        const sidebar = document.querySelector('.fm-sidebar');
        if (sidebarToggle && sidebar) {
            sidebarToggle.addEventListener('click', () => {
                sidebar.classList.toggle('collapsed');
                sidebar.classList.toggle('active'); // For mobile mobile
            });
        }

        const topbarToggle = document.querySelector('[data-fm-toggle="search"]');
        if (topbarToggle) {
            topbarToggle.addEventListener('click', function() {
                const searchBar = document.querySelector('.fm-topbar-search');
                if (searchBar) searchBar.classList.toggle('d-none');
            });
        }
    };

    // Alert Dismissal (Fade out effect)
    const initAlerts = () => {
        document.querySelectorAll('.alert-dismissible [data-bs-dismiss="alert"]').forEach(btn => {
            btn.addEventListener('click', function(e) {
                const alert = this.closest('.alert');
                if (alert) {
                    alert.style.transition = 'opacity 0.3s ease, margin 0.3s ease, padding 0.3s ease';
                    alert.style.opacity = '0';
                    setTimeout(() => {
                        alert.style.margin = '0';
                        alert.style.padding = '0';
                        alert.style.height = '0';
                        setTimeout(() => alert.remove(), 300);
                    }, 300);
                }
            });
        });
    };

    // Table Row Affordance (Navigation from data-fm-href)
    const initTableActions = () => {
        document.querySelectorAll('tr[data-fm-href]').forEach(row => {
            row.style.cursor = 'pointer';
            row.addEventListener('click', function(e) {
                // Don't trigger if clicking on a button, link or checkbox
                if (e.target.tagName !== 'A' && e.target.tagName !== 'BUTTON' && !e.target.closest('button') && !e.target.closest('a') && e.target.type !== 'checkbox') {
                    window.location.href = this.dataset.fmHref;
                }
            });
        });
    };

    // Quantity Stepper logic
    const initQuantitySteppers = () => {
        document.querySelectorAll('.fm-qty-stepper').forEach(stepper => {
            const minusBtn = stepper.querySelector('[data-qty="minus"]');
            const plusBtn = stepper.querySelector('[data-qty="plus"]');
            const input = stepper.querySelector('input');

            if (minusBtn && plusBtn && input) {
                minusBtn.addEventListener('click', () => {
                    let val = parseInt(input.value) || 1;
                    if (val > (parseInt(input.min) || 1)) {
                        input.value = val - 1;
                        input.dispatchEvent(new Event('change'));
                    }
                });

                plusBtn.addEventListener('click', () => {
                    let val = parseInt(input.value) || 1;
                    if (val < (parseInt(input.max) || 999)) {
                        input.value = val + 1;
                        input.dispatchEvent(new Event('change'));
                    }
                });
            }
        });
    };

    // Initialize all components
    document.addEventListener('DOMContentLoaded', () => {
        initPasswordToggles();
        initNavToggles();
        initAlerts();
        initTableActions();
        initQuantitySteppers();
    });

})();
