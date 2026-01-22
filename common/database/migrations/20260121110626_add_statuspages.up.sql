create table statuspages
(
    id                       bigserial primary key,
    created_at               timestamptz,
    updated_at               timestamptz,
    deleted_at               timestamptz,
    name                     text    not null,
    slug                     text    not null unique,
    title                    text    not null,
    description              text,
    logo_url                 text,
    logo_on_click_url        text,
    footer_content           text,
    display_checkmate_footer boolean not null default true,
    user_id                  int8    not null
);

create table statuspage_domains
(
    id            bigserial primary key,
    created_at    timestamptz,
    updated_at    timestamptz,
    deleted_at    timestamptz,
    domain        text not null,
    statuspage_id int8 not null
);

create table statuspage_groups
(
    id            bigserial primary key,
    created_at    timestamptz,
    updated_at    timestamptz,
    deleted_at    timestamptz,
    name          text    not null,
    description   text,
    position      numeric not null default 0,
    statuspage_id int8    not null
);

create table statuspage_targets
(
    id                  bigserial primary key,
    created_at          timestamptz,
    updated_at          timestamptz,
    deleted_at          timestamptz,
    position            numeric not null default 0,
    statuspage_group_id int8,
    statuspage_id       int8    not null,
    target_id           int8    not null
);

create table statuspage_notices
(
    id              bigserial primary key,
    created_at      timestamptz,
    updated_at      timestamptz,
    deleted_at      timestamptz,
    title           text,
    started_at      timestamptz,
    resolved_at     timestamptz,
    scheduled_for   timestamptz,
    scheduled_until timestamptz,
    status          int8 not null,
    type            int8 not null,
    severity        int8 not null,
    statuspage_id   int8 not null
);

create table statuspage_notices_incidents
(
    incident_id          int8 not null,
    statuspage_notice_id int8 not null
);

alter table statuspage_notices_incidents
    add constraint statuspage_notices_incidents_pk primary key (incident_id, statuspage_notice_id);

create table statuspage_notice_updates
(
    id         bigserial primary key,
    created_at timestamptz,
    updated_at timestamptz,
    deleted_at timestamptz,
    message    text not null,
    status     int8 not null,
    notice_id  int8 not null
);
