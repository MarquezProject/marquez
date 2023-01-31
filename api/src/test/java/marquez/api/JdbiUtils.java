/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.api;

import org.jdbi.v3.core.Jdbi;

public class JdbiUtils {

  public static void cleanDatabase(Jdbi jdbi) {
    jdbi.inTransaction(
        handle -> {
          handle.execute("DELETE FROM lineage_events");
          handle.execute("DELETE FROM runs_input_mapping");
          handle.execute("DELETE FROM dataset_versions_field_mapping");
          handle.execute("DELETE FROM stream_versions");
          handle.execute("DELETE FROM column_lineage");
          handle.execute("DELETE FROM dataset_facets");
          handle.execute("DELETE FROM dataset_versions");
          handle.execute("DELETE FROM dataset_symlinks");
          handle.execute("UPDATE runs SET start_run_state_uuid=NULL, end_run_state_uuid=NULL");
          handle.execute("DELETE FROM datasets_tag_mapping");
          handle.execute("DELETE FROM dataset_facets");
          handle.execute("DELETE FROM run_states");
          handle.execute("DELETE FROM job_facets");
          handle.execute("DELETE FROM run_facets");
          handle.execute("DELETE FROM runs");
          handle.execute("DELETE FROM run_args");
          handle.execute("DELETE FROM job_versions_io_mapping");
          handle.execute("DELETE FROM job_versions");
          handle.execute("DELETE FROM jobs");
          handle.execute("DELETE FROM dataset_fields_tag_mapping");
          handle.execute("DELETE FROM dataset_fields");
          handle.execute("DELETE FROM datasets");
          handle.execute("DELETE FROM sources");
          handle.execute("DELETE FROM namespace_ownerships");
          handle.execute("DELETE FROM namespaces");
          handle.execute("DELETE FROM run_facets");
          handle.execute("DELETE FROM job_facets");
          handle.execute("DELETE FROM facet_migration_lock");
          return null;
        });
  }
}
