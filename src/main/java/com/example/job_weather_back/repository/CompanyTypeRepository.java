package com.example.job_weather_back.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.job_weather_back.entity.CompanyType;

@Repository
public interface CompanyTypeRepository extends JpaRepository<CompanyType, Integer>{
    
}
