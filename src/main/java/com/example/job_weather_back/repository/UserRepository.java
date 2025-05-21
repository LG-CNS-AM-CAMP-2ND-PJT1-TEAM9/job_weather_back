package com.example.job_weather_back.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.job_weather_back.entity.User;

@Repository

public interface UserRepository extends JpaRepository<User, Integer> {
    //닉네임 존재여부
    boolean existsByUserNickname(String userNickname);

    //이메일, pw
    Optional<User> findByEmailAndUserPw(String email, String userPw);


    // 회원정보
    Optional<User> findById(int userSn);

    //사용자 존재여부
    Optional<User> findByEmail(String email);

    //회원가입 여부
    boolean existsByEmail(String email);


}
