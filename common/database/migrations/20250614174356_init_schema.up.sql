create table settings
(
    key   text primary key,
    value text
);

create table orchestrators
(
    id         bigserial primary key,
    created_at timestamptz,
    updated_at timestamptz,
    deleted_at timestamptz,
    name       text not null unique,
    host       text not null unique,
    leader     bool default false
);

create table checkers
(
    id         bigserial primary key,
    created_at timestamptz,
    updated_at timestamptz,
    deleted_at timestamptz,
    name       text not null,
    location   text,
    key        text not null
);

create table users
(
    id         bigserial primary key,
    created_at timestamptz,
    updated_at timestamptz,
    deleted_at timestamptz,
    username   text not null unique,
    password   text not null,
    enabled    bool default true,
    admin      bool default false
);

create table targets
(
    id           bigserial primary key,
    created_at   timestamptz,
    updated_at   timestamptz,
    deleted_at   timestamptz,
    checks_up    int8,
    checks_down  int8,
    max_retries  int8 not null,
    used_retries int8 not null default 0,
    last_check   timestamptz,
    last_status  int8,
    type         int8 not null,
    paused       bool          default false,
    timeout      int8,
    user_id      int8 not null
);

create table targets_checkers
(
    checker_id int8 not null,
    target_id  int8 not null
);

create table target_http_infos
(
    id               bigserial primary key,
    created_at       timestamptz,
    updated_at       timestamptz,
    deleted_at       timestamptz,
    host             text not null,
    allowed_codes    text not null,
    follow_redirects bool not null default false,
    verify_ssl_cert  bool not null default false,
    target_id        int8 not null
);

create table target_ping_infos
(
    id         bigserial primary key,
    created_at timestamptz,
    updated_at timestamptz,
    deleted_at timestamptz,
    host       text not null,
    target_id  int8 not null
);

create table heartbeats
(
    id         bigserial primary key,
    created_at timestamptz,
    updated_at timestamptz,
    deleted_at timestamptz,
    latency    int8,
    timestamp  timestamptz not null,
    status     int8        not null,
    target_id  int8        not null,
    checker_id int8        not null
);

create table incidents
(
    id         bigserial primary key,
    created_at timestamptz,
    updated_at timestamptz,
    deleted_at timestamptz,
    start      timestamptz not null,
    "end"      timestamptz,
    duration   int8,
    ongoing    bool        not null,
    target_id  int8        not null
);

create table notifications
(
    id          bigserial primary key,
    created_at  timestamptz,
    updated_at  timestamptz,
    deleted_at  timestamptz,
    name        text not null,
    type        int8 not null,
    credentials text not null,
    user_id     int8 not null
);

create table alarms
(
    id         bigserial primary key,
    created_at timestamptz,
    updated_at timestamptz,
    deleted_at timestamptz,
    name text not null,
    type int8 not null,
    active bool default false,
    muted bool default false,
    threshold numeric,
    threshold_field int8,
    target_id int8 not null
);

create table alarms_notifications (
    alarm_id int8 not null,
    notification_id int8 not null
);