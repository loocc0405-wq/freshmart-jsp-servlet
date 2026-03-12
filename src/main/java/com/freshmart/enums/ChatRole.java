package com.freshmart.enums;

public enum ChatRole {
    USER("user"),
    ASSISTANT("model"), // Gemini expects "model"
    SYSTEM("system");

    private final String value;

    ChatRole(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
