package ru.mail.polis.channel.api;

import java.util.List;

import org.jetbrains.annotations.NotNull;
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

    private static final String H_USER = "x-user";

    private final ChannelService channelService;

    @Autowired
    public ChannelController(@NotNull final ChannelService channelService) {
        this.channelService = channelService;
    }

    @RequestMapping(value = "/messages", method = RequestMethod.POST, headers = H_USER)
    public Message submit(
            @RequestBody @NotNull final String text,
            @RequestHeader(value = H_USER) @NotNull final String user) {
        return channelService.submit(user, text);
    }

    @RequestMapping(value = "/messages", method = RequestMethod.GET)
    public List<Message> messages(@RequestParam final long since,
                                  @RequestParam(defaultValue = "10") final int count) {
        return channelService.listMessages(since, count);
    }

    @RequestMapping(value = "/messages/search", method = RequestMethod.GET)
    public List<Message> search(@RequestParam final String query) {
        return channelService.searchMessages(query);
    }

    @RequestMapping(value = "/messages/unread", method = RequestMethod.GET)
    public boolean hasUnread(@RequestHeader(H_USER) @NotNull final String user) {
        return channelService.hasUnread(user);
    }

    @RequestMapping(value = "/messages/unread", method = RequestMethod.DELETE)
    public void markRead(@RequestHeader(H_USER) @NotNull final String user,
                         @RequestParam final long to) {
        channelService.markReadUntil(user, to);
    }
}
