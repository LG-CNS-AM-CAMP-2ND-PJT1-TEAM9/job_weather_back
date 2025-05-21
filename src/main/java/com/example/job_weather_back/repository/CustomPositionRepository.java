// src/main/java/com/example/job_weather_back/repository/CustomPositionRepository.java
package com.example.job_weather_back.repository;

import com.example.job_weather_back.entity.CustomPosition; // 엔티티 패키지 경로 수정
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface CustomPositionRepository extends JpaRepository<CustomPosition, Integer> {

    // 뉴스 수집 시 키워드 확장을 위해 모든 포지션 이름 조회 (중복 제거)
    @Query("SELECT DISTINCT cp.positionName FROM CustomPosition cp")
    Set<String> findAllDistinctPositionNames();

    // 또는 모든 CustomPosition 엔티티를 가져와서 서비스단에서 이름만 추출
    // List<CustomPosition> findAll();
}
