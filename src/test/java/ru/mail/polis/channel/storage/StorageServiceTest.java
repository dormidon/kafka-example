package ru.mail.polis.channel.storage;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import ru.mail.polis.channel.Message;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StorageServiceTest {

    private final StorageService storage =
            new StorageService("localhost", 5432,
                    "channel", "password", "channel");

    @Test
    void roundTrip() {
        final Message message =
                new Message(6, LocalDateTime.now(), "Alice", "Foo");
        storage.write(message);
        assertEquals(
                Collections.singletonList(message),
                storage.read(6, 10));
    }

    @Test
    void getByIds() {
        final List<Message> messages = new ArrayList<>(10);
        
        for (int i = 0; i < 10; i++) {
            final Message message =
                    new Message(i + 300, LocalDateTime.now(), "Bob", "Text " + i);
            messages.add(message);
            storage.write(message);
        }
        
        final List<Message> initialOddMessages = messages.stream()
                .filter(x -> x.getId() % 2 == 0)
                .collect(Collectors.toList());
        
        assertEquals(initialOddMessages,
                storage.get(
                        initialOddMessages.stream()
                                .map(Message::getId)
                                .collect(Collectors.toList())));

    }
}
