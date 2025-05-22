package com.example.job_weather_back.weather.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WeatherResponseDto {
    private LocalDate date;
    private int score;
    private String weather;      // 예: "맑음", "흐림"
    private String commentary;   // GPT가 생성한 코멘트
    // 필요하다면 아이콘 정보나 GPT 코멘트의 뉘앙스 등을 추가할 수 있습니다.
}