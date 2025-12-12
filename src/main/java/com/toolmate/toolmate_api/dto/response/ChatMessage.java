package com.toolmate.toolmate_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    private Long id;
    private Long borrowRequestId;
    private Long senderId;
    private String senderName;
    private String senderImageUrl;
    private Long recipientId;
    private String content;
    private MessageType type;
    private LocalDateTime timestamp;
    private MessageStatus status;

    public enum MessageType {
        CHAT,
        JOIN,
        LEAVE,
        TYPING,
        IMAGE,
        FILE
    }

    public enum MessageStatus {
        SENT,
        DELIVERED,
        READ
    }
}