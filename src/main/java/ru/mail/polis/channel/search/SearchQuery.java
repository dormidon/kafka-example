package ru.mail.polis.channel.search;

import org.jetbrains.annotations.Nullable;

/**
 * Query to full-text search system.
 */
public class SearchQuery {
    private final String text;
    private final Long authorId;

    public SearchQuery(@Nullable final String text,
                       @Nullable final Long authorId) {
        this.text = text;
        this.authorId = authorId;
    }

    public SearchQuery(String text) {
        this(text, null);
    }

    public String getText() {
        return text;
    }

    public Long getAuthorId() {
        return authorId;
    }
}
