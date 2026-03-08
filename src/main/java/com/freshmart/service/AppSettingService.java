package com.freshmart.service;

import com.freshmart.entity.AppSetting;
import com.freshmart.repository.AppSettingRepository;
import com.freshmart.util.JpaExecutor;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AppSettingService {

    public static final String LOW_STOCK_THRESHOLD = "low_stock_threshold";
    public static final String UPCOMING_EXPIRY_DAYS = "upcoming_expiry_days";
    public static final String REPLENISH_HISTORY_DAYS = "replenish_history_days";
    public static final String REPLENISH_LEAD_DAYS = "replenish_lead_days";
    public static final String REPLENISH_BUFFER_DAYS = "replenish_buffer_days";
    public static final String REPLENISH_SAFETY_DAYS = "replenish_safety_days";

    private final JpaExecutor executor = new JpaExecutor();
    private final AppSettingRepository repository = new AppSettingRepository();

    public void ensureDefaults() {
        executor.executeVoid(em -> {
            Map<String, DefaultSetting> defaults = defaultSettings();
            for (Map.Entry<String, DefaultSetting> entry : defaults.entrySet()) {
                repository.findByKey(em, entry.getKey()).orElseGet(() -> {
                    AppSetting setting = new AppSetting(
                            entry.getKey(),
                            entry.getValue().value,
                            entry.getValue().description
                    );
                    return repository.save(em, setting);
                });
            }
        });
    }

    public Map<String, String> getAllAsMap() {
        ensureDefaults();
        return executor.execute(em -> {
            List<AppSetting> rows = repository.findAll(em);
            Map<String, String> map = new LinkedHashMap<>();
            for (AppSetting row : rows) {
                map.put(row.getSettingKey(), row.getSettingValue());
            }
            return map;
        });
    }

    public List<AppSetting> getAll() {
        ensureDefaults();
        return executor.execute(repository::findAll);
    }

    public void saveSettings(Map<String, String> values) {
        ensureDefaults();
        executor.executeVoid(em -> {
            Map<String, DefaultSetting> defaults = defaultSettings();
            for (Map.Entry<String, String> entry : values.entrySet()) {
                DefaultSetting defaultSetting = defaults.get(entry.getKey());
                if (defaultSetting == null) {
                    continue;
                }

                String sanitized = sanitizeInt(entry.getValue(), defaultSetting.value);
                AppSetting setting = repository.findByKey(em, entry.getKey())
                        .orElse(new AppSetting(entry.getKey(), sanitized, defaultSetting.description));

                setting.setSettingValue(sanitized);
                setting.setDescription(defaultSetting.description);
                repository.save(em, setting);
            }
        });
    }

    public int getInt(String key, int defaultValue) {
        ensureDefaults();
        return executor.execute(em -> repository.findByKey(em, key)
                .map(AppSetting::getSettingValue)
                .map(value -> parseInt(value, defaultValue))
                .orElse(defaultValue));
    }

    public int getLowStockThreshold() {
        return getInt(LOW_STOCK_THRESHOLD, 50);
    }

    public int getUpcomingExpiryDays() {
        return getInt(UPCOMING_EXPIRY_DAYS, 7);
    }

    public int getReplenishHistoryDays() {
        return getInt(REPLENISH_HISTORY_DAYS, 30);
    }

    public int getReplenishLeadDays() {
        return getInt(REPLENISH_LEAD_DAYS, 3);
    }

    public int getReplenishBufferDays() {
        return getInt(REPLENISH_BUFFER_DAYS, 2);
    }

    public int getReplenishSafetyDays() {
        return getInt(REPLENISH_SAFETY_DAYS, 2);
    }

    private Map<String, DefaultSetting> defaultSettings() {
        Map<String, DefaultSetting> map = new LinkedHashMap<>();
        map.put(LOW_STOCK_THRESHOLD, new DefaultSetting("50", "Ngưỡng cảnh báo tồn kho thấp"));
        map.put(UPCOMING_EXPIRY_DAYS, new DefaultSetting("7", "Số ngày cảnh báo cận hạn"));
        map.put(REPLENISH_HISTORY_DAYS, new DefaultSetting("30", "Số ngày lịch sử dùng cho gợi ý nhập hàng"));
        map.put(REPLENISH_LEAD_DAYS, new DefaultSetting("3", "Lead time mặc định"));
        map.put(REPLENISH_BUFFER_DAYS, new DefaultSetting("2", "Buffer days mặc định"));
        map.put(REPLENISH_SAFETY_DAYS, new DefaultSetting("2", "Safety days mặc định"));
        return map;
    }

    private int parseInt(String raw, int defaultValue) {
        try {
            return Integer.parseInt(raw);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private String sanitizeInt(String raw, String fallback) {
        int fallbackInt = parseInt(fallback, 0);
        int value = parseInt(raw, fallbackInt);
        if (value < 0) value = fallbackInt;
        return String.valueOf(value);
    }

    private static class DefaultSetting {
        private final String value;
        private final String description;

        private DefaultSetting(String value, String description) {
            this.value = value;
            this.description = description;
        }
    }
}