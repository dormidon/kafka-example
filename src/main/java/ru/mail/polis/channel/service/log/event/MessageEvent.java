package ru.mail.polis.channel.service.log.event;

/**
 * Message sent event to be stored in log.
 */
public final class MessageEvent {
    public long id;
    public long timestamp;
    public String user;
    public String text;
}
