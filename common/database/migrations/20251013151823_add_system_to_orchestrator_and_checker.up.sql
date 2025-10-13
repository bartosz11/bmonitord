ALTER TABLE checkers ADD COLUMN system bool not null default false;
ALTER TABLE orchestrators ADD COLUMN system bool not null default false;