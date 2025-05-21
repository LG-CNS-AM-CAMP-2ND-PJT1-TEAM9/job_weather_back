package com.example.job_weather_back.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.job_weather_back.entity.CustomPosition;

@Repository
public interface CustomPositionRepository extends JpaRepository<CustomPosition, Integer>{
    
}
