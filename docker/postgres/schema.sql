CREATE TABLE messages (
    id         bigint PRIMARY KEY,
    created    TIMESTAMP NOT NULL,
    user_id    bigint NOT NULL,
    text       text NOT NULL
)