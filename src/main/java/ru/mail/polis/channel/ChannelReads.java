package ru.mail.polis.channel;

import java.util.List;

/**
 * "Read" part of the Channel Service.
 */
public interface ChannelReads {
    /**
     * Retrieves messages starting from some ID.
     */
    List<Message> listMessages(final long sinceMessageId,
                               final int count);

    /**
     * Searches messages containing given text.
     */
    List<Message> searchMessages(final String text);

    /**
     * Checks whether user has unread messages.
     */
    boolean hasUnread(final long userId);
}
