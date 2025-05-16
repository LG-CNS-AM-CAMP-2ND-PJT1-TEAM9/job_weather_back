package com.example.job_weather_back.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "job_weather")
public class JobWeather {

    @Id
    @Column(name = "date")
    private LocalDate date;

    @Column(name = "score", nullable = false)
    private int score;

    @Column(name = "weather", nullable = false, length = 20)
    private String weather;

    @Column(name = "commentary", columnDefinition = "TEXT")
    private String commentary;
}
