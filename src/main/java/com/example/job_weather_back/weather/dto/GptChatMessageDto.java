package com.example.job_weather_back.weather.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GptChatMessageDto {
    private String role;    // "system", "user", "assistant"
    private String content;
}