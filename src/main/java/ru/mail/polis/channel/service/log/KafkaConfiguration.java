package ru.mail.polis.channel.service.log;

import org.jetbrains.annotations.NotNull;

/**
 * How to interact with Kafka.
 */
public class KafkaConfiguration {
    private final String bootstrapServers;
    private final String messagesTopic;
    private final String readTopic;

    public KafkaConfiguration(@NotNull final String bootstrapServers,
                              @NotNull final String messagesTopic,
                              @NotNull final String readTopic) {
        this.bootstrapServers = bootstrapServers;
        this.messagesTopic = messagesTopic;
        this.readTopic = readTopic;
    }

    public String getBootstrapServers() {
        return bootstrapServers;
    }

    public String getMessagesTopic() {
        return messagesTopic;
    }

    public String getReadTopic() {
        return readTopic;
    }
}
