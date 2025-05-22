package com.example.job_weather_back.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.job_weather_back.entity.LikedEmployment;
import com.example.job_weather_back.entity.User;

@Repository
public interface LikedEmploymentRepository extends JpaRepository<LikedEmployment,Integer> {
    List<LikedEmployment> findByUserUserSn(int userSn);

    void deleteByUserUserSnAndLikedEmpId(int userSn, int LikedEmpId);

    boolean existsByUserAndEmpNum(User user, int empNum);

    Optional<LikedEmployment> findByUserAndEmpNum(User user, int empNum);

    boolean existsByUserUserSnAndEmpNum(int userSn, int empNum);

    Optional<LikedEmployment> findByUserUserSnAndEmpNum(int userSn, int empNum);
    
    
}






