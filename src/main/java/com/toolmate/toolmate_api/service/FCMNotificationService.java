package com.toolmate.toolmate_api.service;

import com.google.firebase.messaging.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class FCMNotificationService {

    /**
     * Send push notification to a specific device
     *
     * @param deviceToken FCM token from Android app
     * @param title Notification title
     * @param body Notification body
     * @param data Additional data (optional)
     */
    public void sendPushNotification(
            String deviceToken,
            String title,
            String body,
            Map<String, String> data
    ) {
        try {
            // Build notification
            Notification notification = Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build();

            // Build Android-specific configuration
            AndroidConfig androidConfig = AndroidConfig.builder()
                    .setPriority(AndroidConfig.Priority.HIGH)
                    .setNotification(AndroidNotification.builder()
                            .setSound("default")
                            .setColor("#6200EE") // Your app's primary color
                            .build())
                    .build();

            // Build message
            Message.Builder messageBuilder = Message.builder()
                    .setToken(deviceToken)
                    .setNotification(notification)
                    .setAndroidConfig(androidConfig);

            // Add custom data if provided
            if (data != null && !data.isEmpty()) {
                messageBuilder.putAllData(data);
            }

            Message message = messageBuilder.build();

            // Send message
            String response = FirebaseMessaging.getInstance().send(message);
            log.info("Successfully sent push notification: {}", response);

        } catch (FirebaseMessagingException e) {
            log.error("Failed to send push notification", e);
        }
    }

    /**
     * Send notification with click action
     */
    public void sendNotificationWithAction(
            String deviceToken,
            String title,
            String body,
            String clickAction,
            Long relatedId
    ) {
        Map<String, String> data = new HashMap<>();
        data.put("click_action", clickAction);
        data.put("related_id", String.valueOf(relatedId));

        sendPushNotification(deviceToken, title, body, data);
    }
}