package com.example.job_weather_back.controller;

//테스트용 뉴스랑 채용공고 데이터 준비
//나중에 제목과 타이틀 API 연결 (응답호출)

import com.example.job_weather_back.entity.NewContents;
import com.example.job_weather_back.service.NewContentsService;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@Profile("dev")
@RequestMapping("/api/test")
public class InternalTestController {

    private final NewContentsService newContentsService;

    // 테스트용 뉴스 데이터 삽입
    @PostMapping("/news")
    public String testNewsToNotification(@RequestBody List<NewContents> newContentsList) {
        // 테스트용 뉴스 데이터 삽입
//        String title1 = "카카오 프론트엔드 채용 시작"
//        String content1 = "카카오는 서울 본사에서 프론트엔드 직무 신입을 모집합니다."
//
//        String title2 = "삼성전자 UX 디자이너 모집 공고"
//        String content2 = "삼성전자는 서울 R&D 센터에서 UX 디자이너를 찾고 있습니다."
//
//        String title3 = "스타트업 프론트엔드 인턴 채용"
//        String content3 = "성장하는 스타트업에서 프론트엔드 직무 인턴을 채용합니다. 근무지는 부산입니다."
//
//        String title4 = "카카오 데이터 분석가 모집"
//        String content4 = "카카오는 데이터 기반 의사결정을 위한 분석가를 서울에서 모집 중입니다."
//
//        String title5 = "라인 대기업 클라우드 엔지니어 채용"
//        String content5 = "라인은 대기업 클라우드 인프라팀에서 엔지니어를 채용하며, 근무지는 제주입니다."
//
//        newContentsService.saveNewContents("news", title1, content1)
//        newContentsService.saveNewContents("news", title2, content2)
//        newContentsService.saveNewContents("news", title3, content3)
//        newContentsService.saveNewContents("news", title4, content4)
//        newContentsService.saveNewContents("news", title5, content5)

        int size = saveNewContents(newContentsList);
        return "테스트 뉴스 " + size + "건이 저장되고 알림이 생성되었습니다!";
    }


    @PostMapping("/job")
    public String testJobToNotification(@RequestBody List<NewContents> newContentsList) {

        // 테스트용 채용공고 데이터 삽입
//        String title1 = "네이버 백엔드 개발자 채용 공고"
//        String content1 = "네이버는 Java 기반 백엔드 개발자를 모집하며 근무지는 분당입니다."
//
//        String title2 = "쿠팡 데이터 엔지니어 채용"
//        String content2 = "쿠팡은 데이터 파이프라인 구축 경험이 있는 엔지니어를 서울 본사에서 모집 중입니다."
//
//        String title3 = "카카오 보안 전문가 모집"
//        String content3 = "카카오는 보안 기술 및 인프라 운영 경험이 있는 전문가를 찾고 있습니다."
//
//        newContentsService.saveNewContents("job", title1, content1)
//        newContentsService.saveNewContents("job", title2, content2)
//        newContentsService.saveNewContents("job", title3, content3)

        int size = saveNewContents(newContentsList);
        return "테스트 채용공고 " + size + "건이 저장되고 알림이 생성되었습니다!";
    }

    private int saveNewContents(List<NewContents> newContentsList) {
        for (NewContents newContents : newContentsList) {
            newContentsService.saveNewContents(newContents.getType(), newContents.getTitle(), newContents.getContent());
        }

        return newContentsList.size();
    }
}