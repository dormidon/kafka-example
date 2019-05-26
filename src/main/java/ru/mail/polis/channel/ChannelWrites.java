package ru.mail.polis.channel;

import org.jetbrains.annotations.NotNull;

/**
 * "Write" part of the Channel Service.
 */
public interface ChannelWrites {
    /**
     * New message publication.
     */
    Message submit(@NotNull final String user,
                   @NotNull final String text);

    /**
     * Moving "waterline" of read messages for user.
     */
    void markReadUntil(@NotNull final String user,
                       final long messageId);
}
