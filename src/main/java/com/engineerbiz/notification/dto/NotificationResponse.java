package com.engineerbiz.notification.dto;

import com.engineerbiz.notification.enums.NotificationChannel;
import com.engineerbiz.notification.enums.NotificationPriority;

import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        UUID recipientId,
        String recipientFullName,
        String recipientEmail,
        String title,
        String content,
        NotificationChannel channel,
        NotificationPriority priority,
        UUID referenceEntityId,
        String referenceEntityType,
        Boolean isRead,
        Instant readAt,
        Instant sentAt
) {}
