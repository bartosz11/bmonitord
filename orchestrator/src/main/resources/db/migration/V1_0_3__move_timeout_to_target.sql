ALTER TABLE target_http_info DROP COLUMN timeout;
ALTER TABLE target_ping_info DROP COLUMN timeout;
ALTER TABLE targets
    ADD COLUMN timeout int;
