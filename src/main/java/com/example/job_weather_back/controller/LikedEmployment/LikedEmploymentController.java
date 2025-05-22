package com.example.job_weather_back.controller.LikedEmployment;

import com.example.job_weather_back.entity.LikedEmployment;
import com.example.job_weather_back.entity.User;
import com.example.job_weather_back.repository.LikedEmploymentRepository;
import com.example.job_weather_back.dto.LikedEmploymentDTO;

import jakarta.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@CrossOrigin
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/bookmarks")
public class LikedEmploymentController {


    private final LikedEmploymentRepository likedEmploymentRepository;

    private User getLoggedInUser(HttpSession session) {
        User userInfo = (User) session.getAttribute("user_info");
        return userInfo;
    }

    @PostMapping
    public ResponseEntity<?> addBookmark(@RequestBody LikedEmploymentDTO dto, HttpSession session) {
        User userInfo = getLoggedInUser(session);
        if (userInfo == null) {
            log.warn("비로그인 사용자 즐겨찾기 추가 시도");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        int empNumInt;
        try {
            empNumInt = Integer.parseInt(dto.getSaraminJobId());
        } catch (NumberFormatException e) {
            log.error("유효하지 않은 채용 공고 ID 형식: {}", dto.getSaraminJobId(), e);
             return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("유효하지 않은 채용 공고 ID 형식입니다.");
        }

        boolean alreadyBookmarked = likedEmploymentRepository.existsByUserAndEmpNum(userInfo, empNumInt);

        if (alreadyBookmarked) {
            log.info("이미 즐겨찾기된 공고 추가 시도: UserSn={}, JobId={}", userInfo.getUserSn(), empNumInt);
            return ResponseEntity.status(HttpStatus.CONFLICT).body("이미 즐겨찾기된 공고입니다.");
        }

        try {
            LikedEmployment newBookmark = new LikedEmployment();
            newBookmark.setUser(userInfo);
            newBookmark.setEmpNum(empNumInt);

            LikedEmployment savedBookmark = likedEmploymentRepository.save(newBookmark);
            log.info("즐겨찾기 추가 성공: UserSn={}, JobId={}, BookmarkId={}", userInfo.getUserSn(), empNumInt, savedBookmark.getLikedEmpId());

            return ResponseEntity.status(HttpStatus.CREATED).body(savedBookmark);
        } catch (Exception e) {
            log.error("즐겨찾기 추가 중 오류 발생: UserSn={}, JobId={}", userInfo.getUserSn(), empNumInt, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("즐겨찾기 추가 중 오류가 발생했습니다.");
        }
    }


    @DeleteMapping("/{saraminJobId}")
    public ResponseEntity<?> removeBookmark(@PathVariable String saraminJobId, HttpSession session) {
        User userInfo = getLoggedInUser(session);
        if (userInfo == null) {
            log.warn("비로그인 사용자 즐겨찾기 삭제 시도");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        int empNumInt;
        try {
            empNumInt = Integer.parseInt(saraminJobId);
        } catch (NumberFormatException e) {
            log.error("유효하지 않은 채용 공고 ID 형식: {}", saraminJobId, e);
             return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("유효하지 않은 채용 공고 ID 형식입니다.");
        }

         Optional<LikedEmployment> optionalBookmark = likedEmploymentRepository.findByUserAndEmpNum(userInfo, empNumInt);

        if (!optionalBookmark.isPresent()) {
            log.warn("존재하지 않는 즐겨찾기 삭제 시도: UserSn={}, JobId={}", userInfo.getUserSn(), empNumInt);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("해당 즐겨찾기 기록을 찾을 수 없습니다.");
        }


        try {
            likedEmploymentRepository.delete(optionalBookmark.get());
            log.info("즐겨찾기 삭제 성공: UserSn={}, JobId={}", userInfo.getUserSn(), empNumInt);
            return ResponseEntity.ok("즐겨찾기가 삭제되었습니다.");
        } catch (Exception e) {
            log.error("즐겨찾기 삭제 중 오류 발생: UserSn={}, JobId={}", userInfo.getUserSn(), empNumInt, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("즐겨찾기 삭제 중 오류가 발생했습니다.");
        }
    }


    @GetMapping("/user/{userSn}")
public ResponseEntity<?> getUserBookmarks(@PathVariable int userSn, HttpSession session) {
    User userInfo = getLoggedInUser(session);

    if (userInfo == null || userInfo.getUserSn() != userSn) {
        log.warn("다른 사용자의 즐겨찾기 목록 조회 시도 또는 비로그인 사용자 접근");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("접근 권한이 없습니다.");
    }

    try {
       List<LikedEmployment> bookmarks = likedEmploymentRepository.findByUserUserSn(userInfo.getUserSn());

        log.info("사용자 즐겨찾기 목록 조회 성공: UserSn={}, Count={}", userSn, bookmarks.size());

         List<Integer> bookmarkedEmpNums = bookmarks.stream()
                                                   .map(LikedEmployment::getEmpNum)
                                                   .collect(Collectors.toList());

        return ResponseEntity.ok(bookmarkedEmpNums);

    } catch (Exception e) {
        log.error("사용자 즐겨찾기 목록 조회 중 오류 발생: UserSn={}", userSn, e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("즐겨찾기 목록 조회 중 오류가 발생했습니다.");
    }
}
}
