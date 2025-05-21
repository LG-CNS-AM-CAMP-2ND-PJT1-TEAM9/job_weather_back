package com.example.job_weather_back.weather.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "job_weather")
public class JobWeather {

    @Id
    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "score", nullable = false)
    private int score;

    @Column(name = "weather", length = 20, nullable = false)
    private String weather;

    @Lob
    @Column(name = "commentary", columnDefinition = "TEXT")
    private String commentary;
}