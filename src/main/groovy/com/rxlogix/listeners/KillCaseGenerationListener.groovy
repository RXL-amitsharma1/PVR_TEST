package com.rxlogix.listeners

import com.hazelcast.topic.Message
import com.hazelcast.topic.MessageListener
import com.rxlogix.ApplicationSettingsService
import com.rxlogix.ReportExecutorService
import com.rxlogix.config.ApplicationSettings
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

@CompileStatic
@Slf4j
class KillCaseGenerationListener implements MessageListener<String> {

    private ReportExecutorService reportExecutorService

    KillCaseGenerationListener(ReportExecutorService reportExecutorService) {
        this.reportExecutorService = reportExecutorService
    }

    @Override
    void onMessage(Message<String> message) {
        reportExecutorService.killCaseGenerationExecution(message.getMessageObject().toBigDecimal())
    }
}