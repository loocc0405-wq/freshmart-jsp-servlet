package com.freshmart.bootstrap;

import com.freshmart.service.SubscriptionService;
import com.freshmart.service.dto.SubscriptionMaintenanceResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class SubscriptionMaintenanceScheduler {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionMaintenanceScheduler.class);
    private static final long PERIOD_MINUTES = 60;

    private final SubscriptionService subscriptionService = new SubscriptionService();
    private ScheduledExecutorService executor;

    public synchronized void start() {
        if (executor != null && !executor.isShutdown()) {
            return;
        }

        executor = Executors.newSingleThreadScheduledExecutor(new MaintenanceThreadFactory());
        Runnable task = this::runSafely;

        task.run();
        executor.scheduleAtFixedRate(task, PERIOD_MINUTES, PERIOD_MINUTES, TimeUnit.MINUTES);
        log.info("Subscription maintenance scheduler started (every {} minutes)", PERIOD_MINUTES);
    }

    public synchronized void stop() {
        if (executor == null) {
            return;
        }
        executor.shutdownNow();
        log.info("Subscription maintenance scheduler stopped");
    }

    private void runSafely() {
        try {
            SubscriptionMaintenanceResult result = subscriptionService.runMaintenanceSweep();
            log.info("Subscription maintenance finished: scanned={}, downgraded={}, notifications={}, runDate={}",
                    result.getScannedUsers(),
                    result.getDowngradedUsers(),
                    result.getCreatedNotifications(),
                    result.getRunDate());
        } catch (RuntimeException ex) {
            log.error("Subscription maintenance failed", ex);
        }
    }

    private static class MaintenanceThreadFactory implements ThreadFactory {
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r, "subscription-maintenance");
            thread.setDaemon(true);
            return thread;
        }
    }
}
