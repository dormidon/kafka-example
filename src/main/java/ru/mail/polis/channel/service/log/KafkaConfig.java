package ru.mail.polis.channel.service.log;

import org.jetbrains.annotations.NotNull;

/**
 * How to interact with Kafka.
 */
public class KafkaConfig {
    private final String bootstrapServers;
    private final String messagesTopic;
    private final String readTopic;

    public KafkaConfig(@NotNull final String bootstrapServers,
                       @NotNull final String messagesTopic,
                       @NotNull final String readTopic) {
        this.bootstrapServers = bootstrapServers;
        this.messagesTopic = messagesTopic;
        this.readTopic = readTopic;
    }

    @NotNull
    public String getBootstrapServers() {
        return bootstrapServers;
    }

    @NotNull
    public String getMessagesTopic() {
        return messagesTopic;
    }

    @NotNull
    public String getReadTopic() {
        return readTopic;
    }
}
