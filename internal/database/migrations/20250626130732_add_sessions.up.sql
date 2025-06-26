create table sessions
(
    id         bigserial primary key,
    created_at timestamptz,
    updated_at timestamptz,
    deleted_at timestamptz,
    expires_at timestamptz not null,
    last_active timestamptz,
    user_agent text,
    ip_address text,
    user_id int8 not null,
    foreign key (user_id) references users(id)
);