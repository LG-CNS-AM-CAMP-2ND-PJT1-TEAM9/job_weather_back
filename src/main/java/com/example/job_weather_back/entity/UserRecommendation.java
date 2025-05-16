package com.example.job_weather_back.entity;

import jakarta.persistence.*;
import lombok.Data;


@Entity
@Data
@Table(name = "user_recommendations")
public class UserRecommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "user_sn", nullable = false)
    private User user;

    @Column(name = "content_type", nullable = false, length = 20)
    private String contentType;

    @Column(name = "target_sn", nullable = false)
    private int targetSn;

}
