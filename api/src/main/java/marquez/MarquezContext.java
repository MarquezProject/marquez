/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import graphql.kickstart.servlet.GraphQLHttpServlet;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NonNull;
import marquez.api.DatasetResource;
import marquez.api.JobResource;
import marquez.api.NamespaceResource;
import marquez.api.OpenLineageResource;
import marquez.api.SearchResource;
import marquez.api.SourceResource;
import marquez.api.TagResource;
import marquez.api.exceptions.JdbiExceptionExceptionMapper;
import marquez.db.BaseDao;
import marquez.db.DatasetDao;
import marquez.db.DatasetFieldDao;
import marquez.db.DatasetVersionDao;
import marquez.db.JobContextDao;
import marquez.db.JobDao;
import marquez.db.JobVersionDao;
import marquez.db.LineageDao;
import marquez.db.NamespaceDao;
import marquez.db.OpenLineageDao;
import marquez.db.RunArgsDao;
import marquez.db.RunDao;
import marquez.db.RunStateDao;
import marquez.db.SearchDao;
import marquez.db.SourceDao;
import marquez.db.TagDao;
import marquez.graphql.GraphqlSchemaBuilder;
import marquez.graphql.MarquezGraphqlServletBuilder;
import marquez.service.DatasetFieldService;
import marquez.service.DatasetService;
import marquez.service.DatasetVersionService;
import marquez.service.JobService;
import marquez.service.LineageService;
import marquez.service.NamespaceService;
import marquez.service.OpenLineageService;
import marquez.service.RunService;
import marquez.service.RunTransitionListener;
import marquez.service.ServiceFactory;
import marquez.service.SourceService;
import marquez.service.TagService;
import marquez.service.models.Tag;
import org.jdbi.v3.core.Jdbi;

@Getter
public final class MarquezContext {
  @Getter private final NamespaceDao namespaceDao;
  @Getter private final SourceDao sourceDao;
  @Getter private final DatasetDao datasetDao;
  @Getter private final DatasetFieldDao datasetFieldDao;
  @Getter private final DatasetVersionDao datasetVersionDao;
  @Getter private final JobDao jobDao;
  @Getter private final JobVersionDao jobVersionDao;
  @Getter private final JobContextDao jobContextDao;
  @Getter private final RunDao runDao;
  @Getter private final RunArgsDao runArgsDao;
  @Getter private final RunStateDao runStateDao;
  @Getter private final TagDao tagDao;
  @Getter private final OpenLineageDao openLineageDao;
  @Getter private final LineageDao lineageDao;
  @Getter private final SearchDao searchDao;

  @Getter private final List<RunTransitionListener> runTransitionListeners;

  @Getter private final NamespaceService namespaceService;
  @Getter private final SourceService sourceService;
  @Getter private final DatasetService datasetService;
  @Getter private final JobService jobService;
  @Getter private final TagService tagService;
  @Getter private final RunService runService;
  @Getter private final OpenLineageService openLineageService;
  @Getter private final LineageService lineageService;

  @Getter private final NamespaceResource namespaceResource;
  @Getter private final SourceResource sourceResource;
  @Getter private final DatasetResource datasetResource;
  @Getter private final JobResource jobResource;
  @Getter private final TagResource tagResource;
  @Getter private final OpenLineageResource openLineageResource;
  @Getter private final SearchResource searchResource;

  @Getter private final ImmutableList<Object> resources;
  @Getter private final JdbiExceptionExceptionMapper jdbiException;
  @Getter private final GraphQLHttpServlet graphqlServlet;

