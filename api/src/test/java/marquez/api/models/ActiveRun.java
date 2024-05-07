/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.api.models;

import static io.openlineage.client.OpenLineage.RunEvent.EventType.COMPLETE;
import static io.openlineage.client.OpenLineage.RunEvent.EventType.START;

import io.openlineage.client.OpenLineage;
import io.openlineage.client.OpenLineageClient;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import lombok.Getter;
import lombok.NonNull;

public class ActiveRun {
  // ...
  private final OpenLineage ol;
  private final OpenLineageClient olClient;

  // ...
  @Getter private final OpenLineage.Run run;
  @Getter private final OpenLineage.Job job;
  @Getter private final List<OpenLineage.InputDataset> inputs;
  @Getter private final List<OpenLineage.OutputDataset> outputs;

  private ActiveRun(
      @NonNull final OpenLineage ol,
      @NonNull final OpenLineageClient olClient,
      @NonNull final OpenLineage.Run run,
      @NonNull final OpenLineage.Job job,
      @Nullable final List<OpenLineage.InputDataset> inputs,
      @Nullable final List<OpenLineage.OutputDataset> outputs) {
    this.ol = ol;
    this.olClient = olClient;
    this.run = run;
    this.job = job;
    this.inputs = inputs;
    this.outputs = outputs;
  }

  public UUID getRunId() {
    return run.getRunId();
  }

  public String getJobName() {
    return job.getName();
  }

  public void startRun() {
    olClient.emit(ol.newRunEvent(newEventTime(), START, run, job, inputs, outputs));
  }

  public void endRun() {
    endRun(COMPLETE);
  }

  public void endRun(@NonNull OpenLineage.RunEvent.EventType eventType) {
    olClient.emit(ol.newRunEvent(newEventTime(), eventType, run, job, inputs, outputs));
  }

  private static ZonedDateTime newEventTime() {
    return Instant.now().atZone(ZoneId.of("America/Los_Angeles"));
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private OpenLineage ol;
    private OpenLineageClient olClient;

    private OpenLineage.Run run;
    private OpenLineage.Job job;
    private List<OpenLineage.InputDataset> inputs;
    private List<OpenLineage.OutputDataset> outputs;

    private Builder() {}

    public Builder run(@NonNull OpenLineage.Run run) {
      this.run = run;
      return this;
    }

    public Builder job(@NonNull OpenLineage.Job job) {
      this.job = job;
      return this;
    }

    public Builder inputs(@Nullable List<OpenLineage.InputDataset> inputs) {
      this.inputs = inputs;
      return this;
    }

    public Builder outputs(@Nullable List<OpenLineage.OutputDataset> outputs) {
      this.outputs = outputs;
      return this;
    }

    public ActiveRun build(@NonNull OpenLineage ol, @NonNull OpenLineageClient olClient) {
      return new ActiveRun(ol, olClient, run, job, inputs, outputs);
    }
  }
}
