package com.toolmate.toolmate_api.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class ToolRequest {
    private String name;
    private String description;
    private String category;
    private String condition;
    private List<String> imageUrls;
    private Double rentalFee;
    private Boolean isFullyCharged;
}