  private MarquezContext(
      @NonNull final Jdbi jdbi,
      @NonNull final ImmutableSet<Tag> tags,
      List<RunTransitionListener> runTransitionListeners) {
    if (runTransitionListeners == null) {
      runTransitionListeners = new ArrayList<>();
    }

    final BaseDao baseDao = jdbi.onDemand(NamespaceDao.class);
    this.namespaceDao = jdbi.onDemand(NamespaceDao.class);
    this.sourceDao = jdbi.onDemand(SourceDao.class);
    this.datasetDao = jdbi.onDemand(DatasetDao.class);
    this.datasetFieldDao = jdbi.onDemand(DatasetFieldDao.class);
    this.datasetVersionDao = jdbi.onDemand(DatasetVersionDao.class);
    this.jobDao = jdbi.onDemand(JobDao.class);
    this.jobVersionDao = jdbi.onDemand(JobVersionDao.class);
    this.jobContextDao = jdbi.onDemand(JobContextDao.class);
    this.runDao = jdbi.onDemand(RunDao.class);
    this.runArgsDao = jdbi.onDemand(RunArgsDao.class);
    this.runStateDao = jdbi.onDemand(RunStateDao.class);
    this.tagDao = jdbi.onDemand(TagDao.class);
    this.openLineageDao = jdbi.onDemand(OpenLineageDao.class);
    this.lineageDao = jdbi.onDemand(LineageDao.class);
    this.searchDao = jdbi.onDemand(SearchDao.class);
    this.runTransitionListeners = runTransitionListeners;

    this.namespaceService = new NamespaceService(baseDao);
    this.sourceService = new SourceService(baseDao);
    this.runService = new RunService(baseDao, runTransitionListeners);
    this.datasetService = new DatasetService(datasetDao, runService);

    this.jobService = new JobService(baseDao, runService);
    this.tagService = new TagService(baseDao);
    this.tagService.init(tags);
    this.openLineageService = new OpenLineageService(baseDao, runService);
    this.lineageService = new LineageService(lineageDao, jobDao);
    this.jdbiException = new JdbiExceptionExceptionMapper();
    final ServiceFactory serviceFactory =
        ServiceFactory.builder()
            .datasetService(datasetService)
            .jobService(jobService)
            .runService(runService)
            .namespaceService(namespaceService)
            .tagService(tagService)
            .openLineageService(openLineageService)
            .sourceService(sourceService)
            .lineageService(lineageService)
            .datasetFieldService(new DatasetFieldService(baseDao))
            .datasetVersionService(new DatasetVersionService(baseDao))
            .build();
    this.namespaceResource = new NamespaceResource(serviceFactory);
    this.sourceResource = new SourceResource(serviceFactory);
    this.datasetResource = new DatasetResource(serviceFactory);
    this.jobResource = new JobResource(serviceFactory, jobVersionDao);
    this.tagResource = new TagResource(serviceFactory);
    this.openLineageResource = new OpenLineageResource(serviceFactory);
    this.searchResource = new SearchResource(searchDao);

    this.resources =
        ImmutableList.of(
            namespaceResource,
            sourceResource,
            datasetResource,
            jobResource,
            tagResource,
            jdbiException,
            openLineageResource,
            searchResource);

    final MarquezGraphqlServletBuilder servlet = new MarquezGraphqlServletBuilder();
    this.graphqlServlet = servlet.getServlet(new GraphqlSchemaBuilder(jdbi));
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private Jdbi jdbi;
    private ImmutableSet<Tag> tags;
    private List<RunTransitionListener> runTransitionListeners;

    Builder() {
      this.tags = ImmutableSet.of();
      this.runTransitionListeners = new ArrayList<>();
    }

    public Builder jdbi(@NonNull Jdbi jdbi) {
      this.jdbi = jdbi;
      return this;
    }

    public Builder tags(@NonNull ImmutableSet<Tag> tags) {
      this.tags = tags;
      return this;
    }

    public Builder runTransitionListener(@NonNull RunTransitionListener runTransitionListener) {
      return runTransitionListeners(Lists.newArrayList(runTransitionListener));
    }

    public Builder runTransitionListeners(
        @NonNull List<RunTransitionListener> runTransitionListeners) {
      this.runTransitionListeners.addAll(runTransitionListeners);
      return this;
    }

    public MarquezContext build() {
      return new MarquezContext(jdbi, tags, runTransitionListeners);
    }
  }
}
