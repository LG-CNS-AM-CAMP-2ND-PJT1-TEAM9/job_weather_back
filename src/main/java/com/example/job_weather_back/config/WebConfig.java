package com.example.job_weather_back.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")  
                .allowedOrigins(
                    "http://localhost:5173",
                    "https://good-job-today.netlify.app",
                    "http://ec2-3-35-169-97.ap-northeast-2.compute.amazonaws.com",
                    "https://web-job-weather-front-maz1o5u082e9fc0a.sel4.cloudtype.app"
                    )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true); 
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
