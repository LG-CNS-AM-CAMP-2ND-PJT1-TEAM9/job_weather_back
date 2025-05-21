package com.example.job_weather_back.repository;

import java.util.List;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.job_weather_back.entity.LikedNews;

@Repository
public interface LikedNewsRepository extends JpaRepository<LikedNews, Integer> {
    List<LikedNews> findByUserUserSn(int userSn);
    void deleteByUserUserSnAndLikedNewsId(int userSn, int LikedNewsId);
}
