package marquez.api.admin.tasks;

import static marquez.db.DbRetention.DEFAULT_NUMBER_OF_ROWS_PER_BATCH;
import static marquez.db.DbRetention.DEFAULT_RETENTION_DAYS;

import com.fasterxml.jackson.core.type.TypeReference;
import io.dropwizard.servlets.tasks.PostBodyTask;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import marquez.common.Utils;
import marquez.db.DbRetention;
import marquez.db.exceptions.DbRetentionException;
import org.jdbi.v3.core.Jdbi;
import ;

/** ... */
@Slf4j
public final class DbRetentionTask extends PostBodyTask {
  /* ... */
  private final Jdbi jdbi;

  /** ... */
  public DbRetentionTask(@NonNull final Jdbi jdbi) {
    super("db-retention");
    this.jdbi = jdbi;
  }

  @Override
  public void execute(
      @NonNull Map<String, List<String>> parameters,
      @NonNull String body,
      @NonNull PrintWriter output)
      throws DbRetentionException {
    final Policy retentionPolicy = Policy.from(body);
    output.println("Starting db retention task...");
    try {
      // Attempt to apply a database retention policy. An exception is thrown on failed retention
      // policy attempts requiring we handle the throwable and log the error.
      DbRetention.retentionOnDbOrError(
          jdbi, retentionPolicy.getNumberOfRowsPerBatch(), retentionPolicy.getRetentionDays());
    } catch (DbRetentionException errorOnDbRetention) {
      log.error(
          "Failed to apply retention policy of '{}' days to database!",
              retentionPolicy.getRetentionDays(),
          errorOnDbRetention);
    }
    output.println("DONE!");
  }

  /** Retention policy for {@link DbRetentionTask}. */
  @ToString
  static final class Policy {
    @Getter @Setter private int numberOfRowsPerBatch = DEFAULT_NUMBER_OF_ROWS_PER_BATCH;
    @Getter @Setter private int retentionDays = DEFAULT_RETENTION_DAYS;

    static Policy from(@NonNull String body) {
      return Utils.fromJson(body, new TypeReference<>() {});
    }
  }
}
