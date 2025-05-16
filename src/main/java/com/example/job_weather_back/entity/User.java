package com.example.job_weather_back.entity;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "user")
public class User implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int user_sn;

    @Column(nullable = false, length = 20)
    private String user_name;

    @Column(length = 20)
    private String user_pw;

    @Column(length = 20)
    private String user_phone;

    @Column(length = 20)
    private String email;

    @Column(nullable = false, length = 20)
    private String user_nickname;

    @Column(length = 225)
    private String user_social_id;

    @Column(length = 10)
    private String user_delete;
}