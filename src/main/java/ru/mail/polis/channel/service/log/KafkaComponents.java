package ru.mail.polis.channel.service.log;

import java.util.Collections;
import java.util.Properties;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.Serializer;
import org.jetbrains.annotations.NotNull;

/**
 * Helps to create Kafka components.
 */
class KafkaComponents {
    static <T> Producer<Long, T> createProducer(
            @NotNull final String bootstrapServers,
            @NotNull final String clientId,
            @NotNull Serializer<T> valueSerializer) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, clientId);
        props.put(ProducerConfig.ACKS_CONFIG, "1");
        return new KafkaProducer<>(props, new LongSerializer(), valueSerializer);
    }

    static <T> Consumer<Long, T> createConsumer(
            @NotNull final String bootstrapServers,
            @NotNull final String group,
            @NotNull final String topic,
            @NotNull final Deserializer<T> valueDeserializer) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, group);
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 100);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        Consumer<Long, T> consumer =
                new KafkaConsumer<>(props,
                        new LongDeserializer(), valueDeserializer);
        consumer.subscribe(Collections.singleton(topic));
        return consumer;
    }
}
