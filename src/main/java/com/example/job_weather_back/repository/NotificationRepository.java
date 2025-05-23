package com.example.job_weather_back.repository;

<<<<<<< HEAD
=======

import com.example.job_weather_back.entity.NewContents;
>>>>>>> b4f1a8eaec03cc7fbaccc36024fc85994059880d
import com.example.job_weather_back.entity.Notification;
import com.example.job_weather_back.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

<<<<<<< HEAD
public interface NotificationRepository 
extends JpaRepository<Notification, Integer> {
=======
public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    boolean existsByUserAndNewContents(User user, NewContents newContents);
>>>>>>> b4f1a8eaec03cc7fbaccc36024fc85994059880d
    List<Notification> findByUser(User user);
    List<Notification> findAllByUser_UserSn(Integer userSn);

    @Query("SELECT n FROM Notification n join fetch n.newContents JOIN FETCH n.user where n.user.userSn = :userSn")
    List<Notification> findAllWithRelations(@Param("userSn") int userSn);
<<<<<<< HEAD
}
=======
}
>>>>>>> b4f1a8eaec03cc7fbaccc36024fc85994059880d
