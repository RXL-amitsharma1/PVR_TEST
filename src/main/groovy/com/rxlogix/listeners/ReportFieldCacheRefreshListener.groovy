package com.rxlogix.listeners

import com.hazelcast.topic.Message
import com.hazelcast.topic.MessageListener
import com.rxlogix.ReportFieldService
import com.rxlogix.config.ReportField
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import com.rxlogix.localization.Localization

@CompileStatic
@Slf4j
class ReportFieldCacheRefreshListener implements MessageListener<String> {

    private ReportFieldService reportFieldService

    ReportFieldCacheRefreshListener(ReportFieldService reportFieldService){
        this.reportFieldService = reportFieldService
    }

    @Override
    void onMessage(Message<String> message) {
        ReportField.withNewSession {
            log.info("Refresh Report Field Cache called")
            reportFieldService.reLoadValuesToCacheFile()
            Localization.resetAll()
            reportFieldService.clearAllCaches()
        }
    }
}
