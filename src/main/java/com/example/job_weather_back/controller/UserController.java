package com.example.job_weather_back.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.job_weather_back.dto.LogInDto;
import com.example.job_weather_back.dto.SignUpDto;
import com.example.job_weather_back.entity.User;
import com.example.job_weather_back.repository.UserRepository;

import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/users")
public class UserController {
  @Autowired UserRepository userRepository;

  @Transactional
  @PostMapping("/signup")
  public User signupPost(
      @RequestBody SignUpDto dto) {
    User user = new User();
    user.setUserName(dto.getName());
    user.setUserNickname(dto.getNickname());
    user.setEmail(dto.getEmail());
    user.setUserPhone(dto.getPhone());
    user.setUserPw(dto.getPw());
    User savedUser = userRepository.save(user);
    System.out.println("Saved User SN: " + savedUser.getUserSn());
    return userRepository.save(user);
  }

  @GetMapping("/nickname") // 닉네임 존재 여부 찾기
  public boolean checkNickname(@RequestParam String nickname) {
    return !userRepository.existsByUserNickname(nickname);
  }

  @Transactional
  @PostMapping("/login")
  public ResponseEntity<User> loginPost(@RequestBody LogInDto dto, HttpSession session) {
    Optional<User> opt = userRepository.findByEmailAndUserPw(dto.getEmail(), dto.getPw());
    if (opt.isPresent()) {
      session.setAttribute("user_info", opt.get());
      return ResponseEntity.ok(opt.get());
    }
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
  }

  @Transactional
  @PostMapping("/reset-password")
  public ResponseEntity<User> resetPwPost(@RequestBody LogInDto dto) {
    Optional<User> opt = userRepository.findByEmail(dto.getEmail());
    User user = opt.get();
    user.setUserPw(dto.getPw());
    userRepository.save(user);
    return ResponseEntity.ok(user);
  }

  @GetMapping("/email") // 가입된 이메일 찾기
  public boolean checkemail(@RequestParam String email) {
    return userRepository.existsByEmail(email);
  }

}
