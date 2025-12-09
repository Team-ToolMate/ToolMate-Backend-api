package com.toolmate.toolmate_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String fullName;
    private String email;
    private String phoneNumber;
    private Double latitude;
    private Double longitude;
    private String address;
    private String profileImageUrl;
    private Double rating;
    private Integer totalBorrows;
    private Integer totalLends;
}