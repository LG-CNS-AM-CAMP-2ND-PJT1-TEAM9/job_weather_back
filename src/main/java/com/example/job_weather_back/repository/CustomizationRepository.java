package com.example.job_weather_back.repository;

import java.util.List;
import java.util.Optional;

import com.example.job_weather_back.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.job_weather_back.entity.Customization;

@Repository
public interface CustomizationRepository extends JpaRepository<Customization, Integer>{
    // 유저의 맞춤설정 조회
    Optional<Customization> findByUserUserSn(int userSn);
    List<Customization> findByUser(User user); //추가함
}
