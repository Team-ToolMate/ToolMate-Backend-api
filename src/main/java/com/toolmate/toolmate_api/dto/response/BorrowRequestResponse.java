package com.toolmate.toolmate_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BorrowRequestResponse {
    private Long id;
    private ToolResponse tool;
    private UserDTO borrower;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String status;
    private String message;
    private LocalDateTime createdAt;
}