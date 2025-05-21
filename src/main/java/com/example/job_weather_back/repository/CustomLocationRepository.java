package com.example.job_weather_back.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.job_weather_back.entity.CustomLocation;

@Repository
public interface CustomLocationRepository extends JpaRepository<CustomLocation, Integer> {
    
}
