package com.example.job_weather_back.repository;


import com.example.job_weather_back.entity.User;
import com.example.job_weather_back.entity.UserMatchSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserMatchSettingRepository
        extends JpaRepository<UserMatchSetting, Integer> {
    List<UserMatchSetting> findByUser(User user);
    // 여기에 추가적인 쿼리 메서드를 추가할 수 있습니다.
}