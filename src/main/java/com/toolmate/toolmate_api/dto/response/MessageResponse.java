package com.toolmate.toolmate_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MessageResponse {
    private Long id;
    private Long senderId;
    private String senderName;
    private String content;
    private Boolean isRead;
    private String sentAt;
}