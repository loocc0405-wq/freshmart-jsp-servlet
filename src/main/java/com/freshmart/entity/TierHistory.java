package com.freshmart.entity;

import com.freshmart.enums.Tier;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tier_history", indexes = {
        @Index(name = "idx_tier_history_user", columnList = "user_id"),
        @Index(name = "idx_tier_history_created_at", columnList = "created_at")
})
public class TierHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_tier_history_user"))
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "old_tier", nullable = false, length = 10)
    private Tier oldTier;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_tier", nullable = false, length = 10)
    private Tier newTier;

    @Column(name = "old_expired_date")
    private LocalDate oldExpiredDate;

    @Column(name = "new_expired_date")
    private LocalDate newExpiredDate;

    @Column(name = "change_type", nullable = false, length = 30)
    private String changeType;

    @Column(name = "note", length = 255)
    private String note;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public TierHistory() {
    }

    // ---------- getters / setters ----------

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Tier getOldTier() {
        return oldTier;
    }

    public void setOldTier(Tier oldTier) {
        this.oldTier = oldTier;
    }

    public Tier getNewTier() {
        return newTier;
    }

    public void setNewTier(Tier newTier) {
        this.newTier = newTier;
    }

    public LocalDate getOldExpiredDate() {
        return oldExpiredDate;
    }

    public void setOldExpiredDate(LocalDate oldExpiredDate) {
        this.oldExpiredDate = oldExpiredDate;
    }

    public LocalDate getNewExpiredDate() {
        return newExpiredDate;
    }

    public void setNewExpiredDate(LocalDate newExpiredDate) {
        this.newExpiredDate = newExpiredDate;
    }

    public String getChangeType() {
        return changeType;
    }

    public void setChangeType(String changeType) {
        this.changeType = changeType;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
