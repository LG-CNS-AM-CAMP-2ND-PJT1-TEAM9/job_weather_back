package com.example.job_weather_back.dto;

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
public class MainPageRecommendationsDto {

    private List<NewsDto> news;
    // private List<JobDto> jobs; // 채용공고 DTO (추후 정의 필요)

    // 만약 jobs 필드 없이 뉴스만 있다면 아래 생성자/빌더 사용 가능
    // public MainPageRecommendationsDto(List<NewsDto> news) {
    //     this.news = news;
    // }
}
