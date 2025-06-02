package com.rxlogix.listeners

import com.hazelcast.topic.Message
import com.hazelcast.topic.MessageListener
import com.rxlogix.user.User
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j

@Slf4j
class NotificationListener implements MessageListener<String> {

    private def brokerMessagingTemplate

    NotificationListener(def brokerMessagingTemplate) {
        this.brokerMessagingTemplate = brokerMessagingTemplate
    }

    @Override
    void onMessage(Message<String> message) {
        Map notificationMap = new JsonSlurper().parseText(message.getMessageObject()) as Map
        log.info("Received notification with ID:" + notificationMap.id + " for UserId:" + notificationMap.userId + "from Hazelcast")
        if (notificationMap.userId) {
            User.withNewSession {
                Long userId = notificationMap.userId
                brokerMessagingTemplate.convertAndSend(User.read(userId).notificationChannel, JsonOutput.toJson(notificationMap))
            }
        }
    }
}