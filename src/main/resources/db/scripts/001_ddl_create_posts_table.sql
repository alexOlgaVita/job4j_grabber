create table posts (
    id serial primary key,
    name varchar(255),
    text text,
    link varchar(255) UNIQUE,
    created timestamp
);