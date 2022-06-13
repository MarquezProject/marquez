/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.graphql;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.ImmutableMap;
import graphql.schema.DataFetcher;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import marquez.common.Utils;
import marquez.db.JobVersionDao.IoType;
import marquez.graphql.mapper.LineageResultMapper.DatasetResult;
import marquez.graphql.mapper.LineageResultMapper.JobResult;
import marquez.graphql.mapper.LineageResultMapper.LineageResult;
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

  public DataFetcher getQueryJobByNamespaceAndName() {
    return dataFetchingEnvironment -> {
      String name = dataFetchingEnvironment.getArgument("name");
      String namespace = dataFetchingEnvironment.getArgument("namespace");

      return dao.getJobByNamespaceAndName(namespace, name);
    };
  }

  public DataFetcher getLineageJobsByNamespaceAndName() {
    return dataFetchingEnvironment -> {
      JobResult job = dataFetchingEnvironment.getSource();
      if (job == null) {
        return null;
      }

      return dao.getJobByNamespaceAndName(job.getNamespace(), job.getName());
    };
  }

  public DataFetcher getLineageDatasetsByNamespaceAndName() {
    return dataFetchingEnvironment -> {
      DatasetResult ds = dataFetchingEnvironment.getSource();
      if (ds == null) {
        return null;
      }

      return dao.getDatasetsByNamespaceAndName(ds.getNamespace(), ds.getName());
    };
  }

  public DataFetcher getLineage() {
    return dataFetchingEnvironment -> {
      String jobName = dataFetchingEnvironment.getArgument("name");
      String namespace = dataFetchingEnvironment.getArgument("namespace");
      Integer depth = dataFetchingEnvironment.getArgument("depth");

      List<JobResult> results = dao.getLineage(jobName, namespace, depth);
      Set<LineageResult> formattedResults = flattenToLineageRows(results);
      return ImmutableMap.of("graph", formattedResults);
    };
  }

  private Set<LineageResult> flattenToLineageRows(List<JobResult> rows) {
    Set<LineageResult> lineageResults = new LinkedHashSet<>();
    for (JobResult result : rows) {
      lineageResults.add(result);
      lineageResults.addAll(result.getInEdges());
      lineageResults.addAll(result.getOutEdges());
    }

    return lineageResults;
  }
}
