alter table alarms add column system bool not null default false;
create unique index unique_unavailable_alarm_per_target on alarms (target_id) where type = 0 and system = true;