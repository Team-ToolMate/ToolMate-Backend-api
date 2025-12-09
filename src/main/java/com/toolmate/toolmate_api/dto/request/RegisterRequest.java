package com.toolmate.toolmate_api.dto.request;

import lombok.Data;

@Data
public class RegisterRequest {
    private String fullName;
    private String email;
    private String password;
    private String phoneNumber;
    private Double latitude;
    private Double longitude;
    private String address;
}