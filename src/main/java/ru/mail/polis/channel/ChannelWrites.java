package ru.mail.polis.channel;

/**
 * "Write" part of the Channel Service.
 */
public interface ChannelWrites {
    /**
     * New message publication.
     */
    Message submit(final long userId,
                   final String text);

    /**
     * Moving "waterline" of read messages for user.
     */
    void markReadUntil(final long userId,
                       final long messageId);
}
