package com.example.job_weather_back.dto;

import com.example.job_weather_back.entity.Notification;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDto {

    private Long notificationsId;

    private String title;
    private String content;
    private String type;

    private Boolean isRead;

    public NotificationDto(Notification notification) {
        this.notificationsId = notification.getNotificationsId();
        this.title = notification.getNewContents().getTitle();
        this.content = notification.getNewContents().getContent();
        this.type = notification.getNewContents().getType();
        this.isRead = notification.getIsRead();
    }
}