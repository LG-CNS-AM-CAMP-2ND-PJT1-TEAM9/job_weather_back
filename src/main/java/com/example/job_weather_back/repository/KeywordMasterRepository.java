package com.example.job_weather_back.repository;

import com.example.job_weather_back.entity.KeywordMaster;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KeywordMasterRepository 
extends JpaRepository<KeywordMaster, Integer> {
}