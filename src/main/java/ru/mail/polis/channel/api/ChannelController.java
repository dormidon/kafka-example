package ru.mail.polis.channel.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ru.mail.polis.channel.ChannelService;
import ru.mail.polis.channel.Message;

/**
 * HTTP API for channel.
 */
@RestController
public class ChannelController {

    private final ChannelService channelService;

    @Autowired
    public ChannelController(final ChannelService channelService) {
        this.channelService = channelService;
    }

    @RequestMapping(value = "/messages", method = RequestMethod.POST, headers = "x-uid")
    public Message submit(
            @RequestBody String text,
            @RequestHeader(value = "x-uid") long userId) {
        return channelService.submit(userId, text);
    }

    @RequestMapping(value = "/messages", method = RequestMethod.GET)
    public List<Message> messages(@RequestParam long since,
                                  @RequestParam(defaultValue = "10") int count) {
        return channelService.listMessages(since, count);
    }

    @RequestMapping(value = "/messages/search", method = RequestMethod.GET)
    public List<Message> search(@RequestParam String query) {
        return channelService.searchMessages(query);
    }

    @RequestMapping(value = "/messages/unread", method = RequestMethod.GET)
    public boolean hasUnread(@RequestHeader("x-uid") long userId) {
        return channelService.hasUnread(userId);
    }

    @RequestMapping(value = "/messages/unread", method = RequestMethod.DELETE)
    public void markRead(@RequestHeader("x-uid") long userId,
                         @RequestParam long to) {
        channelService.markReadUntil(userId, to);
    }
}
