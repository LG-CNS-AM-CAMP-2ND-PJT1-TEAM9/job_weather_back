package com.example.job_weather_back.service;

import com.example.job_weather_back.entity.News;
import com.example.job_weather_back.entity.NewContents;
import com.example.job_weather_back.repository.NewsRepository;
import com.example.job_weather_back.repository.NewContentsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NewsTransferService {
    private final NewsRepository newsRepository;
    private final NewContentsRepository newContentsRepository;

    @Transactional
    public void transferNewsToNewContents() {
        try {
            log.info("뉴스 컨텐츠 전송 시작");
            List<News> allNews = newsRepository.findAllByOrderByNewsDateTimeDesc();
            log.info("전송할 뉴스 총 개수: {}", allNews.size());

            if (allNews.isEmpty()) {
                log.info("전송할 뉴스가 없습니다.");
                return;
            }

            // 청크 단위로 처리
            int chunkSize = 1000;

            for (int i = 0; i < allNews.size(); i += chunkSize) {
                int end = Math.min(i + chunkSize, allNews.size());
                List<News> newsChunk = allNews.subList(i, end);

                List<NewContents> newContentsList = newsChunk.stream()
                        .map(this::convertNewsToNewContents)
                        .collect(Collectors.toList());

                newContentsRepository.saveAll(newContentsList);

                int processedCount = i + newsChunk.size();
                int percentage = processedCount * 100 / allNews.size();
                log.info("전송 진행률: {}% ({}/{})",
                        percentage, processedCount, allNews.size());
            }

            log.info("뉴스 컨텐츠 전송 완료. 총 처리된 뉴스 수: {}", allNews.size());
        } catch (Exception e) {
            log.error("뉴스 컨텐츠 전송 중 오류 발생: ", e);
            throw e;
        }
    }

    private NewContents convertNewsToNewContents(News news) {
        NewContents newContents = new NewContents();
        newContents.setType("NEWS");
        newContents.setTitle(news.getNewsTitle());
        newContents.setContent(news.getNewsDescription());
        newContents.setCreatedAt(news.getNewsDateTime());
        return newContents;
    }
}