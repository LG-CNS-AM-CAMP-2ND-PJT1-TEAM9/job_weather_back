package com.example.job_weather_back.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "liked_news")
@Data
public class LikedNews {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "liked_news_id")
    private int likedNewsId;

    @ManyToOne
    @JoinColumn(name = "news_sn", nullable = false)
    private News news;

    @ManyToOne
    @JoinColumn(name = "user_sn", nullable = false)
    private User user;
}

