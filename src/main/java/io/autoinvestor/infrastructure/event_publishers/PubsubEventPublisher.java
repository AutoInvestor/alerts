package io.autoinvestor.infrastructure.event_publishers;

import io.autoinvestor.domain.events.Event;
import io.autoinvestor.domain.events.EventPublisher;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.pubsub.v1.ProjectTopicName;

@Slf4j
@Component
@Profile("prod")
public class PubsubEventPublisher implements EventPublisher {

    private final Publisher publisher;
    private final EventMessageMapper mapper;

    public PubsubEventPublisher(
            @Value("${GCP_PROJECT}") String projectId,
            @Value("${PUBSUB_TOPIC}") String topic,
            ObjectMapper objectMapper)
            throws Exception {
        this.mapper = new EventMessageMapper(objectMapper);
        ProjectTopicName topicName = ProjectTopicName.of(projectId, topic);
        this.publisher = Publisher.newBuilder(topicName).build();

        log.info("Pub/Sub publisher created for topic {}", topicName);
    }

    @Override
    public void publish(List<Event<?>> events) {
        if (events.isEmpty()) {
            log.debug("publish invoked with empty list — nothing to do");
            return;
        }

        log.info("Publishing {} domain event(s)", events.size());

        events.stream()
                .map(mapper::toMessage)
                .forEach(
                        msg -> {
                            publisher
                                    .publish(msg)
                                    .addListener(
                                            () ->
                                                    log.debug(
                                                            "Published msgId={}",
                                                            msg.getMessageId()),
                                            Runnable::run);
                        });
    }

    @PreDestroy
    public void shutdown() throws Exception {
        log.info("Shutting down Pub/Sub publisher...");
        publisher.shutdown();
        publisher.awaitTermination(1, TimeUnit.MINUTES);
    }
}
