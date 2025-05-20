package com.example.job_weather_back.weather.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WeatherInputMetricsDto {
    private long dailyNewJobPostings;       // 오늘 등록된 신규 공고 수
    private double changeRateFromYesterday;   // 어제 대비 공고 증감률 (예: 0.1은 10% 증가)
    private int majorCompanyPostingCount;   // 주요 기업의 신규 공고 수
    private String trendingKeywords;        // 최근 언급되는 주요 기술 스택 또는 키워드 (쉼표 구분 문자열 등)
    // 추가적으로 필요한 지표들을 여기에 정의합니다.
}