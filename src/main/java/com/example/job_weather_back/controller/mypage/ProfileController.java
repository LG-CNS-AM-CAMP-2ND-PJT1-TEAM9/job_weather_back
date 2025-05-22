package com.example.job_weather_back.controller.mypage;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import com.example.job_weather_back.dto.ProfileDTO;
import com.example.job_weather_back.entity.User;
import com.example.job_weather_back.repository.UserRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@Tag(name = "User Profile API (Mypage)", description = "마이페이지 - 사용자 프로필 정보 조회 및 수정, 비밀번호 확인 API")
@CrossOrigin
@RestController
public class ProfileController {
    @Autowired UserRepository userRepository;

    // 회원정보 조회
    @Operation(summary = "회원정보 조회", description = "현재 로그인된 사용자의 프로필 정보를 반환합니다. (회원정보 수정을 위한 이전 데이터 조회용)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "401", description = "로그인 필요 (인증되지 않음)", content = @Content),
            @ApiResponse(responseCode = "404", description = "사용자 정보를 찾을 수 없음", content = @Content)
    })
    @GetMapping("/mypage/profile")
    public User printProfile(HttpSession session) {
        User user = (User) session.getAttribute("user_info");
        int id = user.getUserSn();

        Optional<User> opt = userRepository.findById(id);
        User result = opt.get();

        return result;
    }
    
    //회원정보 수정
    @Operation(summary = "회원정보 수정", description = "현재 로그인된 사용자의 프로필 정보를 전달받은 내용으로 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수정 성공",
                         content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"message\": \"Update successfully\"}"))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (예: 중복된 닉네임)", content = @Content),
            @ApiResponse(responseCode = "401", description = "로그인 필요 (인증되지 않음)", content = @Content),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음", content = @Content)
    })
    @PostMapping("/mypage/profile")
    public ResponseEntity<String> updateProfile(
        @RequestBody ProfileDTO profileDTO,
        HttpSession session
    ) {
        int id = ((User) session.getAttribute("user_info")).getUserSn();
        Optional<User> opt = userRepository.findById(id);
        if(opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        User user = opt.get();

        if( !user.getUserNickname().equals(profileDTO.getUserNickname()) &&
                    userRepository.existsByUserNickname(profileDTO.getUserNickname())) {
            return ResponseEntity.badRequest().body("중복된 닉네임");
        }
        
        user.setUserName(profileDTO.getUserName());
        user.setEmail(profileDTO.getEmail());
        user.setUserNickname(profileDTO.getUserNickname());
        user.setUserPhone(profileDTO.getUserPhone());
        user.setUserPw(profileDTO.getUserPw());

        userRepository.save(user);

        return ResponseEntity.ok("Update successfully");
    }
    
    //비밀번호 확인
    @Operation(summary = "비밀번호 확인", description = "현재 로그인된 사용자의 비밀번호가 입력된 비밀번호와 일치하는지 확인합니다. (주로 회원 탈퇴 전 확인용)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "비밀번호 일치",
                         content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"success\": true}"))),
            @ApiResponse(responseCode = "400", description = "비밀번호 불일치 또는 잘못된 요청",
                         content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"success\": false, \"message\": \"비밀번호 불일치\"}"))),
            @ApiResponse(responseCode = "401", description = "로그인 필요 (인증되지 않음)", content = @Content)
    })
    @PostMapping("/mypage/profile/checkPw")
    public ResponseEntity<?> checkPw(
        @RequestBody Map<String,String> body,
        HttpSession session
     ) {
        User userInfo = (User) session.getAttribute("user_info");
        if(userInfo == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다");
        }

        int id = userInfo.getUserSn();

        Optional<User> opt = userRepository.findById(id);
        String pw = opt.get().getUserPw();
        String inputPw = body.get("pw");
        
        if(inputPw.equals(pw)) {
            return ResponseEntity.ok(Map.of("success",true));
        } else {
            return ResponseEntity.badRequest()
                    .body(Map.of("success",false,"message","비밀번호 불일치"));
        }
    }
    
}