package com.example.job_weather_back.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.job_weather_back.entity.Customization;

@Repository
public interface CustomizationRepository extends JpaRepository<Customization, Integer>{
    // 유저의 맞춤설정 조회
    Optional<Customization> findByUserUserSn(int userSn);
}
