/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package marquez.db;

import static org.jooq.impl.DSL.inline;
import static org.jooq.impl.DSL.jsonbArrayAgg;
import static org.jooq.impl.DSL.jsonbObject;
import static org.jooq.impl.DSL.lateral;
import static org.jooq.impl.DSL.one;
import static org.jooq.impl.DSL.param;
import static org.jooq.impl.DSL.using;
import static org.jooq.impl.DSL.zero;
import static org.jooq.util.postgres.PostgresDSL.arrayAggDistinct;
import static org.jooq.util.postgres.PostgresDSL.arrayCat;
import static org.jooq.util.postgres.PostgresDSL.arrayOverlap;
import static org.jooq.util.postgres.PostgresDSL.field;
import static org.jooq.util.postgres.PostgresDSL.name;
import static org.jooq.util.postgres.PostgresDSL.table;

import graphql.schema.DataFetchingFieldSelectionSet;
import graphql.schema.SelectedField;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import marquez.db.mappers.DatasetDataMapper;
import marquez.db.mappers.JobDataMapper;
import marquez.db.mappers.JobRowMapper;
import marquez.db.mappers.RunMapper;
import marquez.db.models.DatasetData;
import marquez.db.models.JobData;
import marquez.service.models.Run;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.sqlobject.SqlObject;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.BindList;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.JSONB;
import org.jooq.Record;
import org.jooq.Record2;
import org.jooq.SQLDialect;
import org.jooq.SelectFieldOrAsterisk;
import org.jooq.SelectJoinStep;
import org.jooq.SelectOnConditionStep;
import org.jooq.SelectQuery;
import org.jooq.WithStep;
import org.jooq.conf.ParamType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RegisterRowMapper(DatasetDataMapper.class)
@RegisterRowMapper(JobDataMapper.class)
@RegisterRowMapper(RunMapper.class)
@RegisterRowMapper(JobRowMapper.class)
public interface LineageDao extends SqlObject {

  DSLContext ctx = using(SQLDialect.POSTGRES);
  WithStep lineageQuery =
      ctx.withRecursive("job_io", "job_uuid", "inputs", "outputs")
          .as(
              ctx.select(
                      field("j.uuid").as("job_uuid"),
                      field(
                          arrayAggDistinct(field("io.dataset_uuid"))
                              .filterWhere(field("io_type").eq(inline("INPUT")))
                              .as("inputs")),
                      field(
                          arrayAggDistinct(field("io.dataset_uuid"))
                              .filterWhere(field("io_type").eq(inline("OUTPUT")))
                              .as("outputs")))
                  .from(table("jobs").as("j"))
                  .leftJoin(table(name("job_versions")).as("v"))
                  .on(field("j.current_version_uuid").eq(field("v.uuid")))
                  .leftJoin(table(name("job_versions_io_mapping")).as("io"))
                  .on(field("v.uuid").eq(field("io.job_version_uuid")))
                  .groupBy(field("j.uuid"))
                  .getQuery())
          .with("lineage", "job_uuid", "inputs", "outputs", "depth")
          .as(
              ctx.select(
                      field("job_uuid", UUID.class),
                      field("inputs", UUID[].class),
                      field("outputs", UUID[].class),
                      zero().as("depth"))
                  .from(table("job_io"))
                  .where(field("job_uuid").in(param("jobId")))
                  .union(
                      ctx.select(
                              field("io.job_uuid", UUID.class),
                              field("io.inputs", UUID[].class),
                              field("io.outputs", UUID[].class),
                              field("l.depth", Integer.class).plus(one()))
                          .from(table("job_io").as("io"))
                          .innerJoin(table("lineage").as("l"))
                          .on(field("io.job_uuid").notEqual(field("l.job_uuid")))
                          .and(
                              arrayOverlap(
                                  arrayCat(
                                      field("io.inputs", UUID[].class),
                                      field("io.outputs", UUID[].class)),
                                  arrayCat(
                                      field("l.inputs", UUID[].class),
                                      field("l.outputs", UUID[].class))))
                          .where(field("depth").lt(param("depth")))
                          .getQuery())
                  .getQuery());

  /**
   * Fetch all of the jobs that consume or produce the datasets that are consumed or produced by the
   * input jobIds. This returns a single layer from the BFS using datasets as edges. Jobs that have
   * no input or output datasets will have no results. Jobs that have no upstream producers or
   * downstream consumers will have the original jobIds returned.
   *
   * @param jobIds
   * @return
   */
  @SqlQuery(
      "WITH RECURSIVE\n"
          + "    job_io AS (\n"
          + "        SELECT j.uuid AS job_uuid,\n"
          + "        ARRAY_AGG(DISTINCT io.dataset_uuid) FILTER (WHERE io_type='INPUT') AS inputs,\n"
          + "        ARRAY_AGG(DISTINCT io.dataset_uuid) FILTER (WHERE io_type='OUTPUT') AS outputs\n"
          + "        FROM jobs j\n"
          + "        LEFT JOIN job_versions v on j.current_version_uuid = v.uuid\n"
          + "        LEFT JOIN job_versions_io_mapping io on v.uuid = io.job_version_uuid\n"
          + "        GROUP BY j.uuid\n"
          + "    ),\n"
          + "    lineage(job_uuid, inputs, outputs) AS (\n"
          + "        SELECT job_uuid, inputs, outputs, 0 AS depth\n"
          + "        FROM job_io\n"
          + "        WHERE job_uuid IN (<jobIds>)\n"
          + "        UNION\n"
          + "        SELECT io.job_uuid, io.inputs, io.outputs, l.depth + 1\n"
          + "        FROM job_io io,\n"
          + "             lineage l\n"
          + "        WHERE io.job_uuid != l.job_uuid AND\n"
          + "        array_cat(io.inputs, io.outputs) && array_cat(l.inputs, l.outputs)\n"
          + "        AND depth < :depth"
          + "    )\n"
          + "SELECT DISTINCT ON (l2.job_uuid) j.*, inputs AS input_uuids, outputs AS output_uuids, jc.context\n"
          + "FROM lineage l2\n"
          + "INNER JOIN jobs j ON j.uuid=l2.job_uuid\n"
          + "LEFT JOIN job_contexts jc on jc.uuid = j.current_job_context_uuid")
  Set<JobData> getLineage(@BindList Set<UUID> jobIds, int depth);

  default Set<JobData> getLineage(
      UUID jobId, int depth, DataFetchingFieldSelectionSet selectionSet) {
    if (selectionSet == null) {
      return getLineage(Collections.singleton(jobId), depth);
    }
    Collection<SelectFieldOrAsterisk> selectedFields = new ArrayList<>();
    selectedFields.addAll(
        Arrays.asList(
            table("j").asterisk(),
            field("inputs").as("input_uuids"),
            field("outputs").as("output_uuids")));
    Stream<SelectedField> jobFields =
        selectionSet.getImmediateFields().stream()
            .filter(n -> n.getName().equals("Job"));
    boolean includeContext = false;
    if (jobFields.anyMatch(n -> n.getName().equals("context"))) {
      selectedFields.add(field("jc.context"));
      includeContext = true;
    }
    SelectOnConditionStep<Record> selectQuery =
        lineageQuery
            .select(selectedFields)
            .distinctOn(field("l2.job_uuid"))
            .from(table("lineage").as("l2"))
            .innerJoin(table("jobs").as("j"))
            .on(field("j.uuid", UUID.class).eq(field("l2.job_uuid", UUID.class)));
    if (includeContext) {
      selectQuery =
          selectQuery
              .innerJoin(table("job_contexts").as("jc"))
              .on(field("jc.uuid").eq(field("j.current_job_context_uuid")));
    }

    SelectQuery<Record> query = selectQuery.getQuery();

    Logger logger = LoggerFactory.getLogger(LineageDao.class);
    String sql = query.getSQL(ParamType.NAMED);
    logger.info(sql);
    JobDataMapper mapper = new JobDataMapper();
    Set<JobData> data =
        getHandle().createQuery(sql).bind("jobId", jobId).bind("depth", depth).map(mapper).stream()
            .collect(Collectors.toSet());
    logger.info("Elapsed {} millis mapping job data", mapper.getSw().elapsed().toMillis());
    return data;
  }

  @SqlQuery("SELECT uuid from jobs where name = :jobName and namespace_name = :namespace")
  Optional<UUID> getJobUuid(String jobName, String namespace);

  @SqlQuery(
      "SELECT ds.*, dv.fields\n"
          + "FROM datasets ds\n"
          + "LEFT JOIN dataset_versions dv on dv.uuid = ds.current_version_uuid\n"
          + "WHERE ds.uuid IN (<dsUuids>);")
  Set<DatasetData> getDatasetData(@BindList Set<UUID> dsUuids);

  @SqlQuery(
      "select j.uuid from jobs j\n"
          + "inner join job_versions jv on jv.job_uuid = j.uuid\n"
          + "inner join job_versions_io_mapping io on io.job_version_uuid = jv.uuid\n"
          + "inner join datasets ds on ds.uuid = io.dataset_uuid\n"
          + "where ds.name = :datasetName and ds.namespace_name = :namespaceName\n"
          + "order by io_type DESC, jv.created_at DESC\n"
          + "limit 1")
  Optional<UUID> getJobFromInputOrOutput(String datasetName, String namespaceName);

  @SqlQuery(
      "WITH latest_runs AS (\n"
          + "    SELECT DISTINCT on(r.job_name, r.namespace_name) r.*, jv.version\n"
          + "    FROM runs r\n"
          + "    INNER JOIN job_versions jv ON jv.uuid=r.job_version_uuid\n"
          + "    WHERE jv.job_uuid in (<jobUuid>)\n"
          + "    ORDER BY r.job_name, r.namespace_name, created_at DESC\n"
          + ")\n"
          + "SELECT r.*, ra.args, ctx.context, f.facets,\n"
          + "  r.version AS job_version, ri.input_versions, ro.output_versions\n"
          + "  from latest_runs AS r\n"
          + "LEFT JOIN run_args AS ra ON ra.uuid = r.run_args_uuid\n"
          + "LEFT JOIN job_contexts AS ctx ON r.job_context_uuid = ctx.uuid\n"
          + "LEFT JOIN LATERAL (\n"
          + "    SELECT le.run_uuid, JSON_AGG(event->'run'->'facets') AS facets\n"
          + "    FROM lineage_events le\n"
          + "    WHERE le.run_uuid=r.uuid\n"
          + "    GROUP BY le.run_uuid\n"
          + ") AS f ON r.uuid=f.run_uuid\n"
          + "LEFT JOIN LATERAL (\n"
          + "    SELECT im.run_uuid,\n"
          + "           JSON_AGG(json_build_object('namespace', dv.namespace_name,\n"
          + "                                      'name', dv.dataset_name,\n"
          + "                                      'version', dv.version)) AS input_versions\n"
          + "    FROM runs_input_mapping im\n"
          + "    INNER JOIN dataset_versions dv on im.dataset_version_uuid = dv.uuid\n"
          + "    WHERE im.run_uuid=r.uuid\n"
          + "    GROUP BY im.run_uuid\n"
          + ") ri ON ri.run_uuid=r.uuid\n"
          + "LEFT JOIN LATERAL (\n"
          + "    SELECT run_uuid, JSON_AGG(json_build_object('namespace', namespace_name,\n"
          + "                                                'name', dataset_name,\n"
          + "                                                'version', version)) AS output_versions\n"
          + "    FROM dataset_versions\n"
          + "    WHERE run_uuid=r.uuid\n"
          + "    GROUP BY run_uuid\n"
          + ") ro ON ro.run_uuid=r.uuid")
  List<Run> getCurrentRuns(@BindList Collection<UUID> jobUuid);

  default List<Run> getCurrentRuns(
      Collection<UUID> jobUuid, DataFetchingFieldSelectionSet runFields) {
    DSLContext ctx = using(SQLDialect.POSTGRES);
    List<Field<?>> selectedRunFields = new ArrayList<>();
    selectedRunFields.add(field("r.*"));
    selectedRunFields.addAll(selectRunFields(runFields));
    boolean includeArgs = selectedRunFields.contains(field("ra.args"));
    boolean includeCtx = selectedRunFields.contains(field("ctx.context"));
    boolean includeFacets = selectedRunFields.contains(field("f.facets"));
    boolean includeInputVersions = false;
    boolean includeOutputVersions = false;
    if (runFields.contains("inputVersions/*")) {
      selectedRunFields.add(field("ri.input_versions"));
      includeInputVersions = true;
    }
    if (runFields.contains("outputVersions/*")) {
      selectedRunFields.add(field("ro.output_versions"));
      includeOutputVersions = true;
    }
    SelectJoinStep<Record> selectQuery =
        ctx.with("latest_runs")
            .as(getLatestRunsQuery(ctx))
            .select(selectedRunFields)
            .from(table("latest_runs").as("r"));
    if (includeArgs) {
      selectQuery =
          selectQuery
              .leftJoin(table("run_args").as("ra"))
              .on(field("ra.uuid").eq(field("r.run_args_uuid")));
    }
    if (includeCtx) {
      // job_contexts AS ctx ON r.job_context_uuid = ctx.uuid
      selectQuery =
          selectQuery
              .leftJoin(table("job_contexts").as("ctx"))
              .on(field("r.job_context_uuid").eq(field("ctx.uuid")));
    }
    if (includeInputVersions) {
      selectQuery =
          selectQuery
              .leftJoin(lateral(selectInputDatasets(ctx, field("r.uuid", UUID.class))).as("ri"))
              .on(field("ri.run_uuid").eq(field("r.uuid")));
    }
    if (includeOutputVersions) {
      selectQuery =
          selectQuery
              .leftJoin(lateral(selectOutputDatasets(ctx, field("r.uuid", UUID.class))).as("ro"))
              .on(field("ro.run_uuid").eq(field("r.uuid")));
    }
    if (includeFacets) {
      // f ON r.uuid=f.run_uuid
      selectQuery =
          selectQuery
              .leftJoin(lateral(getRunFacets(ctx, field("r.uuid", UUID.class))).as("f"))
              .on(field("f.run_uuid").eq(field("r.uuid")));
    }
    String sql = selectQuery.getQuery().getSQL();
    Handle handle = getHandle();
    return handle.createQuery(sql).bindList("jobIds", jobUuid).map(new RunMapper()).stream()
        .collect(Collectors.toList());
  }

  private List<Field<?>> selectRunFields(DataFetchingFieldSelectionSet runFields) {
    return runFields.getImmediateFields().stream()
        .map(
            f -> {
              switch (f.getName()) {
                case "args":
                  return field("ra.args");
                case "context":
                  return field("ctx.context");
                case "facets":
                  return field("f.facets");
                default:
                  return null;
              }
            })
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  default SelectQuery<Record> getLatestRunsQuery(DSLContext ctx) {
    return ctx.select(table("r").asterisk(), field("jv.version").as("job_version"))
        .distinctOn(field("r.job_name"), field("r.namespace_name"))
        .from(table("runs").as("r"))
        .join(table("job_versions").as("jv"))
        .on(field("jv.uuid").eq(field("r.job_version_uuid")))
        .where(field("jv.job_uuid").in(field("<jobIds>")))
        .orderBy(field("r.job_name"), field("r.namespace_name"), field("created_at").desc())
        .getQuery();
  }

  default SelectQuery<Record2<UUID, JSONB>> getRunFacets(DSLContext ctx, Field<UUID> runId) {
    return ctx.select(
            field("le.run_uuid", UUID.class), jsonbArrayAgg(field("event->'run'->'facets'")))
        .from(table("lineage_events").as("le"))
        .where(field("le.run_uuid").eq(runId))
        .groupBy(field("le.run_uuid"))
        .getQuery();
  }

  default SelectQuery<Record2<UUID, JSONB>> selectInputDatasets(DSLContext ctx, Field<UUID> runId) {
    return ctx.select(
            field("im.run_uuid", UUID.class),
            jsonbArrayAgg(
                    jsonbObject(
                        inline("namespace"),
                        field("dv.namespace_name"),
                        inline("name"),
                        field("dv.dataset_name"),
                        inline("version"),
                        field("dv.version")))
                .as("input_versions"))
        .from(table("runs_input_mapping").as("im"))
        .innerJoin(table("dataset_versions").as("dv"))
        .on(field("im.dataset_version-uuid").eq(field("dv.uuid")))
        .where(field("im.run_uuid").eq(runId))
        .groupBy(field("im.run_uuid"))
        .getQuery();
  }

  default SelectQuery<Record2<UUID, JSONB>> selectOutputDatasets(
      DSLContext ctx, Field<UUID> runId) {
    return ctx.select(
            field("run_uuid", UUID.class),
            jsonbArrayAgg(
                    jsonbObject(
                        inline("namespace"),
                        field("dv.namespace_name"),
                        inline("name"),
                        field("dv.dataset_name"),
                        inline("version"),
                        field("dv.version")))
                .as("input_versions"))
        .from(table("dataset_versions").as("dv"))
        .where(field("dv.run_uuid").eq(runId))
        .groupBy(field("dv.run_uuid"))
        .getQuery();
  }
}
