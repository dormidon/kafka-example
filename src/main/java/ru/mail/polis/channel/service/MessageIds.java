package ru.mail.polis.channel.service;

/**
 * Generates "unique" ids for messages ;)
 */
public class MessageIds {
    /**
     * Not them most successful implementation,
     * even for non-distributed environment.
     * Millisecond precision is very poor.
     * <p>
     * In production you must use something like
     * https://en.wikipedia.org/wiki/Universally_unique_identifier
     * or something from
     * https://www.callicoder.com/distributed-unique-id-sequence-number-generator/
     */
    public static long next() {
        return System.currentTimeMillis();
    }
}
