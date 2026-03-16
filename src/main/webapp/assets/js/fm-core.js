/**
 * FM Core JS - Lightweight UI enhancements
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
                
                // Update icon or text inside button
                const icon = this.querySelector('i');
                if (icon) {
                    icon.className = isPassword ? 'bi bi-eye-slash' : 'bi bi-eye';
                }
            });
        });
    };

    // Sidebar Toggle (For future phases)
    const initSidebar = () => {
        const toggle = document.querySelector('[data-fm-toggle="sidebar"]');
        const sidebar = document.querySelector('.fm-sidebar');
        if (toggle && sidebar) {
            toggle.addEventListener('click', () => {
                sidebar.classList.toggle('collapsed');
            });
        }
    };

    // Initialize all components
    document.addEventListener('DOMContentLoaded', () => {
        initPasswordToggles();
        initSidebar();
    });

})();
