CREATE TABLE IF NOT EXISTS alerts
(
    uuid        UUID PRIMARY KEY,
    created_at  TIMESTAMPTZ  NOT NULL,
    entity_uuid UUID NOT NULL,
    entity_type VARCHAR(256) NOT NULL,
    type        VARCHAR(256) NOT NULL,
    config      JSONB
);

CREATE UNIQUE INDEX IF NOT EXISTS alerts_entity_uuid_entity_type_inx ON alerts (entity_uuid, entity_type);
CREATE UNIQUE INDEX IF NOT EXISTS alerts_entity_uuid_entity_type_type_inx ON alerts (entity_uuid, entity_type, type);

CREATE TABLE IF NOT EXISTS notifications
(
    uuid        UUID PRIMARY KEY,
    alert_uuid  UUID REFERENCES alerts (uuid) ON DELETE CASCADE,
    created_at  TIMESTAMPTZ NOT NULL,
    archived_at TIMESTAMPTZ
)