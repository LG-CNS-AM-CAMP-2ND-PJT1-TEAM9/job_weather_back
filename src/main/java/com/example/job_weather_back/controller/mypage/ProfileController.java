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

import jakarta.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@CrossOrigin
@RestController
public class ProfileController {
    @Autowired UserRepository userRepository;

    // 회원정보 조회
    @GetMapping("/mypage/profile")
    public User printProfile(HttpSession session) {
        User user = (User) session.getAttribute("user_info");
        int id = user.getUserSn();

        Optional<User> opt = userRepository.findById(id);
        User result = opt.get();

        return result;
    }
    
    //회원정보 수정
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