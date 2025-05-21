package com.example.job_weather_back.dto;

import lombok.Data;

@Data
public class KakaoDto {
    private Long kakaoId;
    private String email;
    private String userNickname;

    public KakaoDto(Long kakaoId, String email, String userNickname) {
        this.kakaoId = kakaoId;
        this.email = email;
        this.userNickname = userNickname;
    }

}
