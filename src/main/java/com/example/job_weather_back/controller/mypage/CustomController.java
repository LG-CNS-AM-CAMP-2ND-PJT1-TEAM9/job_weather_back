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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@Tag(name = "User Customization API (Mypage)", description = "마이페이지 - 사용자 맞춤 설정(관심사) 관련 API")
@RestController
public class CustomController {
    @Autowired CustomizationRepository customizationRepository;
    @Autowired CustomPositionRepository customPositionRepository;
    @Autowired CustomLocationRepository customLocationRepository;
    @Autowired CompanyTypeRepository companyTypeRepository;
    @Autowired CustomizationService customizationService;

    // 저장된 맞춤설정 불러오기
    @Operation(summary = "저장된 맞춤설정 조회", description = "현재 로그인된 사용자의 저장된 맞춤설정(기업유형, 포지션, 지역 ID)을 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                         content = @Content(mediaType = "application/json",
                         schema = @Schema(type = "object", example = "{\"companyType\": 1, \"position\": 2, \"location\": 3}"))),
            @ApiResponse(responseCode = "200", description = "저장된 맞춤설정 없음 (빈 객체 반환)",
                         content = @Content(mediaType = "application/json", schema = @Schema(type = "object", example = "{}"))),
            @ApiResponse(responseCode = "401", description = "로그인 필요", content = @Content)
    })
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
    @Operation(summary = "맞춤설정 옵션 목록 조회", description = "특정 카테고리(position, companyType, location)에 대한 전체 옵션 목록을 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                         content = @Content(mediaType = "application/json",
                         schema = @Schema(type = "array", implementation = Object.class, // 실제 타입은 item에 따라 다름
                                          example = "[{\"positionId\":1, \"positionName\":\"백엔드\"}, ...] 또는 [{\"typeId\":1, \"typeName\":\"대기업SI\"}, ...]"))),
            @ApiResponse(responseCode = "400", description = "잘못된 카테고리 이름", content = @Content)
    })
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
    @Operation(summary = "맞춤설정 저장/수정", description = "현재 로그인된 사용자의 맞춤설정(기업유형, 포지션, 지역)을 저장하거나 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "저장/수정 성공",
                         content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"success\": true, \"message\": \"맞춤설정 저장 완료\"}"))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터 또는 저장 실패", content = @Content),
            @ApiResponse(responseCode = "401", description = "로그인 필요", content = @Content)
    })
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
