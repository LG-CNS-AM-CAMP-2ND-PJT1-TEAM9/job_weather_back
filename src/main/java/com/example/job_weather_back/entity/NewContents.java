//뉴스/채용 소식의 기본 정보를 담는 테이블

package com.example.job_weather_back.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "new_contents")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
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

    private String link;

    @Column(name = "original_link")
    private String originalLink;
}