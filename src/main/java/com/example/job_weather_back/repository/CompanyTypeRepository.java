package com.example.job_weather_back.repository;

import com.example.job_weather_back.entity.CompanyType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface CompanyTypeRepository extends JpaRepository<CompanyType, Integer> {

    // 뉴스 수집 시 키워드 확장을 위해 모든 기업 유형 이름 조회 (중복 제거)
    @Query("SELECT DISTINCT ct.typeName FROM CompanyType ct")
    Set<String> findAllDistinctTypeNames();

    // 또는 모든 CompanyType 엔티티를 가져와서 서비스단에서 이름만 추출해도 됩니다.
    // List<CompanyType> findAll();
}
