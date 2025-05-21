// src/main/java/com/example/job_weather_back/repository/CompanyRepository.java
package com.example.job_weather_back.repository;

import com.example.job_weather_back.entity.Company; // 엔티티 패키지 경로 수정
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Integer> {

    // 뉴스 수집 시 키워드 확장을 위해 모든 회사 이름 조회 (중복 제거)
    @Query("SELECT DISTINCT c.companyName FROM Company c")
    Set<String> findAllDistinctCompanyNames();

    // 또는 모든 Company 엔티티를 가져와서 서비스단에서 이름만 추출
    // List<Company> findAll();
}
