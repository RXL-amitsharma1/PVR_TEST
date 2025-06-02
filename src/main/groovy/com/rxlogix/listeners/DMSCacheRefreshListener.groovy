package com.rxlogix.listeners

import com.hazelcast.topic.Message
import com.hazelcast.topic.MessageListener
import com.rxlogix.ApplicationSettingsService
import com.rxlogix.config.ApplicationSettings
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

@CompileStatic
@Slf4j
class DMSCacheRefreshListener implements MessageListener<String> {

    private ApplicationSettingsService applicationSettingsService

    DMSCacheRefreshListener(ApplicationSettingsService applicationSettingsService) {
        this.applicationSettingsService = applicationSettingsService
    }

    @Override
    void onMessage(Message<String> message) {
        ApplicationSettings.withNewSession {
            log.debug("Refresh DMS Cache called.")
            applicationSettingsService.dmsCacheRefresh()
        }
    }
}