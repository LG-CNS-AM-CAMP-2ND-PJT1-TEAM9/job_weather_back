//디비연결x 데이터 전달용
package com.example.job_weather_back.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class KeywordMatch {
    private int id;
    private String type;
    private String name;
}