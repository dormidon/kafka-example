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
import ru.mail.polis.channel.storage.StorageService;

/**
 * Consumes {@link Message}s and saves them to main storage.
 * This consumer easily may be a separate application supported by
 * other developer team.
 */
class StorageConsumer implements Runnable {

    private static final Logger log =
            LoggerFactory.getLogger(StorageConsumer.class);

    private final Consumer<Long, Message> consumer;
    private final StorageService storageService;
    private final BooleanSupplier stop;

    StorageConsumer(@NotNull final Consumer<Long, Message> consumer,
                    @NotNull final StorageService storageService,
                    @NotNull final BooleanSupplier stop) {
        this.consumer = consumer;
        this.storageService = storageService;
        this.stop = stop;
    }

    @Override
    public void run() {
        log.info("Start consuming messages for StorageService");

        while (!stop.getAsBoolean()) {
            final ConsumerRecords<Long, Message> records =
                    consumer.poll(1000);

            if (records.count() > 0) {
                boolean stored;
                do {
                    stored = store(records);
                    if (stored) {
                        log.info("Consumed {} records for storage", records.count());
                        consumer.commitSync();
                    } else {
                        log.warn("Go to next attempt");
                        Pause.pause();
                    }
                } while (!stored);
            }
        }

        log.info("Stop consuming messages for StorageService");

        consumer.close();
    }

    private boolean store(final ConsumerRecords<Long, Message> records) {
        try {
            for (final ConsumerRecord<Long, Message> record : records) {
                storageService.write(record.value());
            }
            return true;
        } catch (Exception e) {
            log.error("Error while consuming messages for storage", e);
            return false;
        }
    }
}
