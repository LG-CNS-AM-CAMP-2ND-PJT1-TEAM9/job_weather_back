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

    // 오늘 등록된 전체 신입 개발자 공고 수
    private long totalNewEntryLevelDeveloperJobsToday;

    // 어제 대비 오늘 신규 신입 개발자 공고 수 변화량 (절대값, 예: +10, -5)
    private int newJobsAbsoluteChangeFromYesterday;

    // 주요 IT 기업 (예: 네카라쿠배 등 미리 정의된 리스트)의 오늘 신규 신입 개발자 공고 수
    private int newJobsInMajorCompaniesToday;

    // 오늘 신규 신입 개발자 공고 중 '원격/재택근무 가능' 옵션이 있는 공고 수
    private int newJobsWithRemoteOptionToday;

    // 오늘 신규 신입 개발자 공고 중 '정규직 전환형 인턴' 공고 수
    private int newInternshipToFullTimeJobsToday;

    // (선택적 추가 가능 지표)
    // private double averageSalaryOfferIndicator; // 평균 제시 연봉 지표 (0-1 사이 정규화 값)
    // private double competitionRatioIndicator; // 예상 경쟁률 지표 (0-1 사이 정규화 값, 낮을수록 좋음)
}