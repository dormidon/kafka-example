package ru.mail.polis.channel.service;

/**
 * Generates "unique" ids for messages ;)
 */
public class MessageIds {
    public static long next() {
        return System.currentTimeMillis();
    }
}
