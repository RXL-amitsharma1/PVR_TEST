package websocket

import grails.plugin.springwebsocket.GrailsSimpAnnotationMethodMessageHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry

@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig extends AbstractWebSocketMessageBrokerConfigurer {

	@Override
	void configureMessageBroker(MessageBrokerRegistry messageBrokerRegistry) {
		messageBrokerRegistry.enableSimpleBroker "/queue", "/topic", "/notifications"
		messageBrokerRegistry.setApplicationDestinationPrefixes "/app"
	}

	@Override
	void registerStompEndpoints(StompEndpointRegistry stompEndpointRegistry) {
		stompEndpointRegistry.addEndpoint("/stomp")
	}

	@Bean
	GrailsSimpAnnotationMethodMessageHandler grailsSimpAnnotationMethodMessageHandler(
		MessageChannel clientInboundChannel,
		MessageChannel clientOutboundChannel,
		SimpMessagingTemplate brokerMessagingTemplate
	) {
		def handler = new GrailsSimpAnnotationMethodMessageHandler(clientInboundChannel, clientOutboundChannel, brokerMessagingTemplate)
		handler.destinationPrefixes = ["/app"]
		return handler
	}
	
}
