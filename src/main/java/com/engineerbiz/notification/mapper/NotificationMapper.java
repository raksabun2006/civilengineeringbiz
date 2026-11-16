package com.engineerbiz.notification.mapper;

import com.engineerbiz.notification.dto.NotificationResponse;
import com.engineerbiz.notification.entity.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    @Mapping(target = "recipientId", source = "recipient.id")
    @Mapping(target = "recipientFullName", source = "recipient.fullName")
    @Mapping(target = "recipientEmail", source = "recipient.email")
    NotificationResponse toResponse(Notification notification);

    List<NotificationResponse> toResponseList(List<Notification> notifications);
}
