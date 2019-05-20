package ru.mail.polis.channel.cache;

import java.io.Closeable;
import java.io.IOException;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;

import com.google.common.base.Charsets;
import com.google.common.primitives.Longs;
import redis.clients.jedis.Jedis;

/**
 * "Waterlines" of last message in the channel
 * and last read message by user.
 */
public class ReadCache
        implements Closeable {

    private static final byte[] LAST_MESSAGE_KEY =
            "last".getBytes(Charsets.UTF_8);

    private final Jedis jedis;

    public ReadCache(@NotNull final String host,
                     final int port) {
        this.jedis = new Jedis(host, port);
    }

    public Optional<Long> getLast() {
        return read(LAST_MESSAGE_KEY);
    }

    public void setLast(final long messageId) {
        write(LAST_MESSAGE_KEY, messageId);
    }

    public Optional<Long> getLastRead(final long userId) {
        return read(lastReadKey(userId));
    }

    public void setLastRead(final long userId,
                            final long messageId) {
        write(lastReadKey(userId), messageId);
    }

    private Optional<Long> read(@NotNull final byte[] key) {
        return Optional
                .ofNullable(jedis.get(key))
                .map(Longs::fromByteArray);
    }

    private void write(@NotNull final byte[] key,
                       final long value) {
        jedis.set(key, Longs.toByteArray(value));
    }

    private static byte[] lastReadKey(final long userId) {
        return ("r:" + userId).getBytes(Charsets.UTF_8);
    }

    @Override
    public void close() {
        jedis.close();
    }
}
