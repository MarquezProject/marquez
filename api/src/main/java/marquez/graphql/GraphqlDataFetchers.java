package marquez.graphql;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.ImmutableMap;
import graphql.schema.DataFetcher;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import marquez.common.Utils;
import marquez.db.JobVersionDao.IoType;
import marquez.graphql.mapper.RowMap;
import org.jdbi.v3.core.Jdbi;

public class GraphqlDataFetchers {
  private GraphqlDaos dao;

  public GraphqlDataFetchers(Jdbi jdbi) {
    this.dao = jdbi.onDemand(GraphqlDaos.class);
  }

  public DataFetcher getDatasets() {
    return dataFetchingEnvironment -> {
      return dao.getDatasets();
    };
  }

  public DataFetcher getDatasetByNamespaceAndName() {
    return dataFetchingEnvironment -> {
      String name = dataFetchingEnvironment.getArgument("name");
      String namespace = dataFetchingEnvironment.getArgument("namespace");

      return dao.getDatasetByNamespaceAndName(namespace, name);
    };
  }

  public DataFetcher getSourcesByDataset() {
    return dataFetchingEnvironment -> {
      Map<String, Object> map = dataFetchingEnvironment.getSource();

      return dao.getSource((UUID) map.get("sourceUuid"));
    };
  }

  public DataFetcher getNamespaceByDataset() {
    return dataFetchingEnvironment -> {
      Map<String, Object> map = dataFetchingEnvironment.getSource();

      return dao.getNamespace((UUID) map.get("namespaceUuid"));
    };
  }

  public DataFetcher getCurrentVersionByDataset() {
    return dataFetchingEnvironment -> {
      Map<String, Object> map = dataFetchingEnvironment.getSource();

      return dao.getCurrentDatasetVersion((UUID) map.get("currentVersionUuid"));
    };
  }

  public DataFetcher getFieldsByDataset() {
    return dataFetchingEnvironment -> {
      Map<String, Object> map = dataFetchingEnvironment.getSource();

      return dao.getDatasetField((UUID) map.get("uuid"));
    };
  }

  public DataFetcher getJobVersionAsInputByDataset() {
    return dataFetchingEnvironment -> {
      Map<String, Object> map = dataFetchingEnvironment.getSource();

      return dao.getJobVersionsByIoMapping((UUID) map.get("uuid"), IoType.INPUT);
    };
  }

  public DataFetcher getVersionAsOutputByDataset() {
    return dataFetchingEnvironment -> {
      Map<String, Object> map = dataFetchingEnvironment.getSource();

      return dao.getJobVersionsByIoMapping((UUID) map.get("uuid"), IoType.OUTPUT);
    };
  }

  public DataFetcher getTagsByDataset() {
    return dataFetchingEnvironment -> {
      Map<String, Object> map = dataFetchingEnvironment.getSource();

      return dao.getTagsByDatasetTag((UUID) map.get("uuid"));
    };
  }

  public DataFetcher getVersionsByDataset() {
    return dataFetchingEnvironment -> {
      Map<String, Object> map = dataFetchingEnvironment.getSource();

      return dao.getDatasetVersionsByDataset((UUID) map.get("uuid"));
    };
  }

  public DataFetcher getDatasetFieldsByTag() {
    return dataFetchingEnvironment -> {
      Map<String, Object> map = dataFetchingEnvironment.getSource();

      return dao.getDatasetFieldsByTagUuid((UUID) map.get("uuid"));
    };
  }

  public DataFetcher getDatasetsByTag() {
    return dataFetchingEnvironment -> {
      Map<String, Object> map = dataFetchingEnvironment.getSource();

      return dao.getDatasetsByTagUuid((UUID) map.get("uuid"));
    };
  }

  public DataFetcher getDatasetsBySource() {
    return dataFetchingEnvironment -> {
      Map<String, Object> map = dataFetchingEnvironment.getSource();

      return dao.getDatasetsBySource((UUID) map.get("uuid"));
    };
  }

