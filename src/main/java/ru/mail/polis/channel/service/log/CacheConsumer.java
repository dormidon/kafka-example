package ru.mail.polis.channel.service.log;

import java.util.function.BooleanSupplier;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.mail.polis.channel.Message;
import ru.mail.polis.channel.cache.ReadCache;

/**
 * Consumes {@link Message}s and moves waterline of last message.
 * This consumer easily may be a separate application supported by
 * other developer team.
 */
public class CacheConsumer
        implements Runnable {

    private static final Logger log =
            LoggerFactory.getLogger(CacheConsumer.class);

    private final Consumer<Long, Message> consumer;
    private final ReadCache readCache;
    private final BooleanSupplier stop;

    public CacheConsumer(@NotNull final Consumer<Long, Message> consumer,
                         @NotNull final ReadCache readCache,
                         @NotNull final BooleanSupplier stop) {
        this.consumer = consumer;
        this.readCache = readCache;
        this.stop = stop;
    }

    @Override
    public void run() {
        log.info("Start consuming messages for ReadCache");

        while (!stop.getAsBoolean()) {
            final ConsumerRecords<Long, Message> poll =
                    consumer.poll(1000);

            if (poll.count() > 0) {
                boolean cached;
                do {
                    cached = cache(poll);
                    if (cached) {
                        log.info("Consumed {} records for cache", poll.count());
                        consumer.commitSync();
                    } else {
                        log.warn("Go to next attempt");
                        Pause.pause();
                    }
                } while (!cached);
            }
        }

        log.info("Stop consuming messages for ReadCache");

        consumer.close();
    }

    private boolean cache(final ConsumerRecords<Long, Message> records) {
        try {
            long last = 0;
            for (final ConsumerRecord<Long, Message> record : records) {
                if (record.value().getId() > last) {
                    last = record.value().getId();
                }
            }
            if (last > 0) {
                readCache.setLast(last);
            }
            return true;
        } catch (Exception e) {
            log.error("Error while consuming messages for cache", e);
            return false;
        }
    }
}
