package com.example.job_weather_back.weather.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GptChatResponseDto {
    private String id;
    private String object;
    private long created;
    private String model;
    private List<Choice> choices;
    private Usage usage;
    // 에러 발생 시를 위한 error 필드 (선택적)
    // private Error error;


    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Choice {
        private int index;
        private GptChatMessageDto message;
        private String finish_reason;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Usage {
        private int prompt_tokens;
        private int completion_tokens;
        private int total_tokens;
    }

    // @Getter
    // @Setter
    // @NoArgsConstructor
    // @AllArgsConstructor
    // public static class Error {
    //    private String message;
    //    private String type;
    //    private String param;
    //    private String code;
    // }
}