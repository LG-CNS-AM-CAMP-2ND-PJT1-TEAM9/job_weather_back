package com.example.job_weather_back.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "custom_position")
@Data
public class CustomPosition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column (name = "position_id")
    private int positionId;

    @Column (nullable = false, name = "position_code")
    private int positionCode;

    @Column (nullable = false, name = "position_name", length = 50)
    private String positionName;
}