  public DataFetcher getRunByRunStateRecord() {
    return dataFetchingEnvironment -> {
      Map<String, Object> map = dataFetchingEnvironment.getSource();

      return dao.getRun((UUID) map.get("runUuid"));
    };
  }

  public DataFetcher getRunsByRunArgs() {
    return dataFetchingEnvironment -> {
      Map<String, Object> map = dataFetchingEnvironment.getSource();

      return dao.getRunsByRunArgs((UUID) map.get("uuid"));
    };
  }

  public DataFetcher getJobVersionByRun() {
    return dataFetchingEnvironment -> {
      Map<String, Object> map = dataFetchingEnvironment.getSource();

      return dao.getJobVersion((UUID) map.get("jobVersionUuid"));
    };
  }

  public DataFetcher getRunStatesByRun() {
    return dataFetchingEnvironment -> {
      Map<String, Object> map = dataFetchingEnvironment.getSource();

      return dao.getRunStateByRun((UUID) map.get("uuid"));
    };
  }

  public DataFetcher getStartStateByRun() {
    return dataFetchingEnvironment -> {
      Map<String, Object> map = dataFetchingEnvironment.getSource();

      return dao.getRunStateByUuid((UUID) map.get("startStateUuid"));
    };
  }

  public DataFetcher getEndStateByRun() {
    return dataFetchingEnvironment -> {
      Map<String, Object> map = dataFetchingEnvironment.getSource();

      return dao.getRunStateByUuid((UUID) map.get("endStateUuid"));
    };
  }

  public DataFetcher getInputsByRun() {
    return dataFetchingEnvironment -> {
      Map<String, Object> map = dataFetchingEnvironment.getSource();

      return dao.getDatasetVersionInputsByRun((UUID) map.get("uuid"));
    };
  }

  public DataFetcher getOutputsByRun() {
    return dataFetchingEnvironment -> {
      Map<String, Object> map = dataFetchingEnvironment.getSource();

      return dao.getDatasetVersionByRun((UUID) map.get("uuid"));
    };
  }

  public DataFetcher getRunArgsByRun() {
    return dataFetchingEnvironment -> {
      Map<String, Object> map = dataFetchingEnvironment.getSource();
      Map<String, Object> runArgs = dao.getRunArgs((UUID) map.get("runArgsUuid"));
      if (runArgs == null) {
        return null;
      }

      return Utils.fromJson(
          (String) map.get("args"), new TypeReference<ImmutableMap<String, String>>() {});
    };
  }

  public DataFetcher getNamespacesByOwner() {
    return dataFetchingEnvironment -> {
      Map<String, Object> map = dataFetchingEnvironment.getSource();

      return dao.getNamespacesByOwner((UUID) map.get("uuid"));
    };
  }

  public DataFetcher getOwnersByNamespace() {
    return dataFetchingEnvironment -> {
      Map<String, Object> map = dataFetchingEnvironment.getSource();

      return dao.getOwnersByNamespace((UUID) map.get("uuid"));
    };
  }

  public DataFetcher getCurrentOwnerByNamespace() {
    return dataFetchingEnvironment -> {
      Map<String, Object> map = dataFetchingEnvironment.getSource();

      return dao.getCurrentOwnerByNamespace((String) map.get("currentOwnerName"));
    };
  }

  public DataFetcher getJobContextByJobVersion() {
    return dataFetchingEnvironment -> {
      Map<String, Object> map = dataFetchingEnvironment.getSource();

      Map jobContext = dao.getJobContext((UUID) map.get("jobContextUuid"));
      if (jobContext == null) {
        return null;
      }
      return Utils.fromJson(
          (String) jobContext.get("context"), new TypeReference<ImmutableMap<String, String>>() {});
    };
  }

  public DataFetcher getLatestRunByJobVersion() {
    return dataFetchingEnvironment -> {
      Map<String, Object> map = dataFetchingEnvironment.getSource();
      UUID latestRunUuid = (UUID) map.get("latestRunUuid");
      if (latestRunUuid == null) {
        return null;
      }
      return dao.getRun(latestRunUuid);
    };
  }

