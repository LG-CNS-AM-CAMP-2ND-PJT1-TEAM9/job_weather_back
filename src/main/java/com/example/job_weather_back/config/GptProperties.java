package com.example.job_weather_back.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
// import org.springframework.stereotype.Component; // @Component 제거

// @Component // 생성자 바인딩을 사용할 때는 @Component를 제거합니다.
@ConfigurationProperties(prefix = "gpt")
public class GptProperties {

    private static final Logger log = LoggerFactory.getLogger(GptProperties.class);

    private final String apiKey;
    private final String apiUrl;

    // Spring Boot 2.2 이상에서는 @ConfigurationProperties 클래스에 단일 생성자가 있으면
    // 해당 생성자를 사용하여 값을 바인딩합니다. @ConstructorBinding 어노테이션은 필수는 아닙니다.
    // 이 생성자가 있으므로 Spring Boot가 생성자 바인딩을 시도합니다.
    public GptProperties(String apiKey, String apiUrl) {
        log.info("GptProperties 생성자 호출됨. 전달된 apiKey 길이: {}, 전달된 apiUrl: {}",
                (apiKey != null && !apiKey.isEmpty() ? apiKey.length() : "null 또는 비어있음"), apiUrl);
        this.apiKey = apiKey;
        this.apiUrl = apiUrl;

        if (this.apiKey == null || this.apiKey.isEmpty()) {
            log.warn("GptProperties: apiKey가 null이거나 비어있습니다. application.properties 설정을 확인해주세요.");
        }
        if (this.apiUrl == null || this.apiUrl.isEmpty()) {
            log.warn("GptProperties: apiUrl이 null이거나 비어있습니다. application.properties 설정을 확인해주세요.");
        }
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    // Setter는 생성자 바인딩 시 프로퍼티 주입에 사용되지 않으므로 제거해도 무방합니다.
}
