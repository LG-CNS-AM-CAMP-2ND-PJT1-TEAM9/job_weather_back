package com.example.job_weather_back.controller;


import com.example.job_weather_back.entity.NewContents;
import com.example.job_weather_back.repository.NewContentsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/new-contents")
public class NewContentsController {

    private final NewContentsRepository newContentsRepository;

    // 전체 소식 조회 (뉴스 + 채용)
    @GetMapping
    public List<NewContents> getAll() {
        return newContentsRepository.findAll();
    }

    // 특정 타입(news/job)만 조회
    @GetMapping("/{type}")
    public List<NewContents> getByType(@PathVariable String type) {
        return newContentsRepository.findByType(type);
    }
}
