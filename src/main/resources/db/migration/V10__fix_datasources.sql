ALTER TABLE datasources ADD COLUMN urn VARCHAR(128) NOT NULL;
ALTER TABLE datasources ADD CONSTRAINT unique_name UNIQUE (name, urn)