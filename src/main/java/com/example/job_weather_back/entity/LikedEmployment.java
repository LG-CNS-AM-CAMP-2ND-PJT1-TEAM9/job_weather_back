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
@Table(name = "liked_employment")
@Data
public class LikedEmployment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "liked_emp_id")
    private int likedEmpId;

    @Column(name = "emp_num", nullable = false)
    private int empNum;

    @ManyToOne
    @JoinColumn(name = "user_sn")
    private User user;
}
