package ru.mail.polis.channel.search;

import java.io.IOException;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import ru.mail.polis.channel.Message;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SearchServiceTest {

    private final SearchService searchService =
            new SearchService(
                    "test_index",
                    "localhost", 9200);

    @Test
    void singleDocument() throws IOException {
        final Message message =
                new Message(1L, LocalDateTime.now(), 2L, "brown fox");
        searchService.index(message);

        final SearchResult found = searchService.search(
                "fox",
                new Paging(0, 10));
        assertEquals(1, found.getTotalCount());
        assertTrue(found.getIds().contains(message.getId()));

        final SearchResult notFound = searchService.search(
                "bear",
                new Paging(0, 10));
        assertEquals(0, notFound.getTotalCount());
        assertTrue(notFound.getIds().isEmpty());
    }
}
