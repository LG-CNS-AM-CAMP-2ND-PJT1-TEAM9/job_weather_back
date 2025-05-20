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
    @Column(name = "user_sn")
    private int userSn;

    @Column(name = "user_name", nullable = false, length = 20)
    private String userName;

    @Column(name = "user_pw",  nullable = false, length = 255)
    private String userPw;

    @Column(name = "user_phone", length = 20)
    private String userPhone;

    @Column(length = 20,  nullable = false)
    private String email;

    @Column(name = "user_nickname", nullable = false, length = 20)
    private String userNickname;

    @Column(name = "user_social_id", length = 225)
    private String userSocialId;

    @Column(name = "user_delete", length = 10)
    private String userDelete = "0";
}