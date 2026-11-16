package com.engineerbiz.notification.service;

import com.engineerbiz.common.pagination.PagedResponse;
import com.engineerbiz.notification.dto.CreateNotificationRequest;
import com.engineerbiz.notification.dto.NotificationResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface NotificationService {

    NotificationResponse sendNotification(CreateNotificationRequest request);

    PagedResponse<NotificationResponse> getUserNotifications(UUID recipientId, Pageable pageable);

    long getUnreadCount(UUID recipientId);

    NotificationResponse markAsRead(UUID id, UUID currentUserId);

    void markAllAsRead(UUID recipientId);
}
