package com.freshmart.entity;

import java.util.Date;

public class ChatFeedback {
    private Long id;
    private Long chatMessageId;
    private Integer rating; // 1 = helpful, -1 = unhelpful
    private String comment;
    private Date createdAt;

    // Getters and Setters
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Long getChatMessageId() {
        return chatMessageId;
    }
    public void setChatMessageId(Long chatMessageId) {
        this.chatMessageId = chatMessageId;
    }
    public Integer getRating() {
        return rating;
    }
    public void setRating(Integer rating) {
        this.rating = rating;
    }
    public String getComment() {
        return comment;
    }
    public void setComment(String comment) {
        this.comment = comment;
    }
    public Date getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
