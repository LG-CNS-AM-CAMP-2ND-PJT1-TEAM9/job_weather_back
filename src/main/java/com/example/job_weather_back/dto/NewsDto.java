package com.example.job_weather_back.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewsDto {
    private int newsSn;
    private String newsTitle;
    private String newsDescription;
    private String newsLink;
    private LocalDateTime newsDateTime;
} 