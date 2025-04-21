package io.autoinvestor;

import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.NoCredentialsProvider;
import com.google.api.gax.grpc.GrpcTransportChannel;
import com.google.api.gax.rpc.FixedTransportChannelProvider;
import com.google.cloud.pubsub.v1.SubscriptionAdminClient;
import com.google.cloud.pubsub.v1.SubscriptionAdminSettings;
import com.google.cloud.pubsub.v1.TopicAdminClient;
import com.google.cloud.pubsub.v1.TopicAdminSettings;
import com.google.cloud.spring.pubsub.PubSubAdmin;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PubSubEmulatorContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@SpringBootTest
@Testcontainers
public class UserEventPubSubInboundAdapterTest {

    public static final String PROJECT_ID = "test-project";

    private static final Map<String, String> TOPOLOGY = Map.of(
            "users", "alerts-to-users"
    );

    @Container
    public static final PubSubEmulatorContainer PUB_SUB_EMULATOR_CONTAINER =
            new PubSubEmulatorContainer(DockerImageName.parse("gcr.io/google.com/cloudsdktool/cloud-sdk:317.0.0-emulators"));

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.cloud.gcp.pubsub.project-id", () -> PROJECT_ID);
        registry.add("spring.cloud.gcp.pubsub.emulator-host", PUB_SUB_EMULATOR_CONTAINER::getEmulatorEndpoint);
    }

    @TestConfiguration
    static class PubSubEmulatorConfiguration {
        @Bean
        CredentialsProvider googleCredentials() {
            return NoCredentialsProvider.create();
        }
    }

    @BeforeAll
    static void setup() throws Exception {
        ManagedChannel channel = ManagedChannelBuilder
                .forTarget("dns:///" + PUB_SUB_EMULATOR_CONTAINER.getEmulatorEndpoint())
                .usePlaintext()
                .build();

        var channelProvider = FixedTransportChannelProvider.create(GrpcTransportChannel.create(channel));

        TopicAdminClient topicAdminClient = TopicAdminClient.create(
                TopicAdminSettings.newBuilder()
                        .setCredentialsProvider(NoCredentialsProvider.create())
                        .setTransportChannelProvider(channelProvider)
                        .build());

        SubscriptionAdminClient subscriptionAdminClient = SubscriptionAdminClient.create(
                SubscriptionAdminSettings.newBuilder()
                        .setTransportChannelProvider(channelProvider)
                        .setCredentialsProvider(NoCredentialsProvider.create())
                        .build());

        try (var admin = new PubSubAdmin(() -> PROJECT_ID, topicAdminClient, subscriptionAdminClient)) {
            TOPOLOGY.keySet().forEach(admin::createTopic);
            TOPOLOGY.forEach((topic, subscription) -> admin.createSubscription(subscription, topic));
        } finally {
            channel.shutdown();
        }
    }

    @Test
    void isRunningTest() {
        assertTrue(PUB_SUB_EMULATOR_CONTAINER.isRunning());
    }
}

