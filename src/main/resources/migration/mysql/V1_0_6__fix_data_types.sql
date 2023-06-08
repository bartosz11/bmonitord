alter table monitors drop constraint monitors_agent_id_fk;
alter table monitors modify column agent_id uuid;
alter table agents modify column id uuid not null;
alter table monitors add constraint monitors_agent_id_fk foreign key (agent_id) references agents (id);
alter table heartbeats modify column status tinyint not null;
alter table monitors modify column last_status tinyint not null;
alter table monitors modify column type tinyint not null;
alter table notifications modify column type tinyint not null;
alter table statuspage_announcements modify column type tinyint not null;