  public DataFetcher getJobByJobVersion() {
    return dataFetchingEnvironment -> {
      Map<String, Object> map = dataFetchingEnvironment.getSource();

      return dao.getJob((UUID) map.get("jobUuid"));
    };
  }

  public DataFetcher getInputsByJobVersion() {
    return dataFetchingEnvironment -> {
      Map<String, Object> map = dataFetchingEnvironment.getSource();

      return dao.getIOMappingByJobVersion((UUID) map.get("uuid"), IoType.INPUT);
    };
  }

  public DataFetcher getOutputsByJobVersion() {
    return dataFetchingEnvironment -> {
      Map<String, Object> map = dataFetchingEnvironment.getSource();

      return dao.getIOMappingByJobVersion((UUID) map.get("uuid"), IoType.OUTPUT);
    };
  }

  public DataFetcher getVersionsByJob() {
    return dataFetchingEnvironment -> {
      Map<String, Object> map = dataFetchingEnvironment.getSource();

      return dao.getJobVersionByJob((UUID) map.get("uuid"));
    };
  }

  public DataFetcher getNamespaceByJob() {
    return dataFetchingEnvironment -> {
      Map<String, Object> map = dataFetchingEnvironment.getSource();

      return dao.getNamespace((UUID) map.get("namespaceUuid"));
    };
  }

  public DataFetcher getCurrentVersionByJob() {
    return dataFetchingEnvironment -> {
      Map<String, Object> map = dataFetchingEnvironment.getSource();

      return dao.getJobVersion((UUID) map.get("currentVersionUuid"));
    };
  }

  public DataFetcher getFieldsByDatasetVersion() {
    return dataFetchingEnvironment -> {
      Map<String, Object> map = dataFetchingEnvironment.getSource();

      return dao.getFields((UUID) map.get("uuid"));
    };
  }

  public DataFetcher getRunByDatasetVersion() {
    return dataFetchingEnvironment -> {
      Map<String, Object> map = dataFetchingEnvironment.getSource();

      return dao.getRun((UUID) map.get("runUuid"));
    };
  }

  public DataFetcher getDatasetByDatasetField() {
    return dataFetchingEnvironment -> {
      Map<String, Object> map = dataFetchingEnvironment.getSource();

      return dao.getDataset((UUID) map.get("uuid"));
    };
  }

  public DataFetcher getVersionsByDatasetField() {
    return dataFetchingEnvironment -> {
      Map<String, Object> map = dataFetchingEnvironment.getSource();

      return dao.getVersionsByDatasetField((UUID) map.get("uuid"));
    };
  }

  public DataFetcher getTagsByDatasetField() {
    return dataFetchingEnvironment -> {
      Map<String, Object> map = dataFetchingEnvironment.getSource();

      return dao.getTagsByDatasetField((UUID) map.get("uuid"));
    };
  }

  public DataFetcher getDatasetByDatasetVersion() {
    return dataFetchingEnvironment -> {
      Map<String, Object> map = dataFetchingEnvironment.getSource();

      return dao.getDataset((UUID) map.get("datasetUuid"));
    };
  }

  public DataFetcher getNamespaceByName() {
    return dataFetchingEnvironment -> {
      String name = dataFetchingEnvironment.getArgument("name");

      return dao.getNamespaceByName(name);
    };
  }

  public DataFetcher getJobsByNamespace() {
    return dataFetchingEnvironment -> {
      Map<String, Object> map = dataFetchingEnvironment.getSource();
      if (map.isEmpty()) return null;

      return dao.getJobsByNamespace((UUID) map.get("uuid"));
    };
  }

  public DataFetcher getDatasetsByNamespace() {
    return dataFetchingEnvironment -> {
      Map<String, Object> map = dataFetchingEnvironment.getSource();
      if (map.isEmpty()) {
        return null;
      }

      return dao.getDatasetsByNamespace((UUID) map.get("uuid"));
    };
  }

