package com.engineerbiz.notification.service.impl;

import com.engineerbiz.common.exception.ResourceNotFoundException;
import com.engineerbiz.common.pagination.PagedResponse;
import com.engineerbiz.identity.entity.User;
import com.engineerbiz.identity.repository.UserRepository;
import com.engineerbiz.notification.dto.CreateNotificationRequest;
import com.engineerbiz.notification.dto.NotificationResponse;
import com.engineerbiz.notification.entity.Notification;
import com.engineerbiz.notification.enums.NotificationChannel;
import com.engineerbiz.notification.enums.NotificationPriority;
import com.engineerbiz.notification.mapper.NotificationMapper;
import com.engineerbiz.notification.repository.NotificationRepository;
import com.engineerbiz.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationMapper notificationMapper;

    @Override
    @Transactional
    public NotificationResponse sendNotification(CreateNotificationRequest request) {
        User recipient = userRepository.findById(request.recipientId())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.recipientId()));

        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setTitle(request.title().trim());
        notification.setContent(request.content().trim());
        notification.setChannel(request.channel() != null ? request.channel() : NotificationChannel.IN_APP);
        notification.setPriority(request.priority() != null ? request.priority() : NotificationPriority.MEDIUM);
        notification.setReferenceEntityId(request.referenceEntityId());
        notification.setReferenceEntityType(request.referenceEntityType());
        notification.setIsRead(false);
        notification.setSentAt(Instant.now());

        Notification saved = notificationRepository.save(notification);
        log.info("Sent [{}] notification to user [{}]: {}",
                notification.getChannel(), recipient.getEmail(), notification.getTitle());
        return notificationMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<NotificationResponse> getUserNotifications(UUID recipientId, Pageable pageable) {
        if (!userRepository.existsById(recipientId)) {
            throw new ResourceNotFoundException("User", recipientId);
        }
        Page<Notification> page = notificationRepository.findByRecipientIdOrderBySentAtDesc(recipientId, pageable);
        return PagedResponse.from(page.map(notificationMapper::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(UUID recipientId) {
        return notificationRepository.countByRecipientIdAndIsReadFalse(recipientId);
    }

    @Override
    @Transactional
    public NotificationResponse markAsRead(UUID id, UUID currentUserId) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", id));

        if (!notification.getRecipient().getId().equals(currentUserId)) {
            log.warn("User [{}] attempted to read notification belonging to [{}]",
                    currentUserId, notification.getRecipient().getId());
        }

        notification.setIsRead(true);
        notification.setReadAt(Instant.now());
        Notification saved = notificationRepository.save(notification);
        return notificationMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void markAllAsRead(UUID recipientId) {
        notificationRepository.markAllAsReadForUser(recipientId, Instant.now());
        log.info("Marked all notifications as read for user [{}]", recipientId);
    }
}
