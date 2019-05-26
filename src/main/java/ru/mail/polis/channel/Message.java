package ru.mail.polis.channel;

import java.time.LocalDateTime;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;

import com.google.common.base.MoreObjects;

/**
 * Single message in the channel.
 */
public final class Message {
    private final long id;
    private final LocalDateTime createTime;
    private final String user;
    private final String text;
    
    public Message(final long id,
                   @NotNull final LocalDateTime createTime,
                   @NotNull final String user,
                   @NotNull final String text) {
        this.id = id;
        this.createTime = createTime;
        this.user = user;
        this.text = text;
    }

    public long getId() {
        return id;
    }
    
    @NotNull
    public LocalDateTime getCreateTime() {
        return createTime;
    }
    
    @NotNull
    public String getUser() {
        return user;
    }
    
    @NotNull
    public String getText() {
        return text;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Message message = (Message) o;
        return id == message.id &&
                Objects.equals(createTime, message.createTime) &&
                Objects.equals(user, message.user) &&
                Objects.equals(text, message.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, createTime, user, text);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("createTime", createTime)
                .add("user", user)
                .add("text", text)
                .toString();
    }
}
