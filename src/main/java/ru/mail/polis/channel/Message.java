package ru.mail.polis.channel;

import java.time.LocalDateTime;
import java.util.Objects;

import com.google.common.base.MoreObjects;

/**
 * Single message in the channel.
 */
public final class Message {
    private final long id;
    private final LocalDateTime createTime;
    private final long authorId;
    private final String text;
    
    public Message(final long id,
                   final LocalDateTime createTime,
                   final long authorId,
                   final String text) {
        this.id = id;
        this.createTime = createTime;
        this.authorId = authorId;
        this.text = text;
    }

    public long getId() {
        return id;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public long getAuthorId() {
        return authorId;
    }


    public String getText() {
        return text;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Message message = (Message) o;
        return id == message.id &&
                authorId == message.authorId &&
                Objects.equals(createTime, message.createTime) &&
                Objects.equals(text, message.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, createTime, authorId, text);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("createTime", createTime)
                .add("authorId", authorId)
                .add("text", text)
                .toString();
    }
}
