package com.freshmart.bootstrap;

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

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        new BootstrapService().ensureDevData();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        JPAUtil.shutdown();
    }
}
