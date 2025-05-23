package com.example.job_weather_back.repository;


import com.example.job_weather_back.entity.NewContents;
import com.example.job_weather_back.entity.Notification;
import com.example.job_weather_back.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    boolean existsByUserAndNewContents(User user, NewContents newContents);
    List<Notification> findByUser(User user);
    List<Notification> findAllByUser_UserSn(Integer userSn);

    @Query("SELECT n FROM Notification n join fetch n.newContents JOIN FETCH n.user where n.user.userSn = :userSn")
    List<Notification> findAllWithRelations(@Param("userSn") int userSn);
}
