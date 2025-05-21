package com.example.job_weather_back.controller.mypage;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

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
import com.example.job_weather_back.service.CustomizationService;

import jakarta.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;



@CrossOrigin
@RestController
public class CustomController {
    @Autowired CustomizationRepository customizationRepository;
    @Autowired CustomPositionRepository customPositionRepository;
    @Autowired CustomLocationRepository customLocationRepository;
    @Autowired CompanyTypeRepository companyTypeRepository;
    @Autowired CustomizationService customizationService;

    // 저장된 맞춤설정 불러오기
    @GetMapping("/mypage/custom")
    public ResponseEntity<?> printCustom(HttpSession session) {
        User userInfo = (User) session.getAttribute("user_info");
        if(userInfo == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success",false, "message", "로그인 후 이용하세요"));
        }

        int id = userInfo.getUserSn();
        Optional<Customization> opt = customizationRepository.findByUserUserSn(id);
        if(opt.isEmpty()) {
            return ResponseEntity.ok(Collections.emptyMap());
        }
        int companyId = opt.get().getCompanyType().getTypeId();
        int positionId = opt.get().getCustomPosition().getPositionId();
        int locationId = opt.get().getCustomLocation().getLocationId();

        Map<String, Integer> map = new HashMap<>();
        map.put("companyType", companyId);
        map.put("position", positionId); 
        map.put("location", locationId); 
        System.out.println("custom"+map);

        return ResponseEntity.ok(map);
    }

    // 맞춤설정의 옵션들 조회
    @GetMapping("/mypage/custom/{item}")
    public ResponseEntity<?> getMethodName(@PathVariable String item) {
        List<?> list;
        if(item.equals("position")) {
            list = customPositionRepository.findAll();
        } else if(item.equals("companyType")) {
            list = companyTypeRepository.findAll();
        } else if(item.equals("location")) {
            list = customLocationRepository.findAll();
        } else {
            return ResponseEntity.badRequest().body(Map.of("success",false,"message","올바른 단어로 입력하세요"));
        }

        
        return ResponseEntity.ok(list);
    }

    // 새로 설정된 맞춤설정 저장
    @PostMapping("/mypage/custom")
    public ResponseEntity<?> saveCustom(@RequestBody CustomDTO dto, HttpSession session) {
        try{
            User userInfo = (User) session.getAttribute("user_info");
            customizationService.saveCustomization(userInfo, dto);
            return ResponseEntity.ok(Map.of("success",true,"message","맞춤설정 저장 완료"));
        } catch(Exception e){
            return ResponseEntity.badRequest().body(Map.of("success",false,"message", e.getMessage()));
        }
    }
    
    
}
