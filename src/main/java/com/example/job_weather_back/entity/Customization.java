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
@Table(name = "customization")
@Data
public class Customization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "custom_id")
    private Integer customId;

    @ManyToOne
    @JoinColumn(name = "user_sn", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "type_id", nullable = false)
    private CompanyType companyType;

    @ManyToOne
    @JoinColumn(name = "position_id")
    private CustomPosition customPosition;

    @ManyToOne
    @JoinColumn(name = "location_id")
    private CustomLocation customLocation;
}
