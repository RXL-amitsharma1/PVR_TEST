package com.rxlogix

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import grails.gorm.transactions.ReadOnly
import groovy.util.logging.Slf4j
import org.slf4j.LoggerFactory

@ReadOnly
@Slf4j
class DevToolService {

    def grailsApplication
    def hazelService

    //Created method to execute a code on all nodes. This's for debugging and code override at runtime.
    // use example in console window ctx.devToolService.executeCodeOnAllNodes('config.testhelp=true')
    void executeCodeOnAllNodes(String code) {
        ConfigObject hazelcast = grailsApplication.config.hazelcast
        if (hazelcast.enabled) {
            String consoleChannel = hazelcast.notification.console
            hazelService.publishToTopic(consoleChannel, code)
        } else {
            throw new Exception("Hazelcast is not enabled. Please use console directly")
        }

    }

    void changeLoggingLevel(String packageName, String newLogLevel) {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory()
        Logger logger = loggerContext.getLogger(packageName)
        if (logger) {
            Level changeLevel = newLogLevel ? Level.toLevel(newLogLevel) : null
            log.info("Current log level : " + logger.getLevel() + " for package:" + packageName + " and changed to: " + changeLevel)
            logger.setLevel(changeLevel)
        } else {
            log.warn("No logger found for package: ${packageName} so not able to change logging level")
        }
    }
}
