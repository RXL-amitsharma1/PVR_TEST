package com.rxlogix.hazelcast


import com.hazelcast.core.HazelcastInstance
import com.hazelcast.map.IMap
import com.hazelcast.topic.ITopic
import com.hazelcast.topic.MessageListener
import com.rxlogix.FileGenerationInfoDTO
import com.rxlogix.listeners.CacheListener
import com.rxlogix.listeners.ConsoleListener
import com.rxlogix.listeners.DMSCacheRefreshListener
import com.rxlogix.listeners.KillCaseGenerationListener
import com.rxlogix.listeners.NotificationListener
import com.rxlogix.listeners.ReportFieldCacheRefreshListener
import com.rxlogix.util.MiscUtil
import grails.core.GrailsApplication

import javax.annotation.PostConstruct

/*
DEVELOPMENT NOTE: If you let grails dynamically reload this service without application restart while enhancing or fixing something in this service.
It will create new hazelcast server and client each time without removing older one, this will introduce duplicate notifications
*/

class HazelService {

    static transactional = false

    GrailsApplication grailsApplication

    def brokerMessagingTemplate
    def applicationSettingsService
    def reportExecutorService
    def reportFieldService
    def consoleService

    HazelcastInstance hazelcastInstance

    ConfigObject getHazelcastConfig() {
        return grailsApplication.config.hazelcast
    }

    private HazelService() {}

    @PostConstruct
    void init() {
        if (hazelcastConfig.enabled) {
            log.info("Hazelcast is enabled via config, starting initialization of HazelService...")
            addListenerToTopic('channel', new NotificationListener(brokerMessagingTemplate))
            addListenerToTopic('dmsCache', new DMSCacheRefreshListener(applicationSettingsService))
            addListenerToTopic('killCaseGeneration', new KillCaseGenerationListener(reportExecutorService))
            addListenerToTopic('reportFieldCache', new ReportFieldCacheRefreshListener(reportFieldService))
            addListenerToTopic('console', new ConsoleListener(consoleService))
            addListenerToTopic('cacheChannel', new CacheListener(reportFieldService))
        }
    }

    void addListenerToTopic(String configKey, MessageListener listener){
        String topicName = hazelcastConfig.notification[configKey]
        ITopic topic = hazelcastInstance.getTopic(topicName)
        topic.addMessageListener(listener)
    }

    ITopic topic(String topicName) {
        hazelcastInstance.getTopic(topicName)
    }

    void publishToTopic(String topicName, String message) {
        topic(topicName).publish(message)
    }

    def getAndLockHazelCastMap(String mapName, String fileName) {
        if(mapName) {
            IMap map = hazelcastInstance.getMap(mapName)
            log.trace("Lock HazelCast Map ${mapName}")
            map.lock(fileName)
        }
    }

    def createMap(String mapName) {
        if (mapName) {
            log.trace("Creating HazelCast Map ${mapName}")
            hazelcastInstance.getMap(mapName)
        }
    }

    void unlockAndRemove(def fileName, String mapName) {
        log.trace("Unlock HazelCast Map ${mapName}")
        try {
            IMap map = hazelcastInstance.getMap(mapName)
            map.unlock(fileName)
            map.remove(fileName)
        } catch (Exception ex) {
            log.warn(ex)
        }
    }

    Object removeValueFromMap(def fileName, String mapName) {
        if (mapName) {
            IMap map = hazelcastInstance.getMap(mapName)
            log.trace("Removing value from HazelCast Map ${fileName}")
            map.remove(fileName)
        }
    }

    IMap<String, FileGenerationInfoDTO> populateMap(String fileName, String mapName) {
        if (!createMap(mapName).get(fileName)) {
            createMap(mapName).put(fileName, new FileGenerationInfoDTO())
        }
        log.trace("Populating HazelCast Map: ${mapName}")
        return createMap(mapName)
    }

    void forceUnlockMap(String fileName, String mapName) {
        log.trace("Force Unlock HazelCast Map ${mapName}")
        createMap(mapName).forceUnlock(fileName)
    }

    Boolean isEnabled() {
        return hazelcastConfig.enabled
    }

    // Taken from https://stackoverflow.com/questions/27567485/how-can-i-check-if-a-hazelcast-cluster-is-alive-from-a-java-client
    boolean checkHealthGoodForExecution() {
        if (enabled && (!hazelcastInstance.getLifecycleService().isRunning() || !checkTopicsHealth())) {
            return false
        }
        return true
    }

    private boolean checkTopicsHealth() {
        if (!hazelcastConfig.full.health.check) {
            return true
        }
        try {
            hazelcastInstance.getMap("parameters").size();
            hazelcastInstance.getTopic("rxlogix").getLocalTopicStats();
        } catch (Exception e) {
            log.warn(e.message)
            // instance may run but cluster is down:
            return false
        }
        return true
    }


}