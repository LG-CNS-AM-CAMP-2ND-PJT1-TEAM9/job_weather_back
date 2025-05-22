package com.example.job_weather_back.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "keyword_master")
public class KeywordMaster {

    @Id
    private int id;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private String name;
}