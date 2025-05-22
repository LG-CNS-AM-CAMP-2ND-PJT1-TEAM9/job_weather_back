package com.example.job_weather_back.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notifications_id")
    private Long notificationsId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "new_content_id", nullable = false)
    private NewContents newContents;

    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "user_sn", nullable = false)
    private User user;

}