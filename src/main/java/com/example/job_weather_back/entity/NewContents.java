//뉴스/채용 소식의 기본 정보를 담는 테이블

package com.example.job_weather_back.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "new_contents")
@Getter
@Setter
public class NewContents {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "new_contents_id") // DB 필드명에 맞춤
    private Long id;

    private String type;
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}