package com.example.job_weather_back.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.job_weather_back.entity.User;

public interface UserRepository extends JpaRepository<User, Integer> {
    
}
