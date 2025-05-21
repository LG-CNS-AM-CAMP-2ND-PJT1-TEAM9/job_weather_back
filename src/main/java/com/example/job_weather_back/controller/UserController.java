package com.example.job_weather_back.controller;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.job_weather_back.dto.KakaoDto;
import com.example.job_weather_back.dto.LogInDto;
import com.example.job_weather_back.dto.SignUpDto;
import com.example.job_weather_back.entity.User;
import com.example.job_weather_back.repository.UserRepository;
import com.example.job_weather_back.service.KakaoService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@CrossOrigin
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
  private final KakaoService kakaoService;
  @Autowired
  UserRepository userRepository;

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

  @Value("${kakao.client_id}")
  private String client_id;

  @Value("${kakao.redirect_uri}")
  private String redirect_uri;

  // kakao 로그인 url
  @GetMapping("/kakaologin")
  public String getKakaoLoginUrl() {

    return "https://kauth.kakao.com/oauth/authorize"
        + "?response_type=code"
        + "&client_id=" + client_id
        + "&redirect_uri=" + redirect_uri
        + "&prompt=login";
  }

  // kako사용자 정보 반환, 로그인 회원가입
  @GetMapping("/social-login")
  public void kakaoCallback(@RequestParam String code, HttpServletResponse response, HttpSession session)
      throws IOException {

    String accessToken = kakaoService.getAccessToken(code);
    KakaoDto userInfo = kakaoService.getUserInfo(accessToken);
    String email = userInfo.getEmail();

    if (email == null || email.isEmpty()) {
      email = userInfo.getKakaoId() + "@kakao.com";
    }

    Optional<User> opt = userRepository.findByEmail(email);

    User user;
    if (opt.isPresent()) {
      user = opt.get();
    } else {

      user = new User();
      user.setEmail(email);
      user.setUserNickname(userInfo.getUserNickname());
      user.setUserName(userInfo.getUserNickname());
      user.setUserPw("kakaopw_" + UUID.randomUUID().toString().substring(0, 7) + "!");
      user.setUserSocialId("1");

      userRepository.save(user);
      User savedUser = userRepository.save(user);
      session.setAttribute("user_info", savedUser);
    }

    response.sendRedirect("http://localhost:5173/");
  }

  // 회원 탈퇴
  @DeleteMapping("/delete")
  public ResponseEntity<?> deleteUser(HttpSession session) {
    User userInfo = (User) session.getAttribute("user_info");

    if (userInfo == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인 되어있지 않습니다.");
    }
    int id = userInfo.getUserSn();
    userRepository.deleteById(id);
    session.invalidate();

    return ResponseEntity.ok("탈퇴 완료");
  }

}
