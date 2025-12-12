package com.toolmate.toolmate_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class StatusHistoryDTO {
    private String status;
    private String changedBy;
    private String notes;
    private LocalDateTime changedAt;
}