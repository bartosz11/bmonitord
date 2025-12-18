-- Running this migration will break retries on targets/alarms!

alter table alarms rename column triggered to active;
alter table alarms drop column triggered_state_changed_at;
alter table alarms add column suspended;
alter table alarms add column max_retries;
alter table alarms drop column used_retries;

alter table targets add column max_retries int8 not null default 0;
alter table targets add column used_retries int8 not null default 0;

alter table incidents drop constraint fk_alarm_id;
alter table incidents drop column alarm_id;

