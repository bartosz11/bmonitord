create table agents
(
    id                 bigserial primary key,
    created_at         timestamptz,
    updated_at         timestamptz,
    deleted_at         timestamptz,
    target_id          int8 not null,
    key                text not null,
    installed          bool not null default false,
    last_data_received timestamptz,
    hide_ip            bool not null default true,
);

alter table heartbeats add column payload bytea;
alter table heartbeats alter column checker_id drop not null;