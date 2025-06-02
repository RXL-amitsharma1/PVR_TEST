package com.rxlogix.listeners

import com.hazelcast.topic.Message
import com.hazelcast.topic.MessageListener
import grails.util.Holders
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.grails.plugins.console.ConsoleService
import org.grails.plugins.console.Evaluation

@CompileStatic
@Slf4j
class ConsoleListener implements MessageListener<String> {

    private ConsoleService consoleService

    ConsoleListener(ConsoleService consoleService) {
        this.consoleService = consoleService
    }

    @Override
    void onMessage(Message<String> message) {
        log.info "Channel  : ${Holders.config.getProperty('hazelcast.notification.console')} , Message Received:  ${message.getMessageObject()}"
        Evaluation eval = consoleService.eval(message.getMessageObject(), false, [session: null])
        log.info("Executed code result: ${eval.result}, Exception: ${eval.exception}")
    }
}
