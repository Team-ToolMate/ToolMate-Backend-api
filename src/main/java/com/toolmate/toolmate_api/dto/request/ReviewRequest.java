package com.toolmate.toolmate_api.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.List;

@Data
public class ReviewRequest {

    @NotNull(message = "Reviewee ID is required")
    @Positive(message = "Reviewee ID must be positive")
    private Long revieweeId;

    @Positive(message = "Tool ID must be positive")
    private Long toolId;

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must not exceed 5")
    private Integer rating;

    @Size(max = 10, message = "Maximum 10 tags allowed")
    private List<@NotBlank(message = "Tag cannot be blank") String> tags;

    @Size(max = 1000, message = "Comment must not exceed 1000 characters")
    private String comment;

    @Pattern(
            regexp = "^(Same|Damaged)$",
            message = "Item condition must be either 'Same' or 'Damaged'"
    )
    private String itemConditionOnReturn;
}