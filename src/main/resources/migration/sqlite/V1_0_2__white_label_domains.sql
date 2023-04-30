alter table statuspages add column white_label_domain_id integer;
create table white_label_domains (id integer, domain varchar(255), name varchar(255), statuspage_id integer, user_id integer, primary key (id));

