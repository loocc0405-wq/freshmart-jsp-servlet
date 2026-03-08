package com.freshmart.repository;

import com.freshmart.entity.AppSetting;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Optional;

public class AppSettingRepository {

    public Optional<AppSetting> findByKey(EntityManager em, String key) {
        List<AppSetting> list = em.createQuery(
                        "SELECT s FROM AppSetting s WHERE s.settingKey = :key",
                        AppSetting.class)
                .setParameter("key", key)
                .setMaxResults(1)
                .getResultList();
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public List<AppSetting> findAll(EntityManager em) {
        return em.createQuery(
                        "SELECT s FROM AppSetting s ORDER BY s.settingKey ASC",
                        AppSetting.class)
                .getResultList();
    }

    public AppSetting save(EntityManager em, AppSetting setting) {
        if (setting.getId() == null) {
            em.persist(setting);
            return setting;
        }
        return em.merge(setting);
    }
}