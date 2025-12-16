package com.toolmate.toolmate_api.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import java.util.List;

@Data
public class ToolRequest {

    @NotBlank(message = "Tool name is required")
    @Size(min = 3, max = 100, message = "Tool name must be between 3 and 100 characters")
    private String name;

    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 2000, message = "Description must be between 10 and 2000 characters")
    private String description;

    @NotBlank(message = "Category is required")
    @Pattern(
            regexp = "^(Tools|Garden|Sports|Electronics|Kitchen|Other)$",
            message = "Category must be one of: Tools, Garden, Sports, Electronics, Kitchen, Other"
    )
    private String category;

    @NotBlank(message = "Condition is required")
    @Pattern(
            regexp = "^(New|Like New|Good|Fair)$",
            message = "Condition must be one of: New, Like New, Good, Fair"
    )
    private String condition;

    @NotEmpty(message = "At least one image URL is required")
    @Size(min = 1, max = 5, message = "You can add 1 to 5 images")
    private List<@URL(message = "Invalid image URL") String> imageUrls;

    @NotNull(message = "Rental fee is required")
    @DecimalMin(value = "0.0", message = "Rental fee cannot be negative")
    @DecimalMax(value = "100000.0", message = "Rental fee is too high")
    private Double rentalFee;

    @NotBlank(message = "Rate type is required")
    @Pattern(
            regexp = "^(FREE|HOURLY|DAILY)$",
            message = "Rate type must be one of: FREE, HOURLY, DAILY"
    )
    private String rateType;

    @NotNull(message = "Fully charged status is required")
    private Boolean isFullyCharged;
}