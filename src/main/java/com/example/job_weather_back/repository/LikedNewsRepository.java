package com.example.job_weather_back.repository;

import java.util.List;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.job_weather_back.entity.LikedNews;
import com.example.job_weather_back.entity.User;

import java.util.Optional;


@Repository
public interface LikedNewsRepository extends JpaRepository<LikedNews, Integer> {
    List<LikedNews> findByUserUserSn(int userSn);

    void deleteByUserUserSnAndLikedNewsId(int userSn, int LikedNewsId);

    Optional<LikedNews> findByUserUserSnAndNewsNewsSn(int userSn, int newsSn);

    void deleteByUser(User user);
} 

