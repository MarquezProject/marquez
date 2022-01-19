/* SPDX-License-Identifier: Apache-2.0 */

ALTER TABLE lineage_events
    ADD run_uuid uuid;

CREATE INDEX lineage_events_run_id_index
    on lineage_events(run_uuid);

CREATE INDEX lineage_events_job_name_index
    on lineage_events(job_name, job_namespace);

CREATE OR REPLACE FUNCTION write_run_uuid()
RETURNS trigger
LANGUAGE plpgsql AS
$func$
BEGIN
    NEW.run_uuid := NEW.run_id::uuid;
    RETURN NEW;
END
$func$;

CREATE TRIGGER lineage_events_run_uuid
    BEFORE INSERT ON lineage_events
    FOR EACH ROW
    WHEN (NEW.run_uuid IS NULL AND NEW.run_id IS NOT NULL)
EXECUTE PROCEDURE write_run_uuid();

UPDATE lineage_events SET run_uuid=run_id::uuid;
