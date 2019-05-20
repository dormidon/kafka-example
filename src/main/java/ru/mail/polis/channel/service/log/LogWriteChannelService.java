package ru.mail.polis.channel.service.log;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.jetbrains.annotations.NotNull;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import ru.mail.polis.channel.ChannelService;
import ru.mail.polis.channel.Message;
import ru.mail.polis.channel.cache.ReadCache;
import ru.mail.polis.channel.search.SearchService;
import ru.mail.polis.channel.service.MessageIds;
import ru.mail.polis.channel.service.ReadOnlyChannelService;
import ru.mail.polis.channel.service.log.event.MessageDeserializer;
import ru.mail.polis.channel.service.log.event.MessageSerializer;
import ru.mail.polis.channel.service.log.event.ReadEvent;
import ru.mail.polis.channel.service.log.event.ReadEventDeserializer;
import ru.mail.polis.channel.service.log.event.ReadEventSerializer;
import ru.mail.polis.channel.storage.StorageService;

/**
 * Already good "write" part of Channel Service implementation.
 * It writes only once to "log" and different consumers move
 * data from the "log" to backed systems.
 * <p>
 * It's free from races between writes, and has only
 * eventual consistency issues.
 */
public class LogWriteChannelService
        extends ReadOnlyChannelService
        implements ChannelService {

    private final ExecutorService pool;
    private final Producer<Long, Message> messageProducer;
    private final Producer<Long, ReadEvent> readEventProducer;
    private final AtomicBoolean stop;
    private final KafkaConfig kafkaConfig;

    public LogWriteChannelService(
            @NotNull final StorageService storageService,
            @NotNull final SearchService searchService,
            @NotNull final ReadCache readCache,
            @NotNull final KafkaConfig kafkaConfig) {
        super(storageService, searchService, readCache);

        this.stop = new AtomicBoolean();
        this.kafkaConfig = kafkaConfig;

        this.pool = new ThreadPoolExecutor(4, 4,
                1, TimeUnit.MINUTES, new ArrayBlockingQueue<>(100),
                new ThreadFactoryBuilder()
                        .setNameFormat("log-writes-%d")
                        .build());

        this.messageProducer = KafkaComponents.createProducer(
                kafkaConfig.getBootstrapServers(),
                "message-producer",
                new MessageSerializer());

        this.readEventProducer = KafkaComponents.createProducer(
                kafkaConfig.getBootstrapServers(),
                "read-event-producer",
                new ReadEventSerializer());

        pool.submit(
                new StorageConsumer(
                        KafkaComponents.createConsumer(
                                kafkaConfig.getBootstrapServers(),
                                "storage-consumer",
                                kafkaConfig.getMessagesTopic(),
                                new MessageDeserializer()),
                        storageService,
                        stop::get));

        pool.submit(
                new IndexConsumer(
                        KafkaComponents.createConsumer(
                                kafkaConfig.getBootstrapServers(),
                                "search-consumer",
                                kafkaConfig.getMessagesTopic(),
                                new MessageDeserializer()),
                        searchService,
                        stop::get));

        pool.submit(
                new CacheConsumer(
                        KafkaComponents.createConsumer(
                                kafkaConfig.getBootstrapServers(),
                                "cache-message-consumer",
                                kafkaConfig.getMessagesTopic(),
                                new MessageDeserializer()),
                        readCache,
                        stop::get));

        pool.submit(
                new ReadEventConsumer(
                        KafkaComponents.createConsumer(
                                kafkaConfig.getBootstrapServers(),
                                "cache-reads-consumer",
                                kafkaConfig.getReadTopic(),
                                new ReadEventDeserializer()),
                        readCache,
                        stop::get));
    }

    @Override
    public Message submit(@NotNull final String user,
                          @NotNull final String text) {
        final Message message = new Message(
                MessageIds.next(),
                LocalDateTime.now(),
                user,
                text);

        messageProducer.send(
                new ProducerRecord<>(
                        kafkaConfig.getMessagesTopic(),
                        getUserPartition(kafkaConfig.getMessagesTopic(), user),
                        message.getId(),
                        message));

        return message;
    }

    @Override
    public void markReadUntil(@NotNull final String user,
                              final long messageId) {
        final ReadEvent event = new ReadEvent();
        event.user = user;
        event.messageId = messageId;
        final long key = Hashing
                .goodFastHash(32)
                .newHasher()
                .putString(user, Charsets.UTF_8)
                .putLong(messageId).hash()
                .padToLong();
        readEventProducer.send(
                new ProducerRecord<>(
                        kafkaConfig.getReadTopic(),
                        getUserPartition(kafkaConfig.getReadTopic(), user),
                        key,
                        event));
    }

    // We need to route events from the same user to the same partition,
    // since we have ordering guaranties only within single partition.
    private int getUserPartition(@NotNull final String topic,
                                 @NotNull final String user) {
        return (user.hashCode() % messageProducer.partitionsFor(topic).size());
    }

    @Override
    public void close() throws IOException {
        // Stop producing events to Kafka
        messageProducer.close();
        readEventProducer.close();

        // Stop consuming events from Kafka
        stop.set(true);

        pool.shutdown();
        try {
            pool.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Close services
        super.close();
    }
}
