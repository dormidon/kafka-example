package ru.mail.polis.channel.service.log.event;

import java.sql.Timestamp;
import java.util.Map;

import org.apache.kafka.common.serialization.Serializer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import ru.mail.polis.channel.Message;

/**
 * Writes {@link Message} to Kafka topic.
 */
public class MessageSerializer
        implements Serializer<Message> {

    private static final Logger log =
            LoggerFactory.getLogger(MessageSerializer.class);

    @Override
    public void configure(final Map<String, ?> configs, final boolean isKey) {
        // nothing to do
    }

    @Override
    public byte[] serialize(@NotNull final String topic,
                            @NotNull final Message message) {
        final ObjectMapper mapper = new ObjectMapper();
        try {
            final MessageEvent event = new MessageEvent();
            event.id = message.getId();
            event.timestamp = Timestamp.valueOf(message.getCreateTime()).getTime();
            event.user = message.getUser();
            event.text = message.getText();
            return mapper.writeValueAsString(event).getBytes();
        } catch (Exception e) {
            log.error("Error while serialize Message", e);
            return null;
        }
    }

    @Override
    public void close() {
        // nothing to do
    }
}
