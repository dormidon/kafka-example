package ru.mail.polis.channel.service.log.event;

import java.sql.Timestamp;
import java.util.Map;

import org.apache.kafka.common.serialization.Deserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import ru.mail.polis.channel.Message;

/**
 * Reads {@link Message} from Kafka topic.
 */
public class MessageDeserializer
        implements Deserializer<Message> {

    private static final Logger log =
            LoggerFactory.getLogger(MessageDeserializer.class);

    @Override
    public void configure(final Map<String, ?> configs,
                          final boolean isKey) {
        // nothing to do
    }

    @Override
    public Message deserialize(final String topic,
                               final byte[] data) {
        final ObjectMapper mapper = new ObjectMapper();
        try {
            final MessageEvent event = mapper.readValue(data, MessageEvent.class);
            return new Message(
                    event.id,
                    new Timestamp(event.timestamp).toLocalDateTime(),
                    event.user,
                    event.text);
        } catch (Exception e) {
            log.error("Unable to deserialize Message", e);
        }
        return null;
    }

    @Override
    public void close() {
        // nothing to do
    }
}
