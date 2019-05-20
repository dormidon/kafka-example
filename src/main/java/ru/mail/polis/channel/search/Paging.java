package ru.mail.polis.channel.search;

/**
 * Slice of data to be retrieved from search system.
 */
public class Paging {
    private final int offset;
    private final int count;

    public Paging(final int offset,
                  final int count) {
        this.offset = offset;
        this.count = count;
    }

    public int getOffset() {
        return offset;
    }

    public int getCount() {
        return count;
    }
}
