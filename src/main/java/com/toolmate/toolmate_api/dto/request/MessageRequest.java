package com.toolmate.toolmate_api.dto.request;

import lombok.Data;

@Data
public class MessageRequest {
    private Long borrowRequestId;
    private String content;
}