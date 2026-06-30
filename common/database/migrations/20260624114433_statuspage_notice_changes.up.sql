alter table statuspage_notice_updates add column date timestamptz not null default now();
drop table statuspage_notices_incidents;

create table statuspage_notices_targets (
    target_id int8 not null,
    statuspage_notice_id int8 not null
);

alter table statuspage_notices_targets
    add constraint statuspage_notices_targets_pk primary key (target_id, statuspage_notice_id);

alter table statuspage_notices rename column scheduled_for to scheduled_start_at;
alter table statuspages_notices rename column scheduled_until to scheduled_end_at;