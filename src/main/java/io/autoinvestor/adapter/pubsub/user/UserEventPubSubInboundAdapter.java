package io.autoinvestor.adapter.pubsub.user;

import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import com.google.cloud.spring.pubsub.integration.AckMode;
import com.google.cloud.spring.pubsub.integration.inbound.PubSubInboundChannelAdapter;
import com.google.cloud.spring.pubsub.support.BasicAcknowledgeablePubsubMessage;
import com.google.cloud.spring.pubsub.support.GcpPubSubHeaders;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.handler.annotation.Header;

@Slf4j
@Configuration
@RequiredArgsConstructor
class UserEventPubSubInboundAdapter {

    private static final String PUBSUB_CHANNEL = "alerts-to-users";

    private final UserCreatedEventProcessor userCreatedEventProcessor;
    private final UserUpdatedEventProcessor userUpdatedEventProcessor;

    @Bean(PUBSUB_CHANNEL)
    public MessageChannel alertsToUsers() {
        return new PublishSubscribeChannel();
    }

    @Bean("errorChannel")
    public MessageChannel errorChannel() {
        return new DirectChannel();
    }

    @Bean
    public PubSubInboundChannelAdapter inboundChannelAdapter(
            @Qualifier(PUBSUB_CHANNEL) MessageChannel messageChannel,
            @Qualifier("errorChannel") MessageChannel errorChannel,
            PubSubTemplate pubSubTemplate
    ) {
        PubSubInboundChannelAdapter adapter = new PubSubInboundChannelAdapter(pubSubTemplate, PUBSUB_CHANNEL);
        adapter.setOutputChannel(messageChannel);
        adapter.setErrorChannel(errorChannel);
        adapter.setAckMode(AckMode.MANUAL);
        adapter.setPayloadType(UserEvent.class);
        return adapter;
    }

    @ServiceActivator(inputChannel = PUBSUB_CHANNEL)
    public void messageReceiver(
            UserEvent<?> payload,
            @Header(GcpPubSubHeaders.ORIGINAL_MESSAGE) BasicAcknowledgeablePubsubMessage message
    ) {
        log.info("Received message (ID: {}) from {}: {}", message.getPubsubMessage().getMessageId(), PUBSUB_CHANNEL, payload);

        switch (payload.type()) {
            case "USER_CREATED" -> userCreatedEventProcessor.process((UserCreatedEvent) payload);
            case "USER_UPDATED" -> userUpdatedEventProcessor.process((UserUpdatedEvent) payload);
            default -> log.error("Unrecognized event type: {}", payload.type());
        }
        
        message.ack().thenRun(() -> log.info("Message acknowledged (ID: {})", message.getPubsubMessage().getMessageId()));
    }

    @ServiceActivator(inputChannel = "errorChannel")
    public void handleError(
            Message<?> errorMessage,
            @Header(GcpPubSubHeaders.ORIGINAL_MESSAGE) BasicAcknowledgeablePubsubMessage message
    ) {
        var errorMessagePayload = errorMessage.getPayload();
        var originalPayload = message.getPubsubMessage().getData().toStringUtf8();
        log.info("Received errored message (ID: {}). Error message {}. Original payload: {}", message.getPubsubMessage().getMessageId(), errorMessagePayload, originalPayload);

        message.ack().thenRun(() -> log.info("Error message acknowledged (ID: {})", message.getPubsubMessage().getMessageId()));
    }
}