  public DataFetcher getJobs() {
    return dataFetchingEnvironment -> {
      return dao.getJobs();
    };
  }

  public DataFetcher getJobsByNamespaceAndName() {
    return dataFetchingEnvironment -> {
      String name = dataFetchingEnvironment.getArgument("name");
      String namespace = dataFetchingEnvironment.getArgument("namespace");

      return dao.getJobByNamespaceAndName(namespace, name);
    };
  }

  public DataFetcher searchJobs() {
    return dataFetchingEnvironment -> {
      String name = dataFetchingEnvironment.getArgument("name");
      if (name.isEmpty()) {
        return dao.getJobs();
      }

      return dao.searchJobs(toQueryString(name), name);
    };
  }

  public DataFetcher searchDatasets() {
    return dataFetchingEnvironment -> {
      String name = dataFetchingEnvironment.getArgument("name");
      if (name.isEmpty()) {
        return dao.getDatasets();
      }

      return dao.searchDatasets(toQueryString(name), name);
    };
  }

  private String toQueryString(String name) {
    StringJoiner tsQueryLiteral = new StringJoiner(" & ");
    for (String term : toQueryTerms(name)) {
      // Prefix matching: https://www.postgresql.org/docs/9.0/textsearch-controls.html
      tsQueryLiteral.add(String.format("%s:*", term));
    }

    return tsQueryLiteral.toString();
  }

  private List<String> toQueryTerms(String name) {
    List<String> terms = new ArrayList<>();
    Pattern p = Pattern.compile("((?:\\w|\\d)+)");
    Matcher m = p.matcher(name);
    while (m.find()) {
      terms.add(m.group(1));
    }

    return terms;
  }

  public DataFetcher getDatasetLineageInput() {
    return dataFetchingEnvironment -> {
      Map<String, Object> jobVersion = dataFetchingEnvironment.getSource();
      UUID latestRun = (UUID)jobVersion.get("latestRunUuid");
      return dao.getDatasetVersionInputsByRun(latestRun);
    };
  }

  public DataFetcher getDatasetLineageOutput() {
    return dataFetchingEnvironment -> {
      Map<String, Object> jobVersion = dataFetchingEnvironment.getSource();
      UUID latestRun = (UUID)jobVersion.get("latestRunUuid");

      return dao.getDatasetVersionByRun(latestRun);
    };
  }

  public DataFetcher getJobLineageInput() {
    return dataFetchingEnvironment -> {
      Map<String, Object> datasetVersion = dataFetchingEnvironment.getSource();
      UUID datasetVersionUuid = (UUID)datasetVersion.get("uuid");

      return dao.getDistinctJobVersionsByDatasetVersion(datasetVersionUuid);
    };
  }

  public DataFetcher getJobLineageOutput() {
    return dataFetchingEnvironment -> {
//      MarquezGraphqlContext context = dataFetchingEnvironment.getContext();
      Map<String, Object> datasetVersion = dataFetchingEnvironment.getSource();
      UUID datasetVersionUuid = (UUID)datasetVersion.get("uuid");

      return dao.getDistinctJobVersionsByDatasetVersionOutput(datasetVersionUuid);
    };
  }

  public DataFetcher datasetLineage() {
    return dataFetchingEnvironment -> {
      MarquezGraphqlContext context = dataFetchingEnvironment.getContext();
      return null;
    };
  }

  public DataFetcher jobLineage() {
    return dataFetchingEnvironment -> {
      String name = dataFetchingEnvironment.getArgument("name");
      String namespace = dataFetchingEnvironment.getArgument("namespace");

      Map<String, Object> job = dao.getJobByNamespaceAndName(namespace, name);
      if (job == null) return null;
      UUID currentVersion = (UUID)job.get("currentVersionUuid");

      Map<String, Object> jobVersion = dao.getJobVersion(currentVersion);
      if (jobVersion == null) return null;

        return ImmutableMap.of("data", jobVersion);
    };
  }
}
