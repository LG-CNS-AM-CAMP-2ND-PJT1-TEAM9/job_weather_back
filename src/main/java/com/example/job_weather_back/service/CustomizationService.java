package com.example.job_weather_back.service;

import org.springframework.stereotype.Service;

import com.example.job_weather_back.dto.CustomDTO;
import com.example.job_weather_back.entity.CompanyType;
import com.example.job_weather_back.entity.CustomLocation;
import com.example.job_weather_back.entity.CustomPosition;
import com.example.job_weather_back.entity.Customization;
import com.example.job_weather_back.entity.User;
import com.example.job_weather_back.repository.CompanyTypeRepository;
import com.example.job_weather_back.repository.CustomLocationRepository;
import com.example.job_weather_back.repository.CustomPositionRepository;
import com.example.job_weather_back.repository.CustomizationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor // 생성자 주입
public class CustomizationService {
    private final CustomizationRepository customizationRepository;
    private final CompanyTypeRepository companyTypeRepository;
    private final CustomPositionRepository customPositionRepository;
    private final CustomLocationRepository customLocationRepository;

    public void saveCustomization(User user, CustomDTO dto ) {

    Customization customization = customizationRepository.findByUserUserSn(user.getUserSn())
                .orElse(new Customization());

    customization.setUser(user);
    
    CompanyType companyType = companyTypeRepository.findById(dto.getCompanyType())
                                .orElseThrow(() -> new IllegalArgumentException("해당 회사 분류가 존재하지 않습니다."));
    customization.setCompanyType(companyType);

    if(dto.getPosition() != null) {
        CustomPosition position = customPositionRepository.findById(dto.getPosition())
                                .orElseThrow(() -> new IllegalArgumentException("해당 포지션이 존재하지 않습니다."));
        customization.setCustomPosition(position);
    }

    if(dto.getLocation() != null) {
        CustomLocation location = customLocationRepository.findById(dto.getLocation())
                                .orElseThrow(() -> new IllegalArgumentException("해당 지역이 존재하지 않습니다."));
        customization.setCustomLocation(location);
    }

    customizationRepository.save(customization);
    }
}
