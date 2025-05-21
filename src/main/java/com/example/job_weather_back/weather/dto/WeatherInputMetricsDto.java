package com.example.job_weather_back.weather.dto; // 실제 DTO 패키지 경로

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString; // toString() 로깅을 위해 추가

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString // 로그 출력 시 필드 값을 보기 위해 추가
public class WeatherInputMetricsDto {
    private long totalNewEntryLevelDeveloperJobsToday;
    private int newJobsAbsoluteChangeFromYesterday;
    private int newJobsInMajorCompaniesToday;
    private int newJobsWithRemoteOptionToday;
    private int newInternshipToFullTimeJobsToday;
    private String trendingKeywords; // 추가된 필드
}
