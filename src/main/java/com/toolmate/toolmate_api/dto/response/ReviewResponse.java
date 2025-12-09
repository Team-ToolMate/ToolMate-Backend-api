package com.toolmate.toolmate_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class ReviewResponse {
    private Long id;
    private String reviewerName;
    private Integer rating;
    private List<String> tags;
    private String comment;
    private String itemConditionOnReturn;
    private String createdAt;
}