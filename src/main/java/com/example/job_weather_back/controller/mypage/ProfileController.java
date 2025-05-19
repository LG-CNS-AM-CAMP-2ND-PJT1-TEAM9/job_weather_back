package com.example.job_weather_back.controller.mypage;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.example.job_weather_back.dto.ProfileDTO;
import com.example.job_weather_back.entity.User;
import com.example.job_weather_back.repository.UserRepository;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@CrossOrigin
@RestController
public class ProfileController {
    @Autowired UserRepository userRepository;

    @GetMapping("mypage/profile/{id}")
    public User printProfile(@PathVariable int id) {
        Optional<User> opt = userRepository.findById(id);
        User user = opt.get();

        return user;
    }
    
    @PostMapping("mypage/profile")
    public ResponseEntity<String> updateProfile(
        @RequestBody ProfileDTO profileDTO
    ) {
        int id = profileDTO.getUserSn();
        Optional<User> opt = userRepository.findByUserSn(id);
        User user = opt.get();

        if(opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        } else if( !user.getUserNickname().equals(profileDTO.getUserNickname()) &&
                    userRepository.existsByUserNickname(profileDTO.getUserNickname())) {
            return ResponseEntity.badRequest().body("중복된 닉네임");
        }

        user.setUserName(profileDTO.getUserNickname());
        user.setUserPhone(profileDTO.getUserPhone());
        user.setUserPw(profileDTO.getUserPw());

        userRepository.save(user);

        return ResponseEntity.ok("Update successfully");
    }
    
}