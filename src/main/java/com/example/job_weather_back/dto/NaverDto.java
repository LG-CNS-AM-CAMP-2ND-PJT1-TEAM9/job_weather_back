package com.example.job_weather_back.dto;

import lombok.Data;

@Data
public class NaverDto {
    private Long naverId;
    private String email;
    private String userNickname;
    private String userPhone;

    public NaverDto(Long naverId, String email, String userNickname, String userPhone) {
        this.naverId = naverId;
        this.email = email;
        this.userNickname = userNickname;
        this.userPhone = userPhone;
    }

}
