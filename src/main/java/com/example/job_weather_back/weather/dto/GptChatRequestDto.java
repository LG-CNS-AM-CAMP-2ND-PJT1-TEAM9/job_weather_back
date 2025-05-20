package com.example.job_weather_back.weather.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GptChatRequestDto {
    private String model;
    private List<GptChatMessageDto> messages;
    private Double temperature;    // Optional: 0.0 ~ 2.0, 기본값 1.0 또는 0.7
    private Integer max_tokens;    // Optional: 응답 최대 토큰 수
    // 필요에 따라 top_p, n 등의 다른 파라미터 추가 가능
}