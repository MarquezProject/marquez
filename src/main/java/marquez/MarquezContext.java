package marquez;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import marquez.api.DatasetResource;
import marquez.api.JobResource;
import marquez.api.NamespaceResource;
import marquez.api.OpenLineageResource;
import marquez.api.SourceResource;
import marquez.api.TagResource;
import marquez.api.exceptions.JdbiExceptionExceptionMapper;
import marquez.api.exceptions.MarquezServiceExceptionMapper;
import marquez.db.DatasetDao;
import marquez.db.DatasetFieldDao;
import marquez.db.DatasetVersionDao;
import marquez.db.JobContextDao;
import marquez.db.JobDao;
import marquez.db.JobVersionDao;
import marquez.db.NamespaceDao;
import marquez.db.NamespaceOwnershipDao;
import marquez.db.OpenLineageDao;
import marquez.db.OwnerDao;
import marquez.db.RunArgsDao;
import marquez.db.RunDao;
import marquez.db.RunStateDao;
import marquez.db.SourceDao;
import marquez.db.TagDao;
import marquez.service.DatasetService;
import marquez.service.JobService;
import marquez.service.NamespaceService;
import marquez.service.OpenLineageService;
import marquez.service.RunService;
import marquez.service.RunTransitionListener;
import marquez.service.SourceService;
import marquez.service.TagService;
import marquez.service.exceptions.MarquezServiceException;
import marquez.service.models.Tag;
import org.jdbi.v3.core.Jdbi;

@Getter
public final class MarquezContext {
  private final NamespaceDao namespaceDao;
  private final OwnerDao ownerDao;
  private final NamespaceOwnershipDao namespaceOwnershipDao;
  private final SourceDao sourceDao;
  private final DatasetDao datasetDao;
  private final DatasetFieldDao datasetFieldDao;
  private final DatasetVersionDao datasetVersionDao;
  private final JobDao jobDao;
  private final JobVersionDao jobVersionDao;
  private final JobContextDao jobContextDao;
  private final RunDao runDao;
  private final RunArgsDao runArgsDao;
  private final RunStateDao runStateDao;
  private final TagDao tagDao;

  private final List<RunTransitionListener> runTransitionListeners;

  private final NamespaceService namespaceService;
  private final SourceService sourceService;
  private final DatasetService datasetService;
  private final JobService jobService;
  private final TagService tagService;
  private final MarquezServiceExceptionMapper serviceExceptionMapper;

  private final NamespaceResource namespaceResource;
  private final SourceResource sourceResource;
  private final DatasetResource datasetResource;
  private final JobResource jobResource;
  private final TagResource tagResource;

  private final ImmutableList<Object> resources;
  private final JdbiExceptionExceptionMapper jdbiException;
  private final RunService runService;
  private final OpenLineageResource openLineageResource;
  private final OpenLineageService openLineageService;
  private final OpenLineageDao openLineageDao;

  @Builder
  private MarquezContext(
      @NonNull final Jdbi jdbi,
      @NonNull final ImmutableSet<Tag> tags,
      List<RunTransitionListener> runTransitionListeners)
      throws MarquezServiceException {
    if (runTransitionListeners == null) {
      runTransitionListeners = new ArrayList<>();
    }

    this.namespaceDao = jdbi.onDemand(NamespaceDao.class);
    this.ownerDao = jdbi.onDemand(OwnerDao.class);
    this.namespaceOwnershipDao = jdbi.onDemand(NamespaceOwnershipDao.class);
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
    this.runTransitionListeners = runTransitionListeners;

    this.namespaceService = new NamespaceService(namespaceDao, ownerDao, namespaceOwnershipDao);
    this.sourceService = new SourceService(sourceDao);
    this.datasetService =
        new DatasetService(
            namespaceDao, sourceDao, datasetDao, datasetFieldDao, datasetVersionDao, tagDao);
    this.runService =
        new RunService(
            jobVersionDao,
            datasetDao,
            runArgsDao,
            runDao,
            datasetVersionDao,
            runStateDao,
            runTransitionListeners);

    this.jobService =
        new JobService(
            namespaceDao, datasetDao, jobDao, jobVersionDao, jobContextDao, runDao, runService);
    this.tagService = new TagService(tagDao);
    this.tagService.init(tags);
    this.openLineageService = new OpenLineageService(openLineageDao, getLineageObjectMapper());
    this.serviceExceptionMapper = new MarquezServiceExceptionMapper();
    this.jdbiException = new JdbiExceptionExceptionMapper();

    this.namespaceResource = new NamespaceResource(namespaceService);
    this.sourceResource = new SourceResource(sourceService);
    this.datasetResource =
        new DatasetResource(namespaceService, datasetService, tagService, runService);
    this.jobResource = new JobResource(namespaceService, jobService, runService);
    this.tagResource = new TagResource(tagService);
    this.openLineageResource = new OpenLineageResource(openLineageService);

    this.resources =
        ImmutableList.of(
            namespaceResource,
            sourceResource,
            datasetResource,
            jobResource,
            tagResource,
            serviceExceptionMapper,
            jdbiException,
            openLineageResource);
  }

  public ObjectMapper getLineageObjectMapper() {
    ObjectMapper mapper = new ObjectMapper();
    mapper.setSerializationInclusion(Include.NON_NULL);
    mapper.registerModule(new JavaTimeModule());
    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    mapper.disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);

    return mapper;
  }
}
