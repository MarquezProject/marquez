package marquez;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.zip.GZIPOutputStream;
import javax.ws.rs.POST;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.junit.Test;

public class Export {
  //CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
  @Test
  public void export() throws Exception {
    String[] EDGES_HEADERS = new String[] {"~id", "~from", "~to", "~label"};

    String url = "jdbc:postgresql://localhost/marquez?user=marquez&password=marquez";
    Connection conn = DriverManager.getConnection(url);
    Object[][] data = {
        {
            "v_jobs_vertex.csv",
            new String[] {"~id", "~label", "JobName:String"},
            "select "
                + "namespace_name||'.'||name as jobId, "
                + "'job' as label, "
                + "name as name_property "
                + "from jobs"
        },
        {
            "v_runs_vertex.csv",
            new String[] {"~id", "~label"},
            "select "
                + "'run.'||namespace_name||'.'||job_name||'.'||uuid as id, "
                + "'run' as label "
                + "from runs"
        },
        {
            "v_dataset_vertex.csv",
            new String[] {"~id", "~label", "Name:String"},
            "select "
                + "namespace_name||'.'||name as datasetId,"
                + "'dataset' as label,"
                + "namespace_name||'.'||name "
                + "from datasets"
        },
        {
            "e_job_run_edge.csv",
            EDGES_HEADERS,
            "select "
                + "'e_run_job_'||runs.namespace_name||'.'||runs.job_name||'-'||runs.uuid as edgeId,"
                + "'run.'||runs.namespace_name||'.'||runs.job_name||'.'||runs.uuid as fromRun, "
                + "runs.namespace_name||'.'||runs.job_name as toJob,"
                + "'created_by' as label \n"
                + "from runs"
        },
        {
            "e_run_job_edge.csv",
            EDGES_HEADERS,
            "select "
                + "'e_job_run_'||runs.namespace_name||'.'||runs.job_name||'-'||runs.uuid as edgeId,"
                + "runs.namespace_name||'.'||runs.job_name as fromJob,"
                + "'run.'||runs.namespace_name||'.'||runs.job_name||'.'||runs.uuid as toRun, "
                + "'created_from' as label \n"
                + "from runs"
        },
        {
            "e_dataset_run_output_edges.csv",
            EDGES_HEADERS,
            "select "
                + "'e_out_'||datasets.namespace_name||'.'||datasets.name||'-'||runs.uuid as edgeId,"
                + "'run.'||runs.namespace_name||'.'||runs.job_name||'.'||runs.uuid as fromRun,"
                + "datasets.namespace_name||'.'||datasets.name as toDataset,"
                  + "'is_output' as label\n"
                + " from runs\n"
                + " inner join dataset_versions on runs.uuid = dataset_versions.run_uuid\n"
                + " inner join datasets on dataset_versions.dataset_uuid = datasets.uuid"
        },
        {
            "e_dataset_run_input_edges.csv",
            EDGES_HEADERS,
            "select "
                + "'e_in_'||datasets.namespace_name||'.'||datasets.name||'-'||runs.uuid as edgeId,"
                + "'run.'||runs.namespace_name||'.'||runs.job_name||runs.uuid as fromRun,"
                + "datasets.namespace_name||'.'||datasets.name toDataset, "
                + "'is_input' as label\n"
                + " from runs\n"
                + " inner join runs_input_mapping on runs.uuid = runs_input_mapping.run_uuid\n"
                + " inner join dataset_versions on runs_input_mapping.dataset_version_uuid = dataset_versions.uuid\n"
                + " inner join datasets on dataset_versions.dataset_uuid = datasets.uuid"
        },
        {
            "e_dataset_run_output_from_edges.csv",
            EDGES_HEADERS,
            "select "
                + "'e_out_from_'||runs.uuid||'-'||datasets.namespace_name||'.'||datasets.name as edgeId,"
                + "datasets.namespace_name||'.'||datasets.name as fromDataset,"
                + "'run.'||runs.namespace_name||'.'||runs.job_name||'.'||runs.uuid as toRun,"
                + "'created_by' as label\n"
                + " from runs\n"
                + " inner join dataset_versions on runs.uuid = dataset_versions.run_uuid\n"
                + " inner join datasets on dataset_versions.dataset_uuid = datasets.uuid"
        },
        {
            "e_dataset_run_input_from_edges.csv",
            EDGES_HEADERS,
            "select "
                + "'e_in_from_'||runs.uuid||'-'||datasets.namespace_name||'.'||datasets.name as edgeId,"
                + "datasets.namespace_name||'.'||datasets.name fromDataset, "
                + "'run.'||runs.namespace_name||'.'||runs.job_name||runs.uuid as toRun,"
                + "'read_by' as label\n"
                + " from runs\n"
                + " inner join runs_input_mapping on runs.uuid = runs_input_mapping.run_uuid\n"
                + " inner join dataset_versions on runs_input_mapping.dataset_version_uuid = dataset_versions.uuid\n"
                + " inner join datasets on dataset_versions.dataset_uuid = datasets.uuid"
        },
    };

    for (Object[] d : data) {
      generate(conn, (String)d[0], (String[])d[1], (String)d[2]);
    }
  }
  private void generate(Connection conn, String fileName, String[] headers, String query) throws Exception {
    Path file = Files.createTempFile("", fileName);
    BufferedWriter writer = Files.newBufferedWriter(file);
    try (CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT
        .withHeader(headers))) {
      try (Statement stmt = conn.createStatement()) {
        stmt.setFetchSize(100);
        ResultSet rs = stmt.executeQuery(query);
        int columns = rs.getMetaData().getColumnCount();
        while (rs.next()) {
          Object[] o = new Object[columns];
          for (int i = 0; i < columns; i++) {
            o[i] = rs.getString(i + 1);
          }
          printer.printRecord(o);
        }
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    }
    compress(file, fileName);
  }
  private void compress(Path file, String name) throws Exception {
    try (OutputStream fOut = Files.newOutputStream(Paths.get(name + ".gz"));
        BufferedOutputStream buffOut = new BufferedOutputStream(fOut);
        GZIPOutputStream gzipOS = new GZIPOutputStream(buffOut);) {
      Files.copy(file, gzipOS);
    }
  }
}
