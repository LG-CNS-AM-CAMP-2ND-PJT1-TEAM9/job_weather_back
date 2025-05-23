package com.example.job_weather_back.controller;


import com.example.job_weather_back.entity.NewContents;
import com.example.job_weather_back.repository.NewContentsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@Tag(name = "New Contents API", description = "새로운 소식(뉴스/채용 정보 통합) 조회 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/new-contents")
public class NewContentsController {

    private final NewContentsRepository newContentsRepository;

    // 전체 소식 조회 (뉴스 + 채용)
    @Operation(summary = "모든 새로운 소식 조회", description = "시스템에 저장된 모든 새로운 소식(뉴스 및 채용 정보 통합) 목록을 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                         content = @Content(mediaType = "application/json",
                         schema = @Schema(type = "array", implementation = NewContents.class)))
            // 필요시 500 에러 응답 등 추가
    })
    @GetMapping
    public List<NewContents> getAll() {
        return newContentsRepository.findAll();
    }

    // 특정 타입(news/job)만 조회
    @Operation(summary = "특정 타입의 새로운 소식 조회", description = "경로 변수로 전달된 타입('news' 또는 'job')에 해당하는 새로운 소식 목록을 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                         content = @Content(mediaType = "application/json",
                         schema = @Schema(type = "array", implementation = NewContents.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 타입 파라미터 (예: 'news' 또는 'job'이 아닌 경우)", content = @Content),
            @ApiResponse(responseCode = "404", description = "해당 타입의 소식 없음 (빈 리스트 반환 가능)", content = @Content)
    })
    @GetMapping("/{type}")
    public List<NewContents> getByType(@PathVariable String type) {
        return newContentsRepository.findByType(type);
    }
}
