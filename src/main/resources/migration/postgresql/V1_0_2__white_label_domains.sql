alter table statuspages add column white_label_domain_id bigint;
create table white_label_domains (id bigserial not null, domain varchar(255), name varchar(255), statuspage_id bigint, user_id bigint, primary key (id));
alter table statuspages add constraint statuspages_domain_id_fk foreign key (white_label_domain_id) references white_label_domains;
alter table white_label_domains add constraint domains_statuspage_id_fk foreign key (statuspage_id) references statuspages;
alter table white_label_domains add constraint domains_user_id_fk foreign key (user_id) references users;