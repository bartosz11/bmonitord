-- Running this migration will break retries on targets/alarms!
-- This migration cannot succeed on non-empty DB's due to new FK in incidents being not null

alter table alarms rename column active to triggered;
alter table alarms add column trigger_state_changed_at timestamptz;
alter table alarms add column suspended bool not null default false;
alter table alarms add column max_retries int8 not null default 0;
alter table alarms add column used_retries int8 not null default 0;

alter table targets drop column max_retries;
alter table targets drop column used_retries;

alter table incidents add column alarm_id int8 not null;
alter table incidents add constraint fk_alarm_id foreign key (alarm_id) references alarms(id);

