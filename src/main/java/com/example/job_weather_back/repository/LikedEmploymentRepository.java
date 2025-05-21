package com.example.job_weather_back.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.job_weather_back.entity.LikedEmployment;

@Repository
public interface LikedEmploymentRepository extends JpaRepository<LikedEmployment,Integer> {
    List<LikedEmployment> findByUserUserSn(int userSn);

    void deleteByUserUserSnAndLikedEmpId(int UserSn, int LikedEmpId);
}
