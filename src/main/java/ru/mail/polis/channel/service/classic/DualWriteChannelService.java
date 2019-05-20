package ru.mail.polis.channel.service.classic;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import ru.mail.polis.channel.ChannelService;
import ru.mail.polis.channel.Message;
import ru.mail.polis.channel.cache.ReadCache;
import ru.mail.polis.channel.search.SearchService;
import ru.mail.polis.channel.service.MessageIds;
import ru.mail.polis.channel.service.ReadOnlyChannelService;
import ru.mail.polis.channel.storage.StorageService;

/**
 * Not the most successful "write" part Channel Service implementation.
 * It should know about all backed systems and writes concurrently to them:
 * to the main storage, to the full-test search engine, and to the cache.
 * <p>
 * The implementation is prone to races and perpetual inconsistency
 * among backed storage systems.
 */
public class DualWriteChannelService
        extends ReadOnlyChannelService
        implements ChannelService {

    private static final Logger log =
            LoggerFactory.getLogger(DualWriteChannelService.class);

    private final ExecutorService pool;

    public DualWriteChannelService(
            @NotNull final StorageService storageService,
            @NotNull final SearchService searchService,
            @NotNull final ReadCache readCache) {
        super(storageService, searchService, readCache);

        this.pool = new ThreadPoolExecutor(1, 3,
                1, TimeUnit.MINUTES, new ArrayBlockingQueue<>(100),
                new ThreadFactoryBuilder()
                        .setNameFormat("dual-writes-%d")
                        .build());
    }

    @Override
    public Message submit(final long userId, @NotNull final String text) {
        final Message message = new Message(
                MessageIds.next(),
                LocalDateTime.now(),
                userId,
                text);
        pool.submit(() -> {
            try {
                storageService.write(message);
            } catch (Exception e) {
                log.error("Error while submit update to ReadCache", e);
            }
        });
        pool.submit(() -> {
            try {
                readCache.setLast(message.getId());
            } catch (Exception e) {
                log.error("Error while submit update to ReadCache", e);
            }
        });
        pool.submit(() -> {
            try {
                searchService.index(message);
            } catch (IOException e) {
                log.error("Error while submit update to SearchService", e);
            }
        });
        return message;
    }

    @Override
    public void markReadUntil(final long userId,
                              final long messageId) {
        readCache.setLastRead(userId, messageId);
    }

    @Override
    public void close() throws IOException {
        pool.shutdown();
        try {
            pool.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        super.close();
    }

}
