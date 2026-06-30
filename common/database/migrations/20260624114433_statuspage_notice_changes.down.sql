alter table statuspage_notice_updates drop column date;
drop table statuspage_notices_targets;

create table statuspage_notices_incidents
(
    incident_id          int8 not null,
    statuspage_notice_id int8 not null
);

alter table statuspage_notices_incidents
    add constraint statuspage_notices_incidents_pk primary key (incident_id, statuspage_notice_id);

alter table statuspage_notices rename column scheduled_start_at to scheduled_for;
alter table statuspage_notices rename column scheduled_end_at to scheduled_until;