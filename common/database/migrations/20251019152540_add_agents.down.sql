drop table agents;

alter table heartbeats drop column payload;
alter table heartbeats alter column checker_id set not null;