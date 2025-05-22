package com.example.job_weather_back.weather.repository;

import com.example.job_weather_back.weather.entity.JobWeather;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface JobWeatherRepository extends JpaRepository<JobWeather, LocalDate> {

    Optional<JobWeather> findTopByOrderByDateDesc();
}