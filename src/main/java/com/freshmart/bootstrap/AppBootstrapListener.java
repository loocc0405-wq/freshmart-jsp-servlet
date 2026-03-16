package com.freshmart.bootstrap;

import com.freshmart.service.AppSettingService;
import com.freshmart.service.BootstrapService;
import com.freshmart.util.JPAUtil;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

/**
 * Runs on application startup.
 * - Seeds dev data (default users + sample products/lots)
 * - Closes EntityManagerFactory on shutdown
 */
@WebListener
public class AppBootstrapListener implements ServletContextListener {

    private static final String SCHEDULER_KEY = SubscriptionMaintenanceScheduler.class.getName();

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        new BootstrapService().ensureDevData();
        new AppSettingService().ensureDefaults();

        SubscriptionMaintenanceScheduler scheduler = new SubscriptionMaintenanceScheduler();
        scheduler.start();
        sce.getServletContext().setAttribute(SCHEDULER_KEY, scheduler);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        Object raw = sce.getServletContext().getAttribute(SCHEDULER_KEY);
        if (raw instanceof SubscriptionMaintenanceScheduler) {
            ((SubscriptionMaintenanceScheduler) raw).stop();
        }
        JPAUtil.shutdown();
    }
}
