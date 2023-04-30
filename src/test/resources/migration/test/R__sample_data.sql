
INSERT INTO users (username, password, enabled, last_updated)
 VALUES ('testuser', '$2a$10$eycvJT8oeSvzzaKZGrZNGOG92gC9/IOOmruasNrRugeRmuALzECvG', TRUE, 1680534720);
INSERT INTO users (username, password, enabled, last_updated)
 VALUES ('disabledaccount', '$2a$10$eycvJT8oeSvzzaKZGrZNGOG92gC9/IOOmruasNrRugeRmuALzECvG', FALSE, 1680534720);

INSERT INTO white_label_domains (name, domain, user_id, statuspage_id)
 VALUES ('Test white label domain', 'bmonitord-test1.example.com', 1, NULL);
INSERT INTO white_label_domains (name, domain, user_id, statuspage_id)
 VALUES ('Another test domain', 'bmonitord-test2.example.com', 1, NULL);

INSERT INTO statuspages (name, logo_link, logo_redirect, announcement_id, user_id, white_label_domain_id)
 VALUES ('Test statuspage', 'https://google.com/favicon.ico', 'https://google.com', NULL, 1, NULL);
INSERT INTO statuspage_announcements (title, type, content, statuspage_id)
 VALUES ('Example announcement', 1, 'Example announcement content goes here', 1);
UPDATE statuspages SET announcement_id = 1, white_label_domain_id = 1 WHERE id = 1;
UPDATE white_label_domains SET statuspage_id = 1 WHERE id = 1;
INSERT INTO statuspages (name, logo_link, logo_redirect, announcement_id, user_id, white_label_domain_id)
 VALUES ('Blank statuspage', NULL, NULL, NULL, 1, NULL);

INSERT INTO notifications (credentials, name, type, user_id)
 VALUES ('http://127.0.0.1:3000/', 'Example notification', 3, 1);
INSERT INTO notifications (credentials, name, type, user_id)
 VALUES ('http://127.0.0.1:3000/', 'Not used generic webhook notification', 3, 1);

INSERT INTO monitors (name, host, type, last_status, retries, timeout, last_check, last_successful_check, created_on, paused, checks_up, checks_down, allowed_http_codes, published, verify_certificate, user_id, agent_id)
 VALUES ('Google', 'https://google.com', 0, 3, 0, 5, 0, 0, 1680534720, FALSE, 0, 0, '200', TRUE, FALSE, 1, NULL);
INSERT INTO monitors (name, host, type, last_status, retries, timeout, last_check, last_successful_check, created_on, paused, checks_up, checks_down, allowed_http_codes, published, verify_certificate, user_id, agent_id)
 VALUES ('npx http-echo-server port 3000', 'http://127.0.0.1:3000/', 0, 3, 0, 5, 0, 0, 1680534720, FALSE, 0, 0, '200', TRUE, FALSE, 1, NULL);
INSERT INTO incidents (start_timestamp, end_timestamp, duration, ongoing, monitor_id)
 VALUES (1680534720, 1680534780, 60, FALSE, 1);
INSERT INTO monitors_incidents (monitor_id, incidents_id) VALUES (1, 1);
INSERT INTO incidents (start_timestamp, end_timestamp, duration, ongoing, monitor_id)
 VALUES (1680534720, 0, 60, TRUE, 1);
INSERT INTO monitors_incidents (monitor_id, incidents_id) VALUES (1, 2);
INSERT INTO incidents (start_timestamp, end_timestamp, duration, ongoing, monitor_id)
 VALUES (1680862410, 1680862470, 60, FALSE, 1);
INSERT INTO monitors_incidents (monitor_id, incidents_id) VALUES (1, 3);
INSERT INTO heartbeats (cpu_frequency, cpu_usage, disk_data, disks_usage, iowait, ram_usage, response_time, rx, status, swap_usage, timestamp, tx, monitor_id)
 VALUES (0, 0.0, NULL, 0.0, 0.0, 0.0, 100, 0, 1, 0.0, 1680534720, 0, 1);
INSERT INTO monitors_heartbeats (monitor_id, heartbeats_id) VALUES (1, 1);
INSERT INTO heartbeats (cpu_frequency, cpu_usage, disk_data, disks_usage, iowait, ram_usage, response_time, rx, status, swap_usage, timestamp, tx, monitor_id)
 VALUES (0, 0.0, NULL, 0.0, 0.0, 0.0, 100, 0, 1, 0.0, 1680862410, 0, 1);
INSERT INTO monitors_heartbeats (monitor_id, heartbeats_id) VALUES (1, 2);
INSERT INTO monitors_statuspages (monitor_id, statuspages_id) VALUES (1, 1);
INSERT INTO statuspages_monitors (statuspage_id, monitors_id) VALUES (1, 1);
INSERT INTO monitors_notifications (monitor_id, notifications_id) VALUES (1, 1);
INSERT INTO notifications_monitors (notification_id, monitors_id) VALUES (1, 1);