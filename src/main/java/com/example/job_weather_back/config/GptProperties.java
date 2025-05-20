package com.example.job_weather_back.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
// import org.springframework.validation.annotation.Validated; // 필요시 유효성 검사 추가
// import jakarta.validation.constraints.NotBlank; // @NotBlank 사용 시 (Spring Boot 3.x, Jakarta EE 9+)
// import javax.validation.constraints.NotEmpty; // @NotEmpty 사용 시 (Spring Boot 2.x, Java EE 8)


@Component // 이 클래스의 인스턴스를 Spring 빈으로 등록
@ConfigurationProperties(prefix = "gpt") // "gpt"로 시작하는 속성들을 이 클래스에 바인딩
// @Validated // 만약 아래 필드에 유효성 검사 어노테이션을 사용한다면 필요
public class GptProperties {

    /**
     * OpenAI GPT API Key.
     * application.properties 또는 환경 변수 (GPT_API_KEY)를 통해 설정됩니다.
     */
    // @NotBlank // API 키는 필수 값이므로 추가 가능 (메시지 지정 가능)
    private String apiKey;

    /**
     * OpenAI GPT API Endpoint URL.
     * application.properties 또는 환경 변수를 통해 설정됩니다.
     */
    // @NotBlank
    private String apiUrl;

    // Getter와 Setter가 필요합니다.
    // Lombok을 사용하고 있다면 @Getter @Setter 또는 @Data 어노테이션으로 대체 가능합니다.

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }
}
