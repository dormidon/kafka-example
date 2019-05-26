package ru.mail.polis.channel.service.log.event;

import java.util.Map;

import org.apache.kafka.common.serialization.Serializer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Writes {@link ReadEvent} to Kafka topic.
 */
public class ReadEventSerializer
        implements Serializer<ReadEvent> {

    private static final Logger log =
            LoggerFactory.getLogger(ReadEventSerializer.class);

    @Override
    public void configure(final Map<String, ?> configs,
                          final boolean isKey) {
        // nothing to do
    }

    @Override
    public byte[] serialize(@NotNull final String topic,
                            @NotNull final ReadEvent event) {
        final ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(event).getBytes();
        } catch (Exception e) {
            log.error("Error while serialize ReadEvent", e);
            return null;
        }
    }

    @Override
    public void close() {
        // nothing to do
    }
}

