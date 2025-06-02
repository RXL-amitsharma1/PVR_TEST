package com.rxlogix.listeners

import com.hazelcast.topic.Message
import com.hazelcast.topic.MessageListener
import com.rxlogix.ReportFieldService
import com.rxlogix.localization.Localization
import groovy.json.JsonSlurper
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

@CompileStatic
@Slf4j
class CacheListener implements MessageListener<String> {

    private ReportFieldService reportFieldService

    public CacheListener(ReportFieldService reportFieldService) {
        this.reportFieldService = reportFieldService
    }

    @Override
    public void onMessage(Message<String> message) {
        log.info("Updating Cache Map on all the Hazelcast Nodes")
        Localization.resetAll()
        reportFieldService.clearAllCaches()
        Map fieldVariableMap = new JsonSlurper().parseText(message.getMessageObject()) as Map
        log.debug("Received Map as -> ${fieldVariableMap}")
        reportFieldService.addFileDatatoCache(fieldVariableMap)
        log.info("Updating Cache Map Completed!")
    }
}