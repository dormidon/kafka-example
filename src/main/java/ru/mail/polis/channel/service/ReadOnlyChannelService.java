package ru.mail.polis.channel.service;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;

import ru.mail.polis.channel.ChannelReads;
import ru.mail.polis.channel.Message;
import ru.mail.polis.channel.cache.ReadCache;
import ru.mail.polis.channel.search.Paging;
import ru.mail.polis.channel.search.SearchResult;
import ru.mail.polis.channel.search.SearchService;
import ru.mail.polis.channel.storage.StorageService;

/**
 * "Read" part Channel implementation.
 */
public class ReadOnlyChannelService
        implements ChannelReads, Closeable {

    protected final StorageService storageService;
    protected final ReadCache readCache;
    protected final SearchService searchService;

    public ReadOnlyChannelService(
            @NotNull final StorageService storageService,
            @NotNull final SearchService searchService,
            @NotNull final ReadCache readCache) {
        this.storageService = storageService;
        this.searchService = searchService;
        this.readCache = readCache;
    }

    @Override
    public List<Message> listMessages(final long since,
                                      final int count) {
        return storageService.read(since, count);
    }

    @Override
    public List<Message> searchMessages(@NotNull final String text) {
        try {
            final SearchResult found =
                    searchService.search(
                            text,
                            new Paging(0, 10));
            return storageService.get(found.getIds());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean hasUnread(final long userId) {
        final Optional<Long> last = readCache.getLast();
        if (last.isEmpty()) {
            return false;
        }
        final Optional<Long> lastRead = readCache.getLastRead(userId);
        if (lastRead.isEmpty()) {
            return true;
        }
        return lastRead.get() < last.get();
    }

    @Override
    public void close() throws IOException {
        storageService.close();
        searchService.close();
        readCache.close();
    }
}
