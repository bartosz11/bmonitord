CREATE TABLE users
(
    id         uuid primary key default uuid_generate_v4(),
    created_at timestamptz      default now(),
    updated_at timestamptz      default now(),
    deleted_at timestamptz,
    username   varchar(255) not null unique,
    password   varchar(255) not null,
    enabled    boolean          default true
);
CREATE TABLE targets
(
    id           uuid primary key default uuid_generate_v4(),
    created_at   timestamptz      default now(),
    updated_at   timestamptz      default now(),
    deleted_at   timestamptz,
    name         varchar(255) not null,
    checks_up    bigint,
    checks_down  bigint,
    max_retries  int,
    used_retries int,
    last_check   timestamptz,
    last_status  varchar(255),
    type         varchar(255),
    paused       boolean          default false,
    user_id      uuid         not null
);
CREATE TABLE targets_checkers
(
    id         uuid primary key default uuid_generate_v4(),
    target_id  uuid not null,
    checker_id uuid not null
);
CREATE TABLE target_http_info
(
    id                     uuid primary key default uuid_generate_v4(),
    created_at             timestamptz      default now(),
    updated_at             timestamptz      default now(),
    deleted_at             timestamptz,
    host                   varchar(255),
    allowed_codes          varchar(255),
    timeout                int,
    follow_redirects       boolean,
    verify_ssl_certificate boolean,
    target_id              uuid not null
);
CREATE TABLE target_ping_info
(
    id         uuid primary key default uuid_generate_v4(),
    created_at timestamptz      default now(),
    updated_at timestamptz      default now(),
    deleted_at timestamptz,
    host       varchar(255),
    timeout    int,
    target_id  uuid not null
);
CREATE TABLE heartbeats
(
    id         uuid primary key default uuid_generate_v4(),
    timestamp  timestamptz  not null,
    latency    bigint,
    status     varchar(255) not null,
    target_id  uuid         not null,
    checker_id uuid         not null
);
CREATE TABLE incidents
(
    id        uuid primary key default uuid_generate_v4(),
    start     timestamptz not null,
    "end"     timestamptz,
    duration  interval,
    ongoing   boolean     not null,
    target_id uuid        not null
);
CREATE TABLE notifications
(
    id          uuid primary key default uuid_generate_v4(),
    created_at  timestamptz      default now(),
    updated_at  timestamptz      default now(),
    deleted_at  timestamptz,
    name        varchar(255) not null,
    type        varchar(255) not null,
    credentials varchar(255) not null,
    user_id     uuid         not null
);
CREATE TABLE alarms
(
    id         uuid primary key default uuid_generate_v4(),
    created_at timestamptz      default now(),
    updated_at timestamptz      default now(),
    deleted_at timestamptz,
    name       varchar(255) not null,
    type       varchar(255) not null,
    active     boolean,
    muted      boolean,
    threshold  double precision,
    target_id  uuid         not null
);
CREATE TABLE alarms_notifications
(
    id              uuid primary key default uuid_generate_v4(),
    alarm_id        uuid not null,
    notification_id uuid not null
);

-- idk why I did this "the Hibernate way" so create the tables first then add FK constraints
-- FK_<CHILD TABLE>_<PARENT>_<COLUMN>
ALTER TABLE targets
    ADD CONSTRAINT fk_targets_users_id FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE;
ALTER TABLE notifications
    ADD CONSTRAINT fk_notifications_users_id FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE;
ALTER TABLE target_http_info
    ADD CONSTRAINT fk_target_http_info_targets_id FOREIGN KEY (target_id) REFERENCES targets (id) ON DELETE CASCADE;
ALTER TABLE target_ping_info
    ADD CONSTRAINT fk_target_ping_info_targets_id FOREIGN KEY (target_id) REFERENCES targets (id) ON DELETE CASCADE;
ALTER TABLE incidents
    ADD CONSTRAINT fk_incidents_targets_id FOREIGN KEY (target_id) REFERENCES targets (id) ON DELETE CASCADE;
ALTER TABLE heartbeats
    ADD CONSTRAINT fk_heartbeats_targets_id FOREIGN KEY (target_id) REFERENCES targets (id) ON DELETE CASCADE;
ALTER TABLE alarms
    ADD CONSTRAINT fk_alarms_targets_id FOREIGN KEY (target_id) REFERENCES targets (id) ON DELETE CASCADE;
ALTER TABLE heartbeats
    ADD CONSTRAINT fk_heartbeats_checkers_id FOREIGN KEY (checker_id) REFERENCES checkers (id) ON DELETE CASCADE;
ALTER TABLE targets_checkers
    ADD CONSTRAINT fk_targets_checkers_targets_id FOREIGN KEY (target_id) REFERENCES targets (id) ON DELETE CASCADE;
ALTER TABLE targets_checkers
    ADD CONSTRAINT fk_targets_checkers_checkers_id FOREIGN KEY (checker_id) REFERENCES checkers (id) ON DELETE CASCADE;
ALTER TABLE alarms_notifications
    ADD CONSTRAINT fk_alarms_notifications_alarms_id FOREIGN KEY (alarm_id) REFERENCES alarms (id) ON DELETE CASCADE;
ALTER TABLE alarms_notifications
    ADD CONSTRAINT fk_alarms_notifications_notifications_id FOREIGN KEY (notification_id) REFERENCES notifications (id) ON DELETE CASCADE;
