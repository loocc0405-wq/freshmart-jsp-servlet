package com.freshmart.entity;

import com.freshmart.enums.ChatIntent;
import com.freshmart.enums.ChatRole;
import com.freshmart.enums.ChatSourceType;
import java.util.Date;

public class ChatMessage {
    private Long id;
    private Long chatSessionId;
    private ChatRole role;
    private String messageContent;
    private ChatIntent intent;
    private ChatSourceType sourceType;
    private Date createdAt;

    // Getters and Setters
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Long getChatSessionId() {
        return chatSessionId;
    }
    public void setChatSessionId(Long chatSessionId) {
        this.chatSessionId = chatSessionId;
    }
    public ChatRole getRole() {
        return role;
    }
    public void setRole(ChatRole role) {
        this.role = role;
    }
    public String getMessageContent() {
        return messageContent;
    }
    public void setMessageContent(String messageContent) {
        this.messageContent = messageContent;
    }
    public ChatIntent getIntent() {
        return intent;
    }
    public void setIntent(ChatIntent intent) {
        this.intent = intent;
    }
    public ChatSourceType getSourceType() {
        return sourceType;
    }
    public void setSourceType(ChatSourceType sourceType) {
        this.sourceType = sourceType;
    }
    public Date getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
