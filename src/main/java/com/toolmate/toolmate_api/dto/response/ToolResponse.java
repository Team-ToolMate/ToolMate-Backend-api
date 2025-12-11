package com.toolmate.toolmate_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ToolResponse {
    private Long id;
    private String name;
    private String description;
    private String category;
    private String condition;
    private List<String> imageUrls;
    private Boolean isAvailable;
    private Double rentalFee;
    private String rateType;
    private Boolean isFullyCharged;
    private Double distance;
    private Double rating;
    private Integer totalBorrows;
    private OwnerDTO owner;
    private LocalDateTime createdAt;
}