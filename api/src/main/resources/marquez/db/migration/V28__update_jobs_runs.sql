create index run_name_idx on runs (job_name, namespace_name);

ALTER TABLE runs ADD COLUMN run_time integer;
update runs set run_time = EXTRACT(epoch FROM ended_at - started_at)
WHERE current_run_state in ('COMPLETED', 'ABORTED', 'FAILED');

create materialized view current_run_state as select distinct on (r.job_name, r.namespace_name) r.*
from runs r
order by r.job_name, r.namespace_name, r.transitioned_at desc;

create materialized view daily_averages as
select
    r.namespace_name, r.job_name,
    SUM(run_time) AS run_sum,
    COUNT(1) as run_count,
    date_trunc('day', r.created_at) as day
from runs r
WHERE r.current_run_state = 'COMPLETED'
GROUP BY r.namespace_name, r.job_name, date_trunc('day', r.created_at)
ORDER BY 5 desc;

UPDATE jobs SET (current_inputs) = (select jsonb_agg(query) from
    (select distinct ds.namespace_name as "namespace", ds.name as "name"
     from job_versions_io_mapping m inner join job_versions jv on m.job_version_uuid = jv.uuid inner join datasets ds on m.dataset_uuid = ds.uuid
     where m.io_type = 'INPUT' and jobs.uuid = jv.job_uuid) query);

UPDATE jobs SET (current_outputs) = (select jsonb_agg(query) from
    (select distinct ds.namespace_name as "namespace", ds.name as "name"
     from job_versions_io_mapping m inner join job_versions jv on m.job_version_uuid = jv.uuid inner join datasets ds on m.dataset_uuid = ds.uuid
     where m.io_type = 'OUTPUT' and jobs.uuid = jv.job_uuid) query);