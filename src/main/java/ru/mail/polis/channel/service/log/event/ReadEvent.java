package ru.mail.polis.channel.service.log.event;

/**
 * User's read message event to be stored in log.
 */
public final class ReadEvent {
    public String user;
    public long messageId;
}
