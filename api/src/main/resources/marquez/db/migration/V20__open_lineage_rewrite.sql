DROP TABLE lineage_events;
CREATE TABLE lineage_events (
  event_time timestamp with time zone,
  uuid uuid,
  event jsonb,
  CONSTRAINT lineage_event_pk
   PRIMARY KEY(event_time, uuid)
);
