package com.engineerbiz.notification.service;

import com.engineerbiz.identity.entity.User;
import com.engineerbiz.identity.repository.UserRepository;
import com.engineerbiz.notification.dto.CreateNotificationRequest;
import com.engineerbiz.notification.dto.NotificationResponse;
import com.engineerbiz.notification.entity.Notification;
import com.engineerbiz.notification.enums.NotificationChannel;
import com.engineerbiz.notification.enums.NotificationPriority;
import com.engineerbiz.notification.mapper.NotificationMapper;
import com.engineerbiz.notification.repository.NotificationRepository;
import com.engineerbiz.notification.service.impl.NotificationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private NotificationMapper notificationMapper;

    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationServiceImpl(notificationRepository, userRepository, notificationMapper);
    }

    @Test
    @DisplayName("Should send notification to recipient user")
    void testSendNotificationSuccess() {
        UUID recipientId = UUID.randomUUID();
        User recipient = new User();
        recipient.setId(recipientId);
        recipient.setEmail("engineer@engineerbiz.com");
        recipient.setFullName("John Civil");

        CreateNotificationRequest request = new CreateNotificationRequest(
                recipientId,
                "Milestone Inspection Scheduled",
                "Inspection for Substructure is scheduled on tomorrow at 9 AM",
                NotificationChannel.IN_APP,
                NotificationPriority.HIGH,
                UUID.randomUUID(),
                "Project"
        );

        when(userRepository.findById(recipientId)).thenReturn(Optional.of(recipient));

        Notification saved = new Notification();
        saved.setId(UUID.randomUUID());
        saved.setTitle(request.title());
        saved.setIsRead(false);

        when(notificationRepository.save(any(Notification.class))).thenReturn(saved);

        NotificationResponse expected = new NotificationResponse(
                saved.getId(), recipientId, "John Civil", "engineer@engineerbiz.com",
                request.title(), request.content(), NotificationChannel.IN_APP,
                NotificationPriority.HIGH, request.referenceEntityId(),
                request.referenceEntityType(), false, null, Instant.now()
        );
        when(notificationMapper.toResponse(saved)).thenReturn(expected);

        NotificationResponse response = notificationService.sendNotification(request);

        assertThat(response).isNotNull();
        assertThat(response.title()).isEqualTo("Milestone Inspection Scheduled");
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    @DisplayName("Should mark notification as read")
    void testMarkAsRead() {
        UUID notificationId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        User recipient = new User();
        recipient.setId(userId);

        Notification notification = new Notification();
        notification.setId(notificationId);
        notification.setRecipient(recipient);
        notification.setIsRead(false);

        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        NotificationResponse readResponse = new NotificationResponse(
                notificationId, userId, "John", "j@e.com", "Title", "Content",
                NotificationChannel.IN_APP, NotificationPriority.MEDIUM, null, null,
                true, Instant.now(), Instant.now()
        );
        when(notificationMapper.toResponse(notification)).thenReturn(readResponse);

        NotificationResponse response = notificationService.markAsRead(notificationId, userId);

        assertThat(response.isRead()).isTrue();
        assertThat(notification.getIsRead()).isTrue();
        assertThat(notification.getReadAt()).isNotNull();
    }
}
