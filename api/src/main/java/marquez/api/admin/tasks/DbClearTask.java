package marquez.api.admin.tasks;

import io.dropwizard.servlets.tasks.Task;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import lombok.NonNull;

public final class DbClearTask extends Task {
  public DbClearTask() {
    super("db-clear");
  }

  @Override
  public void execute(@NonNull Map<String, List<String>> parameters, @NonNull PrintWriter output)
      throws Exception {}
}
