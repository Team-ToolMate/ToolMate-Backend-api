package com.toolmate.toolmate_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OwnerDTO {
    private Long id;
    private String fullName;
    private Double rating;
    private String profileImageUrl;
}