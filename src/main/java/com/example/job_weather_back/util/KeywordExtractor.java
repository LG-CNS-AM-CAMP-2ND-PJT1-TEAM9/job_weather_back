//본문 텍스트에서 사전 등록된 키워드를 찾아내는 역할

package com.example.job_weather_back.util;

import com.example.job_weather_back.entity.KeywordMaster;
import com.example.job_weather_back.repository.KeywordMasterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class KeywordExtractor {

    private final KeywordMasterRepository keywordMasterRepository;

    // 사전 키워드 모두 반환
    public List<String> extractAsStringList(String text) {
        List<String> result = new ArrayList<>();

        List<KeywordMaster> keywordList = keywordMasterRepository.findAll();
        for (KeywordMaster keyword : keywordList) {
            if (text.contains(keyword.getName())) {
                result.add(keyword.getName());
            }
        }

        return result;
    }

    // 상위 3개 키워드만 반환
    public List<String> extractTop3AsStringList(String text) {
        List<String> result = new ArrayList<>();

        List<KeywordMaster> keywordList = keywordMasterRepository.findAll();
        for (KeywordMaster keyword : keywordList) {
            if (text.contains(keyword.getName())) {
                result.add(keyword.getName());
            }
            if (result.size() == 3) break;
        }

        return result;
    }
}