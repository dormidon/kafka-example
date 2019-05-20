package ru.mail.polis.channel.service.log;

import java.util.function.BooleanSupplier;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.mail.polis.channel.cache.ReadCache;
import ru.mail.polis.channel.service.log.event.ReadEvent;

/**
 * Consumes {@link ReadEvent}s and moves waterline
 * of last read message for particular user.
 * <p>
 * This consumer easily may be a separate application supported by
 * other developer team.
 */
public class ReadEventConsumer implements Runnable {

    private static final Logger log =
            LoggerFactory.getLogger(ReadEventConsumer.class);

    private final Consumer<Long, ReadEvent> consumer;
    private final ReadCache readCache;
    private final BooleanSupplier stop;

    ReadEventConsumer(@NotNull final Consumer<Long, ReadEvent> consumer,
                      @NotNull final ReadCache readCache,
                      @NotNull final BooleanSupplier stop) {
        this.consumer = consumer;
        this.readCache = readCache;
        this.stop = stop;
    }

    @Override
    public void run() {
        log.info("Start consuming ReadEvents for ReadCache");

        while (!stop.getAsBoolean()) {
            final ConsumerRecords<Long, ReadEvent> poll =
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

        log.info("Stop consuming ReadEvent for ReadCache");

        consumer.close();
    }

    private boolean cache(final ConsumerRecords<Long, ReadEvent> records) {
        try {
            for (final ConsumerRecord<Long, ReadEvent> record : records) {
                readCache.setLastRead(
                        record.value().user,
                        record.value().messageId);
            }
            return true;
        } catch (Exception e) {
            log.error("Error while consuming ReadEvent for cache", e);
            return false;
        }
    }
}
