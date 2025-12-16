package com.toolmate.toolmate_api.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class BorrowRequestRequest {

    @NotNull(message = "Tool ID is required")
    @Positive(message = "Tool ID must be positive")
    private Long toolId;

    @NotNull(message = "Start date is required")
    @FutureOrPresent(message = "Start date must be in the present or future")
    private LocalDateTime startDate;

    @NotNull(message = "End date is required")
    @Future(message = "End date must be in the future")
    private LocalDateTime endDate;

    @Size(max = 500, message = "Message must not exceed 500 characters")
    private String message;

    // Custom validation method
    @AssertTrue(message = "End date must be after start date")
    private boolean isEndDateAfterStartDate() {
        if (startDate == null || endDate == null) {
            return true; // Let @NotNull handle null cases
        }
        return endDate.isAfter(startDate);
    }
}