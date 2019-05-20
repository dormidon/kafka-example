package ru.mail.polis.channel.storage;

import java.io.Closeable;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.google.common.collect.Maps;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import ru.mail.polis.channel.Message;

/**
 * Main {@link Message}s storage over PostgreSQL.
 */
public class StorageService
        implements Closeable {

    private final HikariDataSource dataSource;
    private final NamedParameterJdbcTemplate jdbc;

    private static final RowMapper<Message> MESSAGE_ROW_MAPPER =
            (rs, rowNum) ->
                    new Message(
                            rs.getLong("id"),
                            rs.getTimestamp("created")
                                    .toLocalDateTime(),
                            rs.getLong("user_id"),
                            rs.getString("text"));

    public StorageService(@NotNull final String host, final int port,
                          @NotNull final String user,
                          @NotNull final String password,
                          @NotNull final String database) {
        final HikariConfig config = new HikariConfig();
        config.setDataSourceClassName("org.postgresql.ds.PGSimpleDataSource");
        config.setUsername(user);
        config.setPassword(password);
        config.setJdbcUrl("jdbc:postgresql://" + host + ":" + port + "/" + database);
        dataSource = new HikariDataSource(config);
        jdbc = new NamedParameterJdbcTemplate(dataSource);
    }

    public void write(@NotNull final Message message) {
        final HashMap<String, Object> parameters =
                Maps.newHashMapWithExpectedSize(4);
        parameters.put("id", message.getId());
        parameters.put("created", Timestamp.valueOf(message.getCreateTime()));
        parameters.put("user_id", message.getAuthorId());
        parameters.put("text", message.getText());
        jdbc.update(
                "INSERT INTO messages (id, user_id, created, text)\n" +
                        "VALUES (:id, :user_id, :created, :text)",
                parameters);
    }

    public List<Message> read(final long since,
                              final int count) {
        final HashMap<String, Object> parameters =
                Maps.newHashMapWithExpectedSize(2);
        parameters.put("id", since);
        parameters.put("limit", count);
        return jdbc.query(
                "SELECT id, user_id, created, text\n" +
                        "FROM messages\n" +
                        "WHERE id >= :id\n" +
                        "ORDER BY id\n" +
                        "LIMIT :limit",
                parameters,
                MESSAGE_ROW_MAPPER
        );
    }

    public List<Message> get(final List<Long> ids) {
        if (ids.isEmpty()) {
            return Collections.emptyList();
        }
        return jdbc.query(
                "SELECT id, user_id, created, text\n" +
                        "FROM messages\n" +
                        "WHERE id IN (:ids)\n" +
                        "ORDER BY id",
                Collections.singletonMap("ids", ids),
                MESSAGE_ROW_MAPPER
        );
    }

    @Override
    public void close() {
        dataSource.close();
    }
}
