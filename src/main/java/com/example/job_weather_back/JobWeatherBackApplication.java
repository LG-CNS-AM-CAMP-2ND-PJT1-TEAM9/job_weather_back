package com.example.job_weather_back;

import com.example.job_weather_back.config.GptProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableConfigurationProperties(GptProperties.class)
@EnableScheduling
public class JobWeatherBackApplication {

	public static void main(String[] args) {
		SpringApplication.run(JobWeatherBackApplication.class, args);
	}

}
