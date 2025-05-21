// src/main/java/com/example/job_weather_back/repository/CustomizationRepository.java
package com.example.job_weather_back.repository;

import com.example.job_weather_back.entity.Customization; // 엔티티 패키지 경로 수정
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface CustomizationRepository extends JpaRepository<Customization, Integer> {

    // 특정 사용자의 모든 맞춤 설정 조회
    List<Customization> findByUserSn(Long userSn);

    // 뉴스 수집 시 키워드 확장을 위한, 사용자들이 설정한 모든 고유한 관심사 이름들 조회
    @Query("SELECT DISTINCT c.companyType.typeName FROM Customization c WHERE c.companyType IS NOT NULL")
    Set<String> findAllDistinctCompanyTypeNamesFromCustomizations();

    @Query("SELECT DISTINCT c.customPosition.positionName FROM Customization c WHERE c.customPosition IS NOT NULL")
    Set<String> findAllDistinctPositionNamesFromCustomizations();

    @Query("SELECT DISTINCT c.customLocation.locationName FROM Customization c WHERE c.customLocation IS NOT NULL")
    Set<String> findAllDistinctLocationNamesFromCustomizations();
}
