package com.toolmate.toolmate_api.service;

import com.toolmate.toolmate_api.entity.Notification;
import com.toolmate.toolmate_api.entity.NotificationType;
import com.toolmate.toolmate_api.entity.User;
import com.toolmate.toolmate_api.repository.NotificationRepository;
import com.toolmate.toolmate_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    /**
     * Create a professional notification
     */
    @Transactional
    public void createNotification(
            User user,
            NotificationType type,
            String customMessage,
            Long relatedId,
            String priority
    ) {
        try {
            Notification notification = Notification.builder()
                    .user(user)
                    .title(type.getEmoji() + " " + type.getTitle())
                    .message(customMessage)
                    .type(type)
                    .relatedId(relatedId)
                    .priority(priority != null ? priority : "NORMAL")
                    .build();

            notificationRepository.save(notification);
            log.info("Notification created: {} for user: {}", type, user.getId());

        } catch (Exception e) {
            log.error("Failed to create notification for user: {}", user.getId(), e);
        }
    }

    // ========== HELPER METHODS FOR EACH NOTIFICATION TYPE ==========

    /**
     * REQUEST_RECEIVED: When someone requests to borrow your tool
     */
    public void notifyRequestReceived(User owner, String borrowerName, String toolName, Long requestId) {
        String message = String.format(
                "%s wants to borrow your %s. Tap to view details and respond.",
                borrowerName, toolName
        );
        createNotification(owner, NotificationType.REQUEST_RECEIVED, message, requestId, "HIGH");
    }

    /**
     * REQUEST_ACCEPTED: When owner accepts your borrow request
     */
    public void notifyRequestAccepted(User borrower, String toolName, String ownerName, String ownerPhone, Long requestId) {
        String message = String.format(
                "Great news! %s accepted your request for %s. Contact: %s",
                ownerName, toolName, ownerPhone
        );
        createNotification(borrower, NotificationType.REQUEST_ACCEPTED, message, requestId, "HIGH");
    }

    /**
     * REQUEST_REJECTED: When owner rejects your borrow request
     */
    public void notifyRequestRejected(User borrower, String toolName, String ownerName, Long requestId) {
        String message = String.format(
                "%s declined your request for %s. Browse other tools nearby.",
                ownerName, toolName
        );
        createNotification(borrower, NotificationType.REQUEST_REJECTED, message, requestId, "NORMAL");
    }

    /**
     * TOOL_COLLECTED: When borrower picks up your tool
     */
    public void notifyToolCollected(User owner, String borrowerName, String toolName, Long requestId) {
        String message = String.format(
                "%s has picked up your %s. The item is now in their possession.",
                borrowerName, toolName
        );
        createNotification(owner, NotificationType.TOOL_COLLECTED, message, requestId, "NORMAL");
    }

    /**
     * TOOL_RETURNED: When borrower returns your tool
     */
    public void notifyToolReturned(User owner, String borrowerName, String toolName, Long requestId) {
        String message = String.format(
                "%s has returned your %s. Please inspect and confirm receipt.",
                borrowerName, toolName
        );
        createNotification(owner, NotificationType.TOOL_RETURNED, message, requestId, "HIGH");
    }

    /**
     * TRANSACTION_COMPLETED: When transaction is complete
     */
    public void notifyTransactionCompleted(User user, String otherUserName, String toolName, Long requestId) {
        String message = String.format(
                "Transaction complete! Your %s experience with %s. Rate your experience now.",
                toolName, otherUserName
        );
        createNotification(user, NotificationType.TRANSACTION_COMPLETED, message, requestId, "NORMAL");
    }

    /**
     * REVIEW_REMINDER: Remind user to write review
     */
    public void notifyReviewReminder(User user, String otherUserName, Long requestId) {
        String message = String.format(
                "Don't forget to review %s! Your feedback builds trust in our community.",
                otherUserName
        );
        createNotification(user, NotificationType.REVIEW_REMINDER, message, requestId, "LOW");
    }

    /**
     * REQUEST_CANCELLED: When request is cancelled
     */
    public void notifyRequestCancelled(User user, String otherUserName, String toolName, Long requestId) {
        String message = String.format(
                "%s cancelled the request for %s.",
                otherUserName, toolName
        );
        createNotification(user, NotificationType.REQUEST_CANCELLED, message, requestId, "NORMAL");
    }

    // ========== NOTIFICATION MANAGEMENT METHODS ==========

    /**
     * Get all notifications for a user
     */
    public List<Notification> getMyNotifications(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }

    /**
     * Get unread notifications
     */
    public List<Notification> getUnreadNotifications(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(user);
    }

    /**
     * Get unread count
     */
    public Long getUnreadCount(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return notificationRepository.countByUserAndIsReadFalse(user);
    }

    /**
     * Mark notification as read
     */
    @Transactional
    public void markAsRead(Long notificationId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));

        if (!notification.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Not authorized");
        }

        notification.setIsRead(true);
        notification.setReadAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }

    /**
     * Mark all as read
     */
    @Transactional
    public void markAllAsRead(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<Notification> unreadNotifications =
                notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(user);

        LocalDateTime now = LocalDateTime.now();
        unreadNotifications.forEach(notification -> {
            notification.setIsRead(true);
            notification.setReadAt(now);
        });

        notificationRepository.saveAll(unreadNotifications);
    }

    /**
     * Delete notification
     */
    @Transactional
    public void deleteNotification(Long notificationId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));

        if (!notification.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Not authorized");
        }

        notificationRepository.delete(notification);
    }
}