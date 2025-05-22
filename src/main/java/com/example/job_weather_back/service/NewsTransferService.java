package com.example.job_weather_back.service;

import com.example.job_weather_back.entity.NewContents;
import com.example.job_weather_back.entity.News;
import com.example.job_weather_back.repository.NewContentsRepository;
import com.example.job_weather_back.repository.NewsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class NewsTransferService {
    private final NewsRepository newsRepository;  // News 테이블 접근용
    private final NewContentsRepository newContentsRepository;

    @Transactional
    public void transferNewsToNewContents() {
        // 1. News 테이블에서 모든 뉴스를 가져옴
        List<News> allNews = newsRepository.findAll();

        // 2. News를 NewContents로 변환하여 저장
        List<NewContents> newContentsList = allNews.stream()
                .map(this::convertNewsToNewContents)
                .collect(Collectors.toList());

        // 3. 새로운 컨텐츠 저장
        newContentsRepository.saveAll(newContentsList);
    }

    private NewContents convertNewsToNewContents(News news) {
        return NewContents.builder()
                .type("NEWS")  // 뉴스 타입 지정
                .content(news.getNewsDescription())  // 뉴스 내용
                .title(news.getNewsTitle())  // 뉴스 제목
                .link(news.getNewsLink())  // 뉴스 링크
//                .originalLink(news.getNewsOriginallink())  // 원본 링크
                .createdAt(news.getNewsDateTime())  // 생성 시간
                .build();
    }
}
