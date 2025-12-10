package com.toolmate.toolmate_api.dto.request;

import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String fullName;
    private String phoneNumber;
    private Double latitude;
    private Double longitude;
    private String address;
    private String profileImageUrl;
}