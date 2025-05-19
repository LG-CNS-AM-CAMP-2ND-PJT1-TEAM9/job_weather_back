package com.example.job_weather_back.dto;

import lombok.Data;

@Data
public class ProfileDTO {
    private int userSn;
    private String userPw;
    private String userPhone;
    private String userNickname;
}
