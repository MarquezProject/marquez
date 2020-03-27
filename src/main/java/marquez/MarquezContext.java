package marquez;

import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import marquez.api.DatasetResource;
import marquez.api.JobResource;
import marquez.api.NamespaceResource;
import marquez.api.SourceResource;
import marquez.api.TagResource;
import marquez.api.exceptions.MarquezServiceExceptionMapper;
import marquez.db.DatasetDao;
import marquez.db.DatasetFieldDao;
import marquez.db.DatasetVersionDao;
import marquez.db.JobContextDao;
import marquez.db.JobDao;
import marquez.db.JobVersionDao;
import marquez.db.NamespaceDao;
import marquez.db.NamespaceOwnershipDao;
import marquez.db.OwnerDao;
import marquez.db.RunArgsDao;
import marquez.db.RunDao;
import marquez.db.RunStateDao;
import marquez.db.SourceDao;
import marquez.db.TagDao;
import marquez.service.DatasetService;
import marquez.service.JobService;
import marquez.service.NamespaceService;
import marquez.service.SourceService;
import marquez.service.TagService;
import marquez.service.exceptions.MarquezServiceException;
import marquez.service.models.Tag;
import org.jdbi.v3.core.Jdbi;

public class MarquezContext {

  @Getter private final NamespaceDao namespaceDao;
  @Getter private final OwnerDao ownerDao;
  @Getter private final NamespaceOwnershipDao namespaceOwnershipDao;
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

  @Getter private final NamespaceService namespaceService;
  @Getter private final SourceService sourceService;
  @Getter private final DatasetService datasetService;
  @Getter private final JobService jobService;
  @Getter private final TagService tagService;

  @Getter private final NamespaceResource namespaceResource;
  @Getter private final SourceResource sourceResource;
  @Getter private final DatasetResource datasetResource;
  @Getter private final JobResource jobResource;
  @Getter private final TagResource tagResource;
  @Getter private final MarquezServiceExceptionMapper marquezServiceExceptionMapper;

  @Getter private final List<Object> resources;

  public MarquezContext(Jdbi jdbi, List<Tag> tags) throws MarquezServiceException {

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

    this.namespaceService = new NamespaceService(namespaceDao, ownerDao, namespaceOwnershipDao);
    this.sourceService = new SourceService(sourceDao);
    this.datasetService =
        new DatasetService(
            namespaceDao, sourceDao, datasetDao, datasetFieldDao, datasetVersionDao, tagDao);
    this.jobService =
        new JobService(
            namespaceDao,
            datasetDao,
            datasetVersionDao,
            jobDao,
            jobVersionDao,
            jobContextDao,
            runDao,
            runArgsDao,
            runStateDao);
    this.tagService = new TagService(tagDao);
    this.tagService.init(tags);

    this.namespaceResource = new NamespaceResource(namespaceService);
    this.sourceResource = new SourceResource(sourceService);
    this.datasetResource =
        new DatasetResource(namespaceService, datasetService, jobService, tagService);
    this.jobResource = new JobResource(namespaceService, jobService);
    this.tagResource = new TagResource(tagService);
    this.marquezServiceExceptionMapper = new MarquezServiceExceptionMapper();

    this.resources =
        Arrays.asList(
            namespaceResource,
            sourceResource,
            datasetResource,
            jobResource,
            tagResource,
            marquezServiceExceptionMapper);
  }

  public static MarquezContextBuilder builder() {
    return new MarquezContextBuilder();
  }

  public static class MarquezContextBuilder {

    private final Jdbi jdbi;
    private final List<Tag> tags;

    private MarquezContextBuilder() {
      this(null, null);
    }

    private MarquezContextBuilder(Jdbi jdbi, List<Tag> tags) {
      super();
      this.jdbi = jdbi;
      this.tags = tags;
    }

    public MarquezContextBuilder jdbi(Jdbi jdbi) {
      return new MarquezContextBuilder(jdbi, this.tags);
    }

    public MarquezContextBuilder tags(List<Tag> tags) {
      return new MarquezContextBuilder(this.jdbi, tags);
    }

    public MarquezContext build() throws MarquezServiceException {
      return new MarquezContext(jdbi, tags);
    }
  }
}
