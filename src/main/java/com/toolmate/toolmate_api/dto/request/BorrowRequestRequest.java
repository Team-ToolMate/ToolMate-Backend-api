package com.toolmate.toolmate_api.dto.request;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class BorrowRequestRequest {
    private Long toolId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String message;
}