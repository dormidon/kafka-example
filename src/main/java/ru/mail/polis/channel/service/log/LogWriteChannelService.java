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

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import ru.mail.polis.channel.ChannelService;
import ru.mail.polis.channel.Message;
import ru.mail.polis.channel.cache.ReadCache;
import ru.mail.polis.channel.search.SearchService;
import ru.mail.polis.channel.service.MessageIds;
import ru.mail.polis.channel.service.ReadOnlyChannelService;
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
    private final AtomicBoolean stop;
    private final KafkaConfiguration kafka;

    public LogWriteChannelService(
            @NotNull final StorageService storageService,
            @NotNull final SearchService searchService,
            @NotNull final ReadCache readCache,
            @NotNull final KafkaConfiguration kafka) {
        super(storageService, searchService, readCache);

        this.stop = new AtomicBoolean();
        this.kafka = kafka;

        this.pool = new ThreadPoolExecutor(3, 3,
                1, TimeUnit.MINUTES, new ArrayBlockingQueue<>(100),
                new ThreadFactoryBuilder()
                        .setNameFormat("log-writes-%d")
                        .build());

        this.messageProducer = KafkaComponents.createProducer(
                kafka.getBootstrapServers(),
                "channel-service");

        pool.submit(
                new StorageConsumer(
                        KafkaComponents.createConsumer(
                                kafka.getBootstrapServers(),
                                "storage-consumer",
                                kafka.getMessagesTopic()),
                        storageService,
                        stop::get));

        pool.submit(
                new IndexConsumer(
                        KafkaComponents.createConsumer(
                                kafka.getBootstrapServers(),
                                "search-consumer",
                                kafka.getMessagesTopic()),
                        searchService,
                        stop::get));

        pool.submit(
                new CacheConsumer(
                        KafkaComponents.createConsumer(
                                kafka.getBootstrapServers(),
                                "cache-consumer",
                                kafka.getMessagesTopic()),
                        readCache,
                        stop::get));

    }

    @Override
    public Message submit(final long userId,
                          final String text) {
        final Message message = new Message(
                MessageIds.next(),
                LocalDateTime.now(),
                userId,
                text);

        messageProducer.send(
                new ProducerRecord<>(
                        kafka.getMessagesTopic(),
                        message.getId(),
                        message));

        return message;
    }

    @Override
    public void markReadUntil(final long userId,
                              final long text) {
        //
    }

    @Override
    public void close() throws IOException {
        // Stop consuming from Kafka
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
