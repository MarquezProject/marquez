package marquez.common;

import com.github.henneberger.typekin.annotation.Model;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class MarquezModel {
  @Model(name = "RunModel", refName = "RunRef")
  public abstract class Run implements RunModel {
    public abstract UUID getUuid();

    public abstract Instant getCreatedAt();

    public abstract Instant getUpdatedAt();

    public abstract JobVersionRef getJobVersion();

    public abstract RunArgsRef getRunArgs();

    public abstract List<? extends DatasetVersionRef> getInputDatasetVersion();

    public abstract Optional<Instant> getNominalStartTime();

    public abstract Optional<Instant> getNominalEndTime();

    public abstract Optional<? extends RunStateRef> getCurrentRunState();

    public abstract Optional<? extends RunStateRef> getStartRunState();

    public abstract Optional<? extends RunStateRef> getEndRunState();
  }

  @Model(name = "RunArgsModel", refName = "RunArgsRef")
  public abstract class RunArgs implements RunArgsModel {
    public abstract String getUuid();

    public abstract Instant getCreatedAt();

    public abstract Map<String, String> getArgs();
  }

  @Model(name = "RunStateModel", refName = "RunStateRef")
  public abstract class RunState implements RunStateModel {
    public abstract String getUuid();

    public abstract Instant getTransitionedAt();

    public abstract marquez.common.models.RunState getState();
  }

  @Model(name = "DatasetVersionModel", refName = "DatasetVersionRef")
  public abstract class DatasetVersion implements DatasetVersionModel {
    public abstract String getUuid();

    public abstract String getCreatedAt();

    public abstract Instant getVersion();

    public abstract RunRef getRun();
  }

  @Model(name = "JobVersionModel", refName = "JobVersionRef")
  public abstract class JobVersion implements JobVersionModel {
    public abstract UUID getUuid();

    public abstract Instant getCreatedAt();

    public abstract Instant getUpdatedAt();

    public abstract RunRef getRun();
  }
}
