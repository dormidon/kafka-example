package ru.mail.polis.channel.service.log;

import java.util.function.BooleanSupplier;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.mail.polis.channel.Message;
import ru.mail.polis.channel.search.SearchService;

/**
 * Consumes {@link Message}s and index them.
 * This consumer easily may be a separate application supported by
 * other developer team.
 */
class IndexConsumer implements Runnable {

    private static final Logger log =
            LoggerFactory.getLogger(IndexConsumer.class);

    private final Consumer<Long, Message> consumer;
    private final SearchService searchService;
    private final BooleanSupplier stop;

    IndexConsumer(@NotNull final Consumer<Long, Message> consumer,
                  @NotNull final SearchService searchService,
                  @NotNull final BooleanSupplier stop) {
        this.consumer = consumer;
        this.searchService = searchService;
        this.stop = stop;
    }

    @Override
    public void run() {
        log.info("Start consuming messages for SearchService");

        while (!stop.getAsBoolean()) {
            final ConsumerRecords<Long, Message> poll =
                    consumer.poll(1000);

            if (poll.count() > 0) {
                boolean indexed;
                do {
                    indexed = index(poll);
                    if (indexed) {
                        log.info("Consumed {} records for index", poll.count());
                        consumer.commitSync();
                    } else {
                        log.warn("Go to next attempt");
                        Pause.pause();
                    }
                } while (!indexed);
            }
        }

        log.info("Stop consuming messages for ReadCache");

        consumer.close();
    }

    private boolean index(final ConsumerRecords<Long, Message> records) {
        try {
            for (final ConsumerRecord<Long, Message> record : records) {
                searchService.index(record.value());
            }
            return true;
        } catch (Exception e) {
            log.error("Error while consuming messages for index", e);
            return false;
        }
    }
}
