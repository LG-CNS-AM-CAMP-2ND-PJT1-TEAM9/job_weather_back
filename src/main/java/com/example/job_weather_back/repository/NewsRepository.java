package com.example.job_weather_back.repository;

import com.example.job_weather_back.entity.News;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface NewsRepository extends JpaRepository<News, Integer> {
    // 최신 뉴스 조회
    Optional<News> findTopByOrderByNewsDateTimeDesc();

    // 검색어가 포함된 뉴스 조회
    List<News> findByNewsTitleContainingOrNewsDescriptionContaining(String title, String description);
    
    // 모든 뉴스를 날짜순으로 반환
    List<News> findAllByOrderByNewsDateTimeDesc();

    // 좋아요한 뉴스 출력
    List<News> findByNewsSn(int newsSn);

    // 뉴스 링크로 중복 확인 (추가된 메소드)
    boolean existsByNewsLink(String newsLink);
} 