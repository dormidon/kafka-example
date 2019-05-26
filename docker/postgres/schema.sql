CREATE TABLE messages (
    id         bigint PRIMARY KEY,
    created    TIMESTAMP NOT NULL,
    user_name  varchar(64) NOT NULL,
    text       text NOT NULL
)