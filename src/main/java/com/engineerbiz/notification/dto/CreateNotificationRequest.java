package com.engineerbiz.notification.dto;

import com.engineerbiz.notification.enums.NotificationChannel;
import com.engineerbiz.notification.enums.NotificationPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateNotificationRequest(
        @NotNull(message = "Recipient user ID is required")
        UUID recipientId,

        @NotBlank(message = "Notification title is required")
        @Size(max = 150)
        String title,

        @NotBlank(message = "Notification content is required")
        String content,

        NotificationChannel channel,

        NotificationPriority priority,

        UUID referenceEntityId,

        @Size(max = 50)
        String referenceEntityType
) {}
