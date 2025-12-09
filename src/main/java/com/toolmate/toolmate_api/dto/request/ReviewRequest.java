package com.toolmate.toolmate_api.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class ReviewRequest {
    private Long revieweeId;
    private Long toolId;
    private Integer rating;
    private List<String> tags;
    private String comment;
    private String itemConditionOnReturn;
}