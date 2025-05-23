package com.example.job_weather_back.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.example.job_weather_back.dto.KakaoDto;
import com.example.job_weather_back.dto.LogInDto;
import com.example.job_weather_back.dto.NaverDto;
import com.example.job_weather_back.dto.SignUpDto;
import com.example.job_weather_back.entity.User;
import com.example.job_weather_back.repository.CustomizationRepository;
import com.example.job_weather_back.repository.LikedNewsRepository;
import com.example.job_weather_back.repository.UserRepository;
import com.example.job_weather_back.service.KakaoService;
import com.example.job_weather_back.service.NaverService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
//import io.swagger.v3.oas.annotations.parameters.RequestBody;//
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.*;

@Tag(name = "User API", description = "사용자 인증, 관리 및 소셜 로그인 관련 API")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

  private final KakaoService kakaoService;
  private final NaverService naverService;
  @Autowired
  UserRepository userRepository;
  @Autowired
  CustomizationRepository customizationRepository;
  @Autowired
  LikedNewsRepository likedNewsRepository;

  @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원가입 성공",
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content)
    })
  @Transactional
  @PostMapping("/signup")
  public User signupPost(@RequestBody SignUpDto dto) {
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


  @Operation(summary = "로그인", description = "이메일과 비밀번호를 사용하여 로그인하고 세션을 생성합니다.")
  @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인 성공",
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패 (이메일 또는 비밀번호 불일치)", content = @Content)
  })
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

  @Operation(summary = "닉네임 중복 확인", description = "제공된 닉네임의 사용 가능 여부를 확인합니다. (true: 사용 가능, false: 이미 사용 중)")
  @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "확인 결과 반환",
                         content = @Content(mediaType = "application/json", schema = @Schema(type = "boolean")))
  })
  @GetMapping("/nickname") // 닉네임 존재 여부 찾기
  public boolean checkNickname(@RequestParam String nickname) {
    return !userRepository.existsByUserNickname(nickname);
  }

  @Operation(summary = "비밀번호 재설정", description = "이메일 확인 후 새 비밀번호로 변경합니다. (현재 로직은 이메일만으로 사용자를 찾아 비밀번호를 변경합니다. 실제 구현 시 추가 인증 필요)")
  @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "비밀번호 재설정 성공",
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "404", description = "해당 이메일의 사용자 없음", content = @Content)
  })
  @Transactional
  @PostMapping("/reset-password")
  public ResponseEntity<User> resetPwPost(@RequestBody LogInDto dto) {
    Optional<User> opt = userRepository.findByEmail(dto.getEmail());
    User user = opt.get();
    user.setUserPw(dto.getPw());
    userRepository.save(user);
    return ResponseEntity.ok(user);
  }

  @Operation(summary = "이메일 가입 여부 확인", description = "제공된 이메일이 이미 가입되어 있는지 확인합니다. (true: 이미 가입됨, false: 가입되지 않음)")
  @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "확인 결과 반환",
                         content = @Content(mediaType = "application/json", schema = @Schema(type = "boolean")))
  })
  @GetMapping("/email") // 가입된 이메일 찾기
  public boolean checkemail(@RequestParam String email) {
    return userRepository.existsByEmail(email);
  }

  @Value("${kakao.client_id}")
  private String client_id;

  @Value("${kakao.redirect_uri}")
  private String redirect_uri;

  @Value("${frontend.redirect.url}")
  private String frontendRedirectUrl;

  // kakao 로그인 url
  @Operation(summary = "카카오 로그인 URL 요청", description = "카카오 인증을 위한 인가 코드 요청 URL을 반환합니다. 클라이언트는 이 URL로 리다이렉트해야 합니다.")
  @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "카카오 로그인 URL 문자열",
                         content = @Content(mediaType = "text/plain", schema = @Schema(type = "string", example = "https://kauth.kakao.com/oauth/authorize?...")))
  })
  @GetMapping("/kakaologin")
  public String getKakaoLoginUrl() {

    return "https://kauth.kakao.com/oauth/authorize"
        + "?response_type=code"
        + "&client_id=" + client_id
        + "&redirect_uri=" + redirect_uri;
  }

  // kako사용자 정보 반환, 로그인 회원가입
  @Operation(summary = "카카오 로그인 콜백 처리 (GET)", description = "카카오로부터 인가 코드를 받아 로그인/회원가입 처리 후 메인 페이지로 리다이렉트합니다. (API 명세 테스트 어려움)")
  @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "메인 페이지(http://localhost:5173/)로 리다이렉트")
  })
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
      session.setAttribute("user_info", user);
      session.setAttribute("access_token", accessToken);
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
      session.setAttribute("access_token", accessToken);
    }

    response.sendRedirect(frontendRedirectUrl);
  }

  // 회원 탈퇴
  @Operation(summary = "회원 탈퇴", description = "현재 로그인된 사용자의 정보를 삭제하고 세션을 무효화합니다. 소셜 로그인 사용자의 경우 연결 끊기를 시도합니다.")
  @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "탈퇴 성공", content = @Content(mediaType = "text/plain")),
            @ApiResponse(responseCode = "401", description = "로그인 필요 (인증되지 않음)", content = @Content)
  })
  @Transactional
  @DeleteMapping("/delete")
  public ResponseEntity<?> deleteUser(HttpSession session) {
    User userInfo = (User) session.getAttribute("user_info");

    if (userInfo == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인 되어있지 않습니다.");
    }

    String accessToken = (String) session.getAttribute("access_token");
    String socialType = userInfo.getUserSocialId();

    if (accessToken != null) {
      switch (socialType) {
        case "1": // 카카오
          kakaoService.kakaoDelete(accessToken);
          break;
        case "2": // 네이버
          naverService.naverDelete(accessToken);
          break;
      }
    }

    int id = userInfo.getUserSn();
    likedNewsRepository.deleteByUser(userInfo);
    customizationRepository.deleteByUser(userInfo);
    userRepository.deleteById(id);
    session.invalidate();

    return ResponseEntity.ok("탈퇴 완료");
  }


  @Operation(summary = "로그아웃", description = "현재 로그인된 사용자의 세션을 무효화하고, 소셜 로그인 사용자의 경우 로그아웃 처리 후 메인 페이지로 리다이렉트합니다.")
  @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "메인 페이지 또는 소셜 로그아웃 페이지로 리다이렉트")
  })
  @PostMapping("/logout")
  public ResponseEntity<String> logout(HttpSession session, HttpServletResponse response) throws IOException {
    String accessToken = (String) session.getAttribute("access_token");
    User userInfo = (User) session.getAttribute("user_info");

    if(userInfo == null) {
      session.invalidate();
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인 상태 아님");
    }

    if (accessToken != null && userInfo != null) {
      String socialType = userInfo.getUserSocialId();

      if("1".equals(socialType)) {
        try {
            kakaoService.kakaoLogout(accessToken);
        } catch (Exception e) {
            e.printStackTrace(); // 서버 로그에 에러 상세 찍기
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("카카오 로그아웃 처리 중 오류 발생");
        }
      } else if("2".equals(socialType)) {
        session.invalidate();
          return ResponseEntity.ok("네이버 로그아웃 완료");
      }
      // switch (socialType) {
      //   case "1": // 카카오
      //     try {
      //       kakaoService.kakaoLogout(accessToken);
      //   } catch (Exception e) {
      //       e.printStackTrace(); // 서버 로그에 에러 상세 찍기
      //       return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("카카오 로그아웃 처리 중 오류 발생");
      //   }
      //     break;
      //   case "2": // 네이버
      //     // response.sendRedirect("https://nid.naver.com/nidlogin.logout");
      //     session.invalidate();
      //     return ResponseEntity.ok("네이버 로그아웃 완료");
      //   default:
      //     break;
      // }
    }
    
    session.invalidate();
    return ResponseEntity.ok("로그아웃 성공");
  }

  @Value("${naver.client_id}")
  private String naver_client_id;

  @Value("${naver.redirect_uri}")
  private String naver_redirect_uri;

  @Value("${naver.client_secret}")
  private String naver_client_secret;

  // naver 로그인 url
  @Operation(summary = "네이버 로그인 URL 요청", description = "네이버 인증을 위한 인가 코드 요청 URL을 반환합니다. 클라이언트는 이 URL로 리다이렉트해야 합니다.")
  @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "네이버 로그인 URL 문자열",
                         content = @Content(mediaType = "text/plain", schema = @Schema(type = "string", example = "https://nid.naver.com/oauth2.0/authorize?...")))
  })
  @GetMapping("/naverlogin")
  public String getNaverLoginUrl() {

    return "https://nid.naver.com/oauth2.0/authorize"
        + "?response_type=code"
        + "&client_id=" + naver_client_id
        + "&redirect_uri=" + naver_redirect_uri
        + "&state=" + naver_client_secret;
  }

  // naverlogin
  @Operation(summary = "네이버 로그인 콜백 처리 (GET)", description = "네이버로부터 인가 코드를 받아 로그인/회원가입 처리 후 메인 페이지로 리다이렉트합니다. (API 명세 테스트 어려움)")
  @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "메인 페이지(http://localhost:5173/)로 리다이렉트")
  })
  @GetMapping("/naver-login")
  public void naverCallback(@RequestParam String code, HttpServletResponse response, HttpSession session)
      throws IOException {

    String accessToken = naverService.getAccessToken(code);
    NaverDto userInfo = naverService.getUserInfo(accessToken);
    String email = userInfo.getEmail();

    if (email == null || email.isEmpty()) {
      email = userInfo.getNaverId() + "@naver.com";
    }

    Optional<User> opt = userRepository.findByEmail(email);

    User user;
    if (opt.isPresent()) {
      user = opt.get();
      session.setAttribute("user_info", user);
      session.setAttribute("access_token", accessToken);
    } else {
      user = new User();
      user.setEmail(email);
      user.setUserNickname(userInfo.getUserNickname());
      user.setUserName(userInfo.getUserNickname());
      user.setUserPw("naverpw_" + UUID.randomUUID().toString().substring(0, 7) + "!");
      user.setUserSocialId("2");

      userRepository.save(user);
      User savedUser = userRepository.save(user);
      session.setAttribute("user_info", savedUser);
      session.setAttribute("access_token", accessToken);

    }

    response.sendRedirect(frontendRedirectUrl);
  }

  @GetMapping("/info")
  public ResponseEntity<?> getUserInfo(HttpSession session) {
      Object userObj = session.getAttribute("user_info");
      if (userObj == null || !(userObj instanceof User)) {
          return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인 필요");
      }

      User user = (User) userObj;
      String nickname = user.getUserNickname(); // 또는 user.getNickname();

      Map<String, Object> response = new HashMap<>();
      response.put("nickname", nickname);

      return ResponseEntity.ok(response);
  }

}
