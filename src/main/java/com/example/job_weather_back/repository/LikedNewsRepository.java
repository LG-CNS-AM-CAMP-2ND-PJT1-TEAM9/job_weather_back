package com.example.job_weather_back.repository;

import com.example.job_weather_back.entity.LikedNews;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface LikedNewsRepository extends JpaRepository<LikedNews, Integer> {
    List<LikedNews> findByUserUserSn(int userSn);
    Optional<LikedNews> findByUserUserSnAndNewsNewsSn(int userSn, int newsSn);
} 