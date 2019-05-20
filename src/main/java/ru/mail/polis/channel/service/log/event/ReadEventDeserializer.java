package ru.mail.polis.channel.service.log.event;

import java.util.Map;

import org.apache.kafka.common.serialization.Deserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Reads {@link ReadEvent} from Kafka topic.
 */
public class ReadEventDeserializer
        implements Deserializer<ReadEvent> {

    private static final Logger log =
            LoggerFactory.getLogger(ReadEventDeserializer.class);

    @Override
    public void configure(final Map<String, ?> configs,
                          final boolean isKey) {
        // nothing to do
    }

    @Override
    public ReadEvent deserialize(final String topic,
                                 final byte[] data) {
        final ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(data, ReadEvent.class);
        } catch (Exception e) {
            log.error("Unable to deserialize ReadEvent", e);
        }
        return null;
    }

    @Override
    public void close() {
        // nothing to do
    }
}
