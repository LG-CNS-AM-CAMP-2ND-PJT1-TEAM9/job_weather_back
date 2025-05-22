package com.example.job_weather_back.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_recommendations")
public class UserRecommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_sn", nullable = true) // nullable = true로 변경
    private Integer userSn; // User 엔티티의 PK (int)와 매칭

    @Enumerated(EnumType.STRING)
    @Column(name = "content_type", nullable = false, length = 20)
    private ContentType contentType; // 'NEWS', 'JOB'

    @Column(name = "target_sn", nullable = false)
    private Integer targetSn;

    public enum ContentType {
        NEWS, JOB
    }
}
