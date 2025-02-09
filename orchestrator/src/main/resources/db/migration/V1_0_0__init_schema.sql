CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE settings
(
    key   varchar(255) primary key,
    value varchar(255)
);
CREATE TABLE orchestrators
(
    id         uuid primary key default uuid_generate_v4(),
    created_at timestamptz      default now(),
    updated_at timestamptz      default now(),
    deleted_at timestamptz,
    name       varchar(255),
    host       varchar(255) not null,
    leader     bool             default false
);
CREATE TABLE checkers
(
    id         uuid primary key default uuid_generate_v4(),
    created_at timestamptz      default now(),
    updated_at timestamptz      default now(),
    deleted_at timestamptz,
    name       varchar(255) not null,
    location   varchar(255),
    key        varchar(255) not null
);
