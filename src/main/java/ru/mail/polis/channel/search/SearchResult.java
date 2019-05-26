package ru.mail.polis.channel.search;

import java.util.List;

import org.jetbrains.annotations.NotNull;

/**
 * Full-text search result.
 * Contains only found message identities,
 * so we need to get entire messages from somewhere else
 * (e.g. "primary" message storage).
 */
public class SearchResult {
    private final List<Long> ids;
    private final long totalCount;

    SearchResult(@NotNull final List<Long> ids,
                 long totalCount) {
        this.ids = ids;
        this.totalCount = totalCount;
    }

    @NotNull
    public List<Long> getIds() {
        return ids;
    }

    public long getTotalCount() {
        return totalCount;
    }
}
