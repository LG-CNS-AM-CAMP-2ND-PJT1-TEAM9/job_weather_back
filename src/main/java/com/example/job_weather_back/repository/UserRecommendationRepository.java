package com.example.job_weather_back.repository;

import com.example.job_weather_back.entity.UserRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface UserRecommendationRepository extends JpaRepository<UserRecommendation, Integer> {

    // 로그인한 사용자의 특정 타입 추천 목록 조회
    List<UserRecommendation> findByUserSnAndContentTypeOrderByIdDesc(Integer userSn, UserRecommendation.ContentType contentType);

    // 로그인하지 않은 사용자 (일반 추천)의 특정 타입 추천 목록 조회
    List<UserRecommendation> findByUserSnIsNullAndContentTypeOrderByIdDesc(UserRecommendation.ContentType contentType);

    // 중복 저장 방지 (로그인 사용자용)
    boolean existsByUserSnAndContentTypeAndTargetSn(Integer userSn, UserRecommendation.ContentType contentType, Integer targetSn);
    // 중복 저장 방지 (일반 추천용)
    boolean existsByUserSnIsNullAndContentTypeAndTargetSn(UserRecommendation.ContentType contentType, Integer targetSn);


    // 로그인한 사용자의 특정 타입 추천 삭제
    @Transactional
    void deleteByUserSnAndContentType(Integer userSn, UserRecommendation.ContentType contentType);
    // 일반 추천의 특정 타입 추천 삭제
    @Transactional
    void deleteByUserSnIsNullAndContentType(UserRecommendation.ContentType contentType);
}
