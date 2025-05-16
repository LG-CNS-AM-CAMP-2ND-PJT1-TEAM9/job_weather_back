package com.example.job_weather_back.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "news")
@Data
public class News {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "news_sn")
    private int newsSn;

    @Column(name = "news_title", length = 255)
    private String newsTitle;

    @Column(name = "news_description", length = 5000)
    private String newsDescription;

    @Column(name = "news_originallink", length = 255)
    private String newsOriginallink;

    @Column(name = "news_dateTime")
    private LocalDateTime newsDateTime;
}
