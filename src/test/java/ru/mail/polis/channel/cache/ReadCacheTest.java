package ru.mail.polis.channel.cache;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReadCacheTest {

    private final ReadCache cache =
            new ReadCache("localhost", 6379);

    @Test
    void setLast() {
        cache.setLast(1000L);
        assertEquals(Optional.of(1000L), cache.getLast());
    }

    @Test
    void setLastRead() {
        cache.setLastRead("Alice", 999L);
        assertEquals(Optional.of(999L), cache.getLastRead("Alice"));
    }
}
