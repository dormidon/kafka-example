package ru.mail.polis.channel;

import org.jetbrains.annotations.NotNull;

/**
 * "Write" part of the Channel Service.
 */
public interface ChannelWrites {
    /**
     * New message publication.
     */
    Message submit(final long userId,
                   @NotNull final String text);

    /**
     * Moving "waterline" of read messages for user.
     */
    void markReadUntil(final long userId,
                       final long messageId);
}
