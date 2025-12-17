package com.toolmate.toolmate_api.entity;

public enum NotificationType {

    REQUEST_RECEIVED("📩", "Request Received"),
    REQUEST_ACCEPTED("✅", "Request Accepted"),
    REQUEST_REJECTED("❌", "Request Rejected"),
    TRANSACTION_COMPLETED("💰", "Transaction Completed"),
    REVIEW_REMINDER("📝", "Review Reminder"),
    TOOL_COLLECTED("📦", "Tool Collected"),
    TOOL_RETURNED("📦", "Tool Returned"),
    REQUEST_CANCELLED("⚠️", "Request Cancelled");

    private final String emoji;
    private final String title;

    NotificationType(String emoji, String title) {
        this.emoji = emoji;
        this.title = title;
    }

    public String getEmoji() { return emoji; }
    public String getTitle() { return title; }
}
