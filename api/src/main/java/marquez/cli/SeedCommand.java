/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.cli;

import static com.google.common.base.Preconditions.checkArgument;
import static marquez.client.models.JobType.BATCH;
import static marquez.common.base.MorePreconditions.checkNotBlank;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.dropwizard.cli.ConfiguredCommand;
import io.dropwizard.setup.Bootstrap;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import javax.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import marquez.MarquezConfig;
import marquez.client.MarquezClient;
import marquez.client.Utils;
import marquez.client.models.Dataset;
import marquez.client.models.DatasetId;
import marquez.client.models.DatasetMeta;
import marquez.client.models.DbTableMeta;
import marquez.client.models.Field;
import marquez.client.models.Job;
import marquez.client.models.JobMeta;
import marquez.client.models.Namespace;
import marquez.client.models.NamespaceMeta;
import marquez.client.models.Run;
import marquez.client.models.RunMeta;
import marquez.client.models.Source;
import marquez.client.models.SourceMeta;

/**
 * A command to seed the HTTP API with source, dataset, and job metadata. You can override the
 * default {@code host} and {@code port} using the command-line arguments {@code --host} and {@code
 * --port}. This command is meant to be used to explore the features of Marquez. For example,
 * lineage graph, dataset schemas, job run history, etc.
 *
 * <h2>Usage</h2>
 *
 * For example, to override the {@code port}:
 *
 * <pre>{@code
 * java -jar marquez-api.jar seed --port 5001 marquez.yml
 * }</pre>
 *
 * Note that all metadata is defined within this class and requires a running instance of Marquez.
 */
@Slf4j
public final class SeedCommand extends ConfiguredCommand<MarquezConfig> {
  static final String DEFAULT_MARQUEZ_HOST = "localhost";
  static final int DEFAULT_MARQUEZ_PORT = 8080;

  public static final String NAMESPACE_NAME = "food_delivery";
  static final String SOURCE_NAME = "analytics_db";

  static final int LINEAGE_GRAPH_24_HOUR_WINDOW = 24;
  static final int RUN_TIME_IN_SEC_MIN = 120;
  static final int RUN_TIME_IN_SEC_MAX = 240;

  public SeedCommand() {
    super("seed", "seeds the HTTP API with metadata");
  }

  @Override
  public void configure(@NonNull final net.sourceforge.argparse4j.inf.Subparser subparser) {
    super.configure(subparser);
    subparser
        .addArgument("--host")
        .dest("host")
        .type(String.class)
        .required(false)
        .setDefault(DEFAULT_MARQUEZ_HOST)
        .help("the HTTP API server host");
    subparser
        .addArgument("--port")
        .dest("port")
        .type(Integer.class)
        .required(false)
        .setDefault(DEFAULT_MARQUEZ_PORT)
        .help("the HTTP API server port");
  }

  @Override
  protected void run(
      @NonNull final Bootstrap<MarquezConfig> bootstrap,
      @NonNull final net.sourceforge.argparse4j.inf.Namespace namespace,
      @NonNull final MarquezConfig config) {
    final String host = namespace.getString("host");
    final int port = namespace.getInt("port");

    final URL baseUrl = Utils.toUrl(String.format("http://%s:%d", host, port));
    final MarquezClient client = MarquezClient.builder().baseUrl(baseUrl).build();
    seedApiWithMeta(client, LINEAGE_GRAPH_24_HOUR_WINDOW);
  }

  public void seedApiWithMeta(@NonNull MarquezClient client, int additionalIterations) {
    // (1) Create namespace
    final NamespaceMeta namespaceMeta =
        NamespaceMeta.builder()
            .ownerName("owner@food.com")
            .description("Food delivery example!")
            .build();
    final Namespace newNamespace = client.createNamespace(NAMESPACE_NAME, namespaceMeta);
    log.info("Created namespace: {}", newNamespace);

    // (2) Create source
    final SourceMeta sourceMeta =
        SourceMeta.builder()
            .type("POSTGRESQL")
            .connectionUrl("jdbc:postgres://localhost:3306/deliveries")
            .description("Contains all food delivery orders.")
            .build();
    final Source newSource = client.createSource(SOURCE_NAME, sourceMeta);
    log.info("Created source: {}", newSource);

    // (3) Seed dataset meta
    DATASET_META.forEach(
        (datasetName, datasetMeta) -> {
          final Dataset newDataset = client.createDataset(NAMESPACE_NAME, datasetName, datasetMeta);
          log.info("Created dataset: {}", newDataset);
        });

    // (4) Seed job meta
    JOB_META.forEach(
        (jobName, jobMeta) -> {
          final Job newJob = client.createJob(NAMESPACE_NAME, jobName, jobMeta);
          log.info("Created job: {}", newJob);
        });

    // (5) Define run start times for each graph level
    final Instant startTimesGraphLevel0 = Instant.now();
    final Instant startTimesGraphLevel1 = startTimesGraphLevel0.plusSeconds(RUN_TIME_IN_SEC_MAX);
    final Instant startTimesGraphLevel2 = startTimesGraphLevel1.plusSeconds(RUN_TIME_IN_SEC_MAX);
    final Instant startTimesGraphLevel3 = startTimesGraphLevel2.plusSeconds(RUN_TIME_IN_SEC_MAX);
    final Instant startTimesGraphLevel4 = startTimesGraphLevel3.plusSeconds(RUN_TIME_IN_SEC_MAX);

    final Instant[] startTimesByGraphLevel = {
      startTimesGraphLevel0,
      startTimesGraphLevel1,
      startTimesGraphLevel2,
      startTimesGraphLevel3,
      startTimesGraphLevel4
    };

    // (6) Seed run meta for jobs using graph level start times
    for (int hourOfDay = additionalIterations; hourOfDay >= 0; hourOfDay--) {
      for (final Map.Entry<String, JobMeta> entry : JOB_META.entrySet()) {
        final String jobName = entry.getKey();
        final JobMeta jobMeta = entry.getValue();

        if (hourOfDay >= ACTIVE_RUN_META.get(jobName).size()) {
          continue;
        }

        // On code change, create a new job version
        final ActiveRunMeta activeRunMeta = ACTIVE_RUN_META.get(jobName).get(hourOfDay);
        activeRunMeta
            .getCodeChange()
            .ifPresent(
                codeChange -> {
                  client.createJob(
                      NAMESPACE_NAME,
                      jobName,
                      JobMeta.builder()
                          .type(jobMeta.getType())
                          .inputs(jobMeta.getInputs())
                          .outputs(jobMeta.getOutputs())
                          .location(codeChange.getToUrl())
                          .context(jobMeta.getContext())
                          .description(jobMeta.getDescription().orElse(null))
                          .build());
                });

        // Set run start and end times
        final Instant runStartedAt =
            startTimesByGraphLevel[activeRunMeta.getLevelInGraph()].minus(
                Duration.ofHours(hourOfDay));
        final Instant runEndedAt = runStartedAt.plusSeconds(secondsToAdd());

        // Create run
        final RunMeta runMeta =
            RunMeta.builder()
                .nominalStartTime(runStartedAt.truncatedTo(ChronoUnit.MINUTES))
                .nominalEndTime(runEndedAt.truncatedTo(ChronoUnit.MINUTES))
                .build();
        final Run run = client.createRun(NAMESPACE_NAME, jobName, runMeta);
        log.info("Created run for job '{}': {}", jobName, run);

        // Start run
        final Run running = client.markRunAsRunning(run.getId(), runStartedAt);
        log.info("Marked run for job '{}' as 'RUNNING': {}", jobName, running);

        // Complete or fail run
        if (activeRunMeta.isMarkFailed()) {
          final Run failed = client.markRunAsFailed(run.getId(), runEndedAt);
          log.info("Marked run for job '{}' as 'FAILED': {}", jobName, failed);
        } else if (activeRunMeta.isMarkRunning()) {
          for (final DatasetId output : jobMeta.getOutputs()) {
            final DatasetMeta datasetMeta = DATASET_META.get(output.getName());
            final DbTableMeta.DbTableMetaBuilder dbTableMetaWithChange =
                DbTableMeta.builder()
                    .physicalName(datasetMeta.getPhysicalName())
                    .sourceName(datasetMeta.getSourceName())
                    .description(datasetMeta.getDescription().orElse(null))
                    .runId(run.getId());

            if (activeRunMeta.hasSchemaChange()) {
              activeRunMeta
                  .schemaChangeFor(output.getName())
                  .ifPresent(
                      schemaChange ->
                          dbTableMetaWithChange.fields(
                              fieldsWithChange(datasetMeta.getFields(), schemaChange)));
            } else {
              dbTableMetaWithChange.fields(datasetMeta.getFields());
            }
            final Dataset modifiedDataset =
                client.createDataset(
                    NAMESPACE_NAME, output.getName(), dbTableMetaWithChange.build());
            log.info(
                "Dataset '{}' modified by job '{}' on run '{}': {}",
                output.getName(),
                jobName,
                run.getId(),
                modifiedDataset);
          }

          final Run completed = client.markRunAsCompleted(run.getId(), runEndedAt);
          log.info("Marked run for job '{}' as 'COMPLETED': {}", jobName, completed);
        }
      }
    }
  }

  int secondsToAdd() {
    return new Random().nextInt((RUN_TIME_IN_SEC_MAX - RUN_TIME_IN_SEC_MIN) + 1)
        + RUN_TIME_IN_SEC_MIN;
  }

  List<Field> fieldsWithChange(
      @NonNull List<Field> fields, @NonNull ActiveRunMeta.SchemaChange change) {
    final ImmutableList.Builder<Field> fieldsWithChange = ImmutableList.builder();
    for (final Field field : fields) {
      fieldsWithChange.add(
          (change.getFieldName().equals(field.getName()))
              ? Field.builder()
                  .name(field.getName())
                  .type(change.getToType())
                  .tags(field.getTags())
                  .description(field.getDescription().orElse(null))
                  .build()
              : field);
    }
    return fieldsWithChange.build();
  }

  static final ImmutableMap<String, DbTableMeta> DATASET_META =
      new ImmutableMap.Builder<String, DbTableMeta>()
          .put(
              "public.orders",
              DbTableMeta.builder()
                  .physicalName("public.orders")
                  .sourceName(SOURCE_NAME)
                  .fields(
                      Lists.newArrayList(
                          Field.builder()
                              .name("id")
                              .type("INTEGER")
                              .description("The unique ID of the order.")
                              .build(),
                          Field.builder()
                              .name("placed_on")
                              .type("TIMESTAMP")
                              .description(
                                  "An ISO-8601 timestamp representing the date/time the order was placed.")
                              .build(),
                          Field.builder()
                              .name("menu_item_id")
                              .type("INTEGER")
                              .description("The ID of the menu item related to the order.")
                              .build(),
                          Field.builder()
                              .name("quantity")
                              .type("INTEGER")
                              .description("The number of the item in the order.")
                              .build(),
                          Field.builder()
                              .name("discount_id")
                              .type("INTEGER")
                              .description("The unique ID of the discount applied to the order.")
                              .build(),
                          Field.builder()
                              .name("comment")
                              .type("VARCHAR")
                              .description("The comment of the order.")
                              .build()))
                  .description("A table for orders.")
                  .build())
          .put(
              "public.menus",
              DbTableMeta.builder()
                  .physicalName("public.menus")
                  .sourceName(SOURCE_NAME)
                  .fields(
                      Lists.newArrayList(
                          Field.builder()
                              .name("id")
                              .type("INTEGER")
                              .description("The unique ID of the menu.")
                              .build(),
                          Field.builder()
                              .name("name")
                              .type("VARCHAR")
                              .description("The name of the menu.")
                              .build(),
                          Field.builder()
                              .name("restaurant_id")
                              .type("INTEGER")
                              .description("The ID of the restaurant related to the menu.")
                              .build(),
                          Field.builder()
                              .name("description")
                              .type("TEXT")
                              .description("The description of the menu.")
                              .build()))
                  .description("A table for menus.")
                  .build())
          .put(
              "public.categories",
              DbTableMeta.builder()
                  .physicalName("public.categories")
                  .sourceName(SOURCE_NAME)
                  .fields(
                      Lists.newArrayList(
                          Field.builder()
                              .name("id")
                              .type("INTEGER")
                              .description("The unique ID of the category.")
                              .build(),
                          Field.builder()
                              .name("name")
                              .type("VARCHAR")
                              .description("The name of the category.")
                              .build(),
                          Field.builder()
                              .name("menu_id")
                              .type("INTEGER")
                              .description("The ID of the menu related to the category.")
                              .build(),
                          Field.builder()
                              .name("description")
                              .type("TEXT")
                              .description("The description of the category.")
                              .build()))
                  .description("A table for categories.")
                  .build())
          .put(
              "public.menu_items",
              DbTableMeta.builder()
                  .physicalName("public.menu_items")
                  .sourceName(SOURCE_NAME)
                  .fields(
                      Lists.newArrayList(
                          Field.builder()
                              .name("id")
                              .type("INTEGER")
                              .description("The unique ID of the menu item.")
                              .build(),
                          Field.builder()
                              .name("name")
                              .type("VARCHAR")
                              .description("The name of the menu item.")
                              .build(),
                          Field.builder()
                              .name("price")
                              .type("INTEGER")
                              .description("The price of the menu item.")
                              .build(),
                          Field.builder()
                              .name("category_id")
                              .type("INTEGER")
                              .description("The ID of the category related to the item.")
                              .build(),
                          Field.builder()
                              .name("description")
                              .type("TEXT")
                              .description("The description of the menu item.")
                              .build()))
                  .description("A table for menu items.")
                  .build())
          .put(
              "public.restaurants",
              DbTableMeta.builder()
                  .physicalName("public.restaurants")
                  .sourceName(SOURCE_NAME)
                  .fields(
                      Lists.newArrayList(
                          Field.builder()
                              .name("id")
                              .type("INTEGER")
                              .description("The unique ID of the restaurant.")
                              .build(),
                          Field.builder()
                              .name("created_at")
                              .type("TIMESTAMP")
                              .description(
                                  "An ISO-8601 timestamp representing the date/time the restaurant was created.")
                              .build(),
                          Field.builder()
                              .name("updated_at")
                              .type("TIMESTAMP")
                              .description(
                                  "An ISO-8601 timestamp representing the date/time the restaurant was updated.")
                              .build(),
                          Field.builder()
                              .name("name")
                              .type("VARCHAR")
                              .description("The name of the restaurant.")
                              .build(),
                          Field.builder()
                              .name("email")
                              .type("VARCHAR")
                              .tags(Sets.newHashSet("PII"))
                              .description("The email address of the customer.")
                              .build(),
                          Field.builder()
                              .name("address")
                              .type("VARCHAR")
                              .description("The address of the restaurant.")
                              .build(),
                          Field.builder()
                              .name("phone")
                              .type("VARCHAR")
                              .description("The phone number of the restaurant.")
                              .build(),
                          Field.builder()
                              .name("city_id")
                              .type("INTEGER")
                              .description("The ID of the city related to the restaurant.")
                              .build(),
                          Field.builder()
                              .name("business_hours_id")
                              .type("INTEGER")
                              .description(
                                  "The ID of the business hours related to the restaurant.")
                              .build(),
                          Field.builder()
                              .name("description")
                              .type("TEXT")
                              .description("The description of the restaurant.")
                              .build()))
                  .description("A table for customers.")
                  .build())
          .put(
              "public.customers",
              DbTableMeta.builder()
                  .physicalName("public.customers")
                  .sourceName(SOURCE_NAME)
                  .fields(
                      Lists.newArrayList(
                          Field.builder()
                              .name("id")
                              .type("INTEGER")
                              .description("The unique ID of the customer.")
                              .build(),
                          Field.builder()
                              .name("created_at")
                              .type("TIMESTAMP")
                              .description(
                                  "An ISO-8601 timestamp representing the date/time the customer was created.")
                              .build(),
                          Field.builder()
                              .name("updated_at")
                              .type("TIMESTAMP")
                              .description(
                                  "An ISO-8601 timestamp representing the date/time the customer was updated.")
                              .build(),
                          Field.builder()
                              .name("name")
                              .type("VARCHAR")
                              .description("The name of the customer.")
                              .build(),
                          Field.builder()
                              .name("email")
                              .type("VARCHAR")
                              .tags(Sets.newHashSet("PII"))
                              .description("The email address of the customer.")
                              .build(),
                          Field.builder()
                              .name("address")
                              .type("VARCHAR")
                              .description("The address of the customer.")
                              .build(),
                          Field.builder()
                              .name("phone")
                              .type("VARCHAR")
                              .description("The phone number of the customer.")
                              .build(),
                          Field.builder()
                              .name("city_id")
                              .type("INTEGER")
                              .description("The ID of the city related to the customer.")
                              .build()))
                  .description("A table for customers.")
                  .build())
          .put(
              "public.order_status",
              DbTableMeta.builder()
                  .physicalName("public.order_status")
                  .sourceName(SOURCE_NAME)
                  .fields(
                      Lists.newArrayList(
                          Field.builder()
                              .name("id")
                              .type("INTEGER")
                              .description("The unique ID of the order status.")
                              .build(),
                          Field.builder()
                              .name("transitioned_at")
                              .type("TIMESTAMP")
                              .description(
                                  "An ISO-8601 timestamp representing the date/time the order status was transitioned.")
                              .build(),
                          Field.builder()
                              .name("status")
                              .type("VARCHAR")
                              .description("The status of the order status.")
                              .build(),
                          Field.builder()
                              .name("order_id")
                              .type("INTEGER")
                              .description("The ID of the order related to the order status.")
                              .build(),
                          Field.builder()
                              .name("customer_id")
                              .type("INTEGER")
                              .description("The ID of the customer related to the order status.")
                              .build(),
                          Field.builder()
                              .name("restaurant_id")
                              .type("INTEGER")
                              .description("The ID of the restaurant related to the order status.")
                              .build(),
                          Field.builder()
                              .name("driver_id")
                              .type("INTEGER")
                              .description("The ID of the driver related to the order status.")
                              .build()))
                  .description("A table for order status.")
                  .build())
          .put(
              "public.orders_7_days",
              DbTableMeta.builder()
                  .physicalName("public.orders_7_days")
                  .sourceName(SOURCE_NAME)
                  .fields(
                      Lists.newArrayList(
                          Field.builder()
                              .name("order_id")
                              .type("INTEGER")
                              .description("The ID of the order.")
                              .build(),
                          Field.builder()
                              .name("placed_on")
                              .type("TIMESTAMP")
                              .description(
                                  "An ISO-8601 timestamp representing the date/time the order was placed.")
                              .build(),
                          Field.builder()
                              .name("menu_id")
                              .type("VARCHAR")
                              .description("The ID of the menu related to the order.")
                              .build(),
                          Field.builder()
                              .name("menu_item_id")
                              .type("INTEGER")
                              .description("The ID of the menu item related to the order.")
                              .build(),
                          Field.builder()
                              .name("category_id")
                              .type("INTEGER")
                              .description("The ID of category related to the order.")
                              .build(),
                          Field.builder()
                              .name("discount_id")
                              .type("INTEGER")
                              .description("The ID of the discount applied to the order.")
                              .build(),
                          Field.builder()
                              .name("city_id")
                              .type("INTEGER")
                              .description("The ID of the city related to the order.")
                              .build()))
                  .description("A table for weekly orders.")
                  .build())
          .put(
              "public.drivers",
              DbTableMeta.builder()
                  .physicalName("public.drivers")
                  .sourceName(SOURCE_NAME)
                  .fields(
                      Lists.newArrayList(
                          Field.builder()
                              .name("id")
                              .type("INTEGER")
                              .description("The unique ID of the driver.")
                              .build(),
                          Field.builder()
                              .name("created_at")
                              .type("TIMESTAMP")
                              .description(
                                  "An ISO-8601 timestamp representing the date/time the driver was created.")
                              .build(),
                          Field.builder()
                              .name("updated_at")
                              .type("TIMESTAMP")
                              .description(
                                  "An ISO-8601 timestamp representing the date/time the driver was updated.")
                              .build(),
                          Field.builder()
                              .name("name")
                              .type("VARCHAR")
                              .description("The name of the driver.")
                              .build(),
                          Field.builder()
                              .name("email")
                              .type("VARCHAR")
                              .description("The email of the driver.")
                              .build(),
                          Field.builder()
                              .name("phone")
                              .type("VARCHAR")
                              .description("The phone number of the driver.")
                              .build(),
                          Field.builder()
                              .name("car_make")
                              .type("VARCHAR")
                              .description("The make of the car.")
                              .build(),
                          Field.builder()
                              .name("car_model")
                              .type("VARCHAR")
                              .description("The model of the car.")
                              .build(),
                          Field.builder()
                              .name("car_year")
                              .type("VARCHAR")
                              .description("The year of the car.")
                              .build(),
                          Field.builder()
                              .name("car_color")
                              .type("VARCHAR")
                              .description("The color of the car.")
                              .build(),
                          Field.builder()
                              .name("car_license_plate")
                              .type("VARCHAR")
                              .description("The license plate number of the car.")
                              .build()))
                  .description("A table for drivers.")
                  .build())
          .put(
              "public.delivery_7_days",
              DbTableMeta.builder()
                  .physicalName("public.delivery_7_days")
                  .sourceName(SOURCE_NAME)
                  .fields(
                      Lists.newArrayList(
                          Field.builder()
                              .name("order_id")
                              .type("INTEGER")
                              .description("The ID of the order.")
                              .build(),
                          Field.builder()
                              .name("order_placed_on")
                              .type("TIMESTAMP")
                              .description(
                                  "An ISO-8601 timestamp representing the date/time the order was placed.")
                              .build(),
                          Field.builder()
                              .name("order_dispatched_on")
                              .type("TIMESTAMP")
                              .description(
                                  "An ISO-8601 timestamp representing the date/time the order was dispatched.")
                              .build(),
                          Field.builder()
                              .name("order_delivered_on")
                              .type("TIMESTAMP")
                              .description(
                                  "An ISO-8601 timestamp representing the date/time the order was delivered.")
                              .build(),
                          Field.builder()
                              .name("customer_email")
                              .type("VARCHAR")
                              .description("The email of the customer.")
                              .build(),
                          Field.builder()
                              .name("menu_id")
                              .type("INTEGER")
                              .description("The ID of the menu related to the order.")
                              .build(),
                          Field.builder()
                              .name("menu_item_id")
                              .type("INTEGER")
                              .description("The ID of the menu item related to the order.")
                              .build(),
                          Field.builder()
                              .name("category_id")
                              .type("INTEGER")
                              .description("The ID of category related to the order.")
                              .build(),
                          Field.builder()
                              .name("discount_id")
                              .type("INTEGER")
                              .description("The ID of the discount applied to the order.")
                              .build(),
                          Field.builder()
                              .name("city_id")
                              .type("INTEGER")
                              .description("The ID of the city related to the order.")
                              .build(),
                          Field.builder()
                              .name("driver_id")
                              .type("INTEGER")
                              .description("The ID of the driver related to the order.")
                              .build()))
                  .description("A table for weekly deliveries.")
                  .build())
          .put(
              "public.discounts",
              DbTableMeta.builder()
                  .physicalName("public.discounts")
                  .sourceName(SOURCE_NAME)
                  .fields(
                      Lists.newArrayList(
                          Field.builder()
                              .name("id")
                              .type("INTEGER")
                              .description("The unique ID of the discount.")
                              .build(),
                          Field.builder()
                              .name("amount_off")
                              .type("INTEGER")
                              .description("The amount of the discount.")
                              .build(),
                          Field.builder()
                              .name("customer_email")
                              .type("VARCHAR")
                              .description("The email of the customer.")
                              .build(),
                          Field.builder()
                              .name("starts_on")
                              .type("TIMESTAMP")
                              .description(
                                  "An ISO-8601 timestamp representing the date/time the discount starts.")
                              .build(),
                          Field.builder()
                              .name("ends_on")
                              .type("TIMESTAMP")
                              .description(
                                  "An ISO-8601 timestamp representing the date/time the discount ends.")
                              .build()))
                  .description("A table for discounts.")
                  .build())
          .put(
              "public.top_delivery_times",
              DbTableMeta.builder()
                  .physicalName("public.top_delivery_times")
                  .sourceName(SOURCE_NAME)
                  .fields(
                      Lists.newArrayList(
                          Field.builder()
                              .name("order_id")
                              .type("INTEGER")
                              .description("The ID of the order.")
                              .build(),
                          Field.builder()
                              .name("order_placed_on")
                              .type("TIMESTAMP")
                              .description(
                                  "An ISO-8601 timestamp representing the date/time the order was placed.")
                              .build(),
                          Field.builder()
                              .name("order_dispatched_on")
                              .type("TIMESTAMP")
                              .description(
                                  "An ISO-8601 timestamp representing the date/time the order was dispatched.")
                              .build(),
                          Field.builder()
                              .name("order_delivered_on")
                              .type("TIMESTAMP")
                              .description(
                                  "An ISO-8601 timestamp representing the date/time the order was delivered.")
                              .build(),
                          Field.builder()
                              .name("order_delivered_time")
                              .type("TIMESTAMP")
                              .description(
                                  "An ISO-8601 timestamp representing the total time of delivery.")
                              .build(),
                          Field.builder()
                              .name("customer_email")
                              .type("VARCHAR")
                              .description("The email of the customer.")
                              .build(),
                          Field.builder()
                              .name("restaurant_id")
                              .type("INTEGER")
                              .description("The ID of the restaurant related to the order.")
                              .build(),
                          Field.builder()
                              .name("driver_id")
                              .type("INTEGER")
                              .description("The ID of the driver related to the order.")
                              .build()))
                  .description("A table for top deliveries.")
                  .build())
          .put(
              "public.popular_orders_day_of_week",
              DbTableMeta.builder()
                  .physicalName("public.popular_orders_day_of_week")
                  .sourceName(SOURCE_NAME)
                  .fields(
                      Lists.newArrayList(
                          Field.builder()
                              .name("order_day_of_week")
                              .type("VARCHAR")
                              .description("The day of week of the order.")
                              .build(),
                          Field.builder()
                              .name("order_placed_on")
                              .type("TIMESTAMP")
                              .description(
                                  "An ISO-8601 timestamp representing the date/time the order was placed.")
                              .build(),
                          Field.builder()
                              .name("orders_placed")
                              .type("INTEGER")
                              .description("The number of orders placed on day of week.")
                              .build()))
                  .description("A table for popular orders by day of week.")
                  .build())
          .build();

  static final ImmutableMap<String, JobMeta> JOB_META =
      new ImmutableMap.Builder<String, JobMeta>()
          .put(
              "test.job_with_no_inputs_or_outputs",
              JobMeta.builder()
                  .type(BATCH)
                  .description("An job with no inputs or outputs.")
                  .build())
          .put(
              "test.job_with_no_runs",
              JobMeta.builder().type(BATCH).description("An job with no runs.").build())
          .put(
              "example.etl_orders",
              JobMeta.builder()
                  .type(BATCH)
                  .outputs(NAMESPACE_NAME, "public.orders")
                  .location(
                      "https://github.com/example/jobs/blob/2294bc15eb49071f38425dc927e48655530a2f2e/etl_orders.py")
                  .description("Loads newly placed orders weekly.")
                  .context(
                      sql(
                          "INSERT INTO orders (id, placed_on, menu_item_id, quantity, discount_id, comment)\n  "
                              + "SELECT id, placed_on, menu_item_id, quantity, discount_id, comment\n    "
                              + "FROM tmp_orders;"))
                  .build())
          .put(
              "example.etl_menus",
              JobMeta.builder()
                  .type(BATCH)
                  .outputs(NAMESPACE_NAME, "public.menus")
                  .location(
                      "https://github.com/example/jobs/blob/2294bc15eb49071f38425dc927e48655530a2f2e/etl_menus.py")
                  .description("Loads newly added restaurant menus daily.")
                  .context(
                      sql(
                          "INSERT INTO menus (id, name, restaurant_id, description)\n  "
                              + "SELECT id, name, restaurant_id, description\n    "
                              + "FROM tmp_menus;"))
                  .build())
          .put(
              "example.etl_categories",
              JobMeta.builder()
                  .type(BATCH)
                  .outputs(NAMESPACE_NAME, "public.categories")
                  .location(
                      "https://github.com/example/jobs/blob/2294bc15eb49071f38425dc927e48655530a2f2e/etl_categories.py")
                  .description("Loads newly added menus categories daily.")
                  .context(
                      sql(
                          "INSERT INTO categories (id, name, menu_id, description)\n  "
                              + "SELECT id, name, menu_id, description\n    "
                              + "FROM tmp_categories;"))
                  .build())
          .put(
              "example.etl_menu_items",
              JobMeta.builder()
                  .type(BATCH)
                  .outputs(NAMESPACE_NAME, "public.menu_items")
                  .location(
                      "https://github.com/example/jobs/blob/2294bc15eb49071f38425dc927e48655530a2f2e/etl_menu_items.py")
                  .description("Loads newly added restaurant menu items daily.")
                  .context(
                      sql(
                          "INSERT INTO menu_items (id, name, price, category_id, description)\n  "
                              + "SELECT id, name, price, category_id, description\n    "
                              + "FROM tmp_menu_items;"))
                  .build())
          .put(
              "example.etl_restaurants",
              JobMeta.builder()
                  .type(BATCH)
                  .outputs(NAMESPACE_NAME, "public.restaurants")
                  .location(
                      "https://github.com/example/jobs/blob/2294bc15eb49071f38425dc927e48655530a2f2e/etl_restaurants.py")
                  .description("Loads newly registered restaurants daily.")
                  .context(
                      sql(
                          "INSERT INTO restaurants (id, created_at, updated_at, name, email, address, phone, city_id, business_hours_id, description)\n  "
                              + "SELECT id, created_at, updated_at, name, email, address, phone, city_id, business_hours_id, description\n    "
                              + "FROM tmp_restaurants;"))
                  .build())
          .put(
              "example.etl_customers",
              JobMeta.builder()
                  .type(BATCH)
                  .outputs(NAMESPACE_NAME, "public.customers")
                  .location(
                      "https://github.com/example/jobs/blob/2294bc15eb49071f38425dc927e48655530a2f2e/etl_customers.py")
                  .description("Loads newly registered customers daily.")
                  .context(
                      sql(
                          "INSERT INTO customers (id, created_at, updated_at, name, email, phone, city_id)\n  "
                              + "SELECT id, created_at, updated_at, name, email, phone, city_id\n    "
                              + "FROM tmp_customers;"))
                  .build())
          .put(
              "example.etl_order_status",
              JobMeta.builder()
                  .type(BATCH)
                  .outputs(NAMESPACE_NAME, "public.order_status")
                  .location(
                      "https://github.com/example/jobs/blob/2294bc15eb49071f38425dc927e48655530a2f2e/etl_order_status.py")
                  .description("Loads order statues updates daily.")
                  .context(
                      sql(
                          "INSERT INTO order_status (id, transitioned_at, status, order_id, customer_id, restaurant_id, driver_id)\n  "
                              + "SELECT id, transitioned_at, status, order_id, customer_id, restaurant_id, driver_id\n    "
                              + "FROM tmp_order_status;"))
                  .build())
          .put(
              "example.etl_drivers",
              JobMeta.builder()
                  .type(BATCH)
                  .outputs(NAMESPACE_NAME, "public.drivers")
                  .location(
                      "https://github.com/example/jobs/blob/2294bc15eb49071f38425dc927e48655530a2f2e/etl_drivers.py")
                  .description("Loads newly registered drivers daily.")
                  .context(
                      sql(
                          "INSERT INTO drivers (id, created_at, updated_at, name, email, phone, car_make, car_model, car_year, car_color, car_license_plate)\n  "
                              + "SELECT id, created_at, updated_at, name, email, phone, car_make, car_model, car_year, car_color, car_license_plate\n    "
                              + "FROM tmp_drivers;"))
                  .build())
          .put(
              "example.etl_orders_7_days",
              JobMeta.builder()
                  .type(BATCH)
                  .inputs(
                      NAMESPACE_NAME,
                      "public.menus",
                      "public.menu_items",
                      "public.orders",
                      "public.categories")
                  .outputs(NAMESPACE_NAME, "public.orders_7_days")
                  .location(
                      "https://github.com/example/jobs/blob/2294bc15eb49071f38425dc927e48655530a2f2e/etl_orders_7_days.py")
                  .description("Loads newly placed orders weekly.")
                  .context(
                      sql(
                          "INSERT INTO orders_7_days (order_id, placed_on, discount_id, menu_id, restaurant_id, menu_item_id, category_id)\n  "
                              + "SELECT o.id AS order_id, o.placed_on, o.discount_id, m.id AS menu_id, m.restaurant_id, mi.id AS menu_item_id, c.id AS category_id\n"
                              + "    FROM orders AS o\n"
                              + "   INNER JOIN menu_items AS mi\n"
                              + "      ON menu_items.id = o.menu_item_id\n"
                              + "   INNER JOIN categories AS c\n"
                              + "      ON c.id = mi.category_id\n"
                              + "   INNER JOIN menu AS m\n"
                              + "      ON m.id = c.menu_id\n"
                              + "   WHERE o.placed_on >= NOW() - interval '7 days';"))
                  .build())
          .put(
              "example.etl_delivery_7_days",
              JobMeta.builder()
                  .type(BATCH)
                  .inputs(
                      NAMESPACE_NAME,
                      "public.orders_7_days",
                      "public.customers",
                      "public.order_status",
                      "public.drivers",
                      "public.restaurants")
                  .outputs(NAMESPACE_NAME, "public.delivery_7_days")
                  .location(
                      "https://github.com/example/jobs/blob/4d0b5d374261fdaf60a1fc588dd8f0d124b0e87f/etl_delivery_7_days.py")
                  .description("Loads new deliveries for the week.")
                  .context(
                      sql(
                          "INSERT INTO delivery (order_id, order_placed_on, order_dispatched_on, order_delivered_on, customer_email,\n"
                              + "      customer_address, discount_id, menu_id, restaurant_id, restaurant_address, menu_item_id, category_id, driver_id)\n"
                              + "  SELECT o.order_id, o.placed_on AS order_placed_on,\n"
                              + "    (SELECT transitioned_at FROM order_status WHERE order_id == o.order_id AND status = 'DISPATCHED') AS order_dispatched_on,\n"
                              + "    (SELECT transitioned_at FROM order_status WHERE order_id == o.order_id AND status = 'DELIVERED') AS order_delivered_on,\n"
                              + "    c.email AS customer_email, c.address AS customer_address, o.discount_id, o.menu_id, o.restaurant_id,\n"
                              + "      r.address, o.menu_item_id, o.category_id, d.id AS driver_id\n"
                              + "    FROM orders_7_days AS o\n"
                              + "   INNER JOIN order_status AS os\n"
                              + "      ON os.order_id = o.order_id\n"
                              + "   INNER JOIN customers AS c\n"
                              + "      ON c.id = os.customer_id\n"
                              + "   INNER JOIN restaurants AS r\n"
                              + "      ON r.id = os.restaurant_id\n"
                              + "   INNER JOIN drivers AS d\n"
                              + "      ON d.id = os.driver_id\n"
                              + "   WHERE os.transitioned_at >= NOW() - interval '7 days';"))
                  .build())
          .put(
              "example.delivery_times_7_days",
              JobMeta.builder()
                  .type(BATCH)
                  .inputs(NAMESPACE_NAME, "public.delivery_7_days")
                  .outputs(NAMESPACE_NAME, "public.top_delivery_times", "public.discounts")
                  .location(
                      "https://github.com/example/jobs/blob/2294bc15eb49071f38425dc927e48655530a2f2e/delivery_times_7_days.py")
                  .description("Determine weekly top delivery times by restaurant.")
                  .context(
                      sql(
                          "INSERT INTO top_delivery_times (order_id, order_placed_on, order_dispatched_on, order_delivered_on, order_delivery_time,\n"
                              + "    customer_email, restaurant_id, driver_id)\n"
                              + "  SELECT order_id, order_placed_on, order_delivered_on, DATEDIFF(minute, order_placed_on, order_delivered_on) AS order_delivery_time,\n"
                              + "    customer_email, restaurant_id, driver_id\n"
                              + "    FROM delivery_7_days\n"
                              + "GROUP BY restaurant_id\n"
                              + "ORDER BY order_delivery_time DESC\n"
                              + "   LIMIT 1;"))
                  .build())
          .put(
              "example.email_discounts",
              JobMeta.builder()
                  .type(BATCH)
                  .inputs(NAMESPACE_NAME, "public.customers", "public.discounts")
                  .location(
                      "https://github.com/example/jobs/blob/2294bc15eb49071f38425dc927e48655530a2f2e/email_discounts.py")
                  .description("Email discounts to customers that have experienced order delays.")
                  .context(sql("SELECT * FROM discounts;"))
                  .build())
          .put(
              "example.orders_popular_day_of_week",
              JobMeta.builder()
                  .type(BATCH)
                  .inputs(NAMESPACE_NAME, "public.customers", "public.top_delivery_times")
                  .outputs(NAMESPACE_NAME, "public.popular_orders_day_of_week")
                  .location(
                      "https://github.com/example/jobs/blob/2294bc15eb49071f38425dc927e48655530a2f2e/orders_popular_day_of_week.py")
                  .description("Determines the popular day of week orders are placed.")
                  .context(
                      sql(
                          "INSERT INTO popular_orders_day_of_week (order_day_of_week, order_placed_on, orders_placed)\n"
                              + "  SELECT order_day_of_week, order_placed_on, COUNT(*)\n"
                              + "    FROM top_delivery_times;"))
                  .build())
          .build();

  private static Map<String, String> sql(final String sql) {
    return ImmutableMap.of("sql", sql);
  }

  @EqualsAndHashCode
  @ToString
  static final class ActiveRunMeta {
    @Getter private final int levelInGraph;
    @Getter private final boolean markFailed;
    @Getter private final boolean markRunning;
    @Nullable private final CodeChange codeChange;
    @Getter private final ImmutableSet<SchemaChange> schemaChanges;

    public ActiveRunMeta(
        final int levelInGraph,
        final boolean markFailed,
        final boolean markRunning,
        @Nullable final CodeChange codeChange,
        @NonNull final ImmutableSet<SchemaChange> schemaChanges) {
      this.levelInGraph = levelInGraph;
      this.markFailed = markFailed;
      this.markRunning = markRunning;
      this.codeChange = codeChange;
      this.schemaChanges = schemaChanges;
    }

    public Optional<CodeChange> getCodeChange() {
      return Optional.ofNullable(codeChange);
    }

    public boolean hasChanges() {
      return !hasCodeChange() && !hasSchemaChange();
    }

    public boolean hasCodeChange() {
      return (codeChange != null);
    }

    public boolean hasSchemaChange() {
      return !schemaChanges.isEmpty();
    }

    public Optional<SchemaChange> schemaChangeFor(String datasetName) {
      checkArgument(hasSchemaChange());
      for (SchemaChange schemaChange : schemaChanges) {
        if (schemaChange.getDatasetName().equals(checkNotBlank(datasetName))) {
          return Optional.of(schemaChange);
        }
      }
      return Optional.empty();
    }

    interface Change {}

    @EqualsAndHashCode
    @ToString
    static class CodeChange implements Change {
      @Getter String jobName;
      @Nullable URL fromUrl;
      @Getter URL toUrl;

      public CodeChange(
          @NonNull final String jobName, @Nullable final URL fromUrl, @NonNull final URL toUrl) {
        this.jobName = jobName;
        this.fromUrl = fromUrl;
        this.toUrl = toUrl;
      }

      public Optional<URL> getFromUrl() {
        return Optional.ofNullable(fromUrl);
      }

      public static Builder builder() {
        return new Builder();
      }

      static final class Builder {
        private String jobName;
        private URL fromUrl;
        private URL toUrl;

        public Builder jobName(@NonNull String jobName) {
          this.jobName = jobName;
          return this;
        }

        public Builder fromUrl(@NonNull String fromUrlString) {
          return fromUrl(Utils.toUrl(fromUrlString));
        }

        public Builder fromUrl(@NonNull URL fromUrl) {
          this.fromUrl = fromUrl;
          return this;
        }

        public Builder toUrl(@NonNull String toUrl) {
          return toUrl(Utils.toUrl(toUrl));
        }

        public Builder toUrl(@NonNull URL toUrl) {
          this.toUrl = toUrl;
          return this;
        }

        public CodeChange build() {
          return new CodeChange(jobName, fromUrl, toUrl);
        }
      }
    }

    @EqualsAndHashCode
    @ToString
    static class SchemaChange implements Change {
      @Getter private String datasetName;
      @Getter private String fieldName;
      @Getter private String fromType;
      @Getter private String toType;

      public SchemaChange(
          @NonNull final String datasetName,
          @NonNull final String fieldName,
          @NonNull final String fromType,
          @NonNull final String toType) {
        this.datasetName = datasetName;
        this.fieldName = fieldName;
        this.fromType = fromType;
        this.toType = toType;
      }

      public static Builder builder() {
        return new Builder();
      }

      static final class Builder {
        private String datasetName;
        private String fieldName;
        private String fromType;
        private String toType;

        public Builder datasetName(@NonNull String datasetName) {
          this.datasetName = datasetName;
          return this;
        }

        public Builder fieldName(@NonNull String fieldName) {
          this.fieldName = fieldName;
          return this;
        }

        public Builder fromType(@NonNull String fromType) {
          this.fromType = fromType;
          return this;
        }

        public Builder toType(@NonNull String toType) {
          this.toType = toType;
          return this;
        }

        public SchemaChange build() {
          return new SchemaChange(datasetName, fieldName, fromType, toType);
        }
      }
    }

    public static ImmutableList<ActiveRunMeta> successes(
        final int levelInLineageGraph, final int numOfSuccesses) {
      final ImmutableList.Builder<ActiveRunMeta> activeRuns = ImmutableList.builder();
      for (int i = 0; i < numOfSuccesses; i++) {
        activeRuns.add(ActiveRunMeta.builder().levelInLineageGraph(levelInLineageGraph).build());
      }
      return activeRuns.build();
    }

    public static ImmutableList<ActiveRunMeta> failures(
        final int levelInLineageGraph, final int numOfFailures) {
      final ImmutableList.Builder<ActiveRunMeta> activeRuns = ImmutableList.builder();
      for (int i = 0; i < numOfFailures; i++) {
        activeRuns.add(
            ActiveRunMeta.builder().levelInLineageGraph(levelInLineageGraph).markFailed().build());
      }
      return activeRuns.build();
    }

    public static ImmutableList<ActiveRunMeta> running(
        final int levelInLineageGraph, final int numOfRunning) {
      final ImmutableList.Builder<ActiveRunMeta> activeRuns = ImmutableList.builder();
      for (int i = 0; i < numOfRunning; i++) {
        activeRuns.add(
            ActiveRunMeta.builder().levelInLineageGraph(levelInLineageGraph).markRunning().build());
      }
      return activeRuns.build();
    }

    public static ImmutableList<ActiveRunMeta> randomize(
        final int levelInLineageGraph, final int numOfRandom) {
      final ImmutableList.Builder<ActiveRunMeta> activeRuns = ImmutableList.builder();
      for (int i = 0; i < numOfRandom; i++) {
        if (new Random().nextBoolean()) {
          activeRuns.add(successes(levelInLineageGraph, 1).get(0));
        } else {
          activeRuns.add(failures(levelInLineageGraph, 1).get(0));
        }
      }
      return activeRuns.build();
    }

    public static ActiveRunMeta successesWith(
        int levelInLineageGraph, @Nullable final SchemaChange... schemaChanges) {
      return successesWith(levelInLineageGraph, null, schemaChanges);
    }

    public static ActiveRunMeta successesWith(
        final int levelInLineageGraph,
        @Nullable final CodeChange codeChange,
        @Nullable final SchemaChange... schemaChanges) {
      return ActiveRunMeta.builder()
          .levelInLineageGraph(levelInLineageGraph)
          .codeChange(codeChange)
          .schemaChanges(ImmutableSet.copyOf(schemaChanges))
          .build();
    }

    public static Builder builder() {
      return new Builder();
    }

    static final class Builder {
      private int levelInLineageGraph;
      private boolean markFailed;
      private boolean markRunning;
      private CodeChange codeChange;
      private ImmutableSet<SchemaChange> schemaChanges;

      private Builder() {
        this.markFailed = false;
        this.markRunning = true;
        this.schemaChanges = ImmutableSet.of();
      }

      public Builder levelInLineageGraph(int levelInLineageGraph) {
        this.levelInLineageGraph = levelInLineageGraph;
        return this;
      }

      public Builder markFailed() {
        this.markFailed = true;
        return this;
      }

      public Builder markRunning() {
        this.markRunning = true;
        return this;
      }

      public Builder codeChange(@NonNull CodeChange codeChange) {
        this.codeChange = codeChange;
        return this;
      }

      public Builder schemaChanges(@NonNull ImmutableSet<SchemaChange> schemaChanges) {
        this.schemaChanges = schemaChanges;
        return this;
      }

      public ActiveRunMeta build() {
        return new ActiveRunMeta(
            levelInLineageGraph, markFailed, markRunning, codeChange, schemaChanges);
      }
    }
  }

  static final LinkedHashMap<String, ImmutableList<ActiveRunMeta>> ACTIVE_RUN_META =
      Maps.newLinkedHashMap(
          new ImmutableMap.Builder<String, ImmutableList<ActiveRunMeta>>()
              .put("test.job_with_no_inputs_or_outputs", ActiveRunMeta.failures(0, 2))
              .put("test.job_with_no_runs", ActiveRunMeta.successes(0, 0))
              .put(
                  "example.etl_categories",
                  ActiveRunMeta.successes(0, LINEAGE_GRAPH_24_HOUR_WINDOW))
              .put(
                  "example.etl_menu_items",
                  ActiveRunMeta.successes(0, LINEAGE_GRAPH_24_HOUR_WINDOW))
              .put("example.etl_menus", ActiveRunMeta.successes(0, LINEAGE_GRAPH_24_HOUR_WINDOW))
              .put(
                  "example.etl_orders",
                  ImmutableList.copyOf(
                      Iterables.concat(
                          ActiveRunMeta.running(0, 1),
                          ActiveRunMeta.randomize(0, LINEAGE_GRAPH_24_HOUR_WINDOW - 1))))
              .put(
                  "example.etl_customers", ActiveRunMeta.successes(1, LINEAGE_GRAPH_24_HOUR_WINDOW))
              .put("example.etl_drivers", ActiveRunMeta.successes(1, LINEAGE_GRAPH_24_HOUR_WINDOW))
              .put(
                  "example.etl_order_status",
                  ActiveRunMeta.successes(1, LINEAGE_GRAPH_24_HOUR_WINDOW))
              .put(
                  "example.etl_orders_7_days",
                  ImmutableList.copyOf(
                      Iterables.concat(
                          ActiveRunMeta.running(1, 1),
                          ActiveRunMeta.successes(1, 1),
                          ActiveRunMeta.randomize(1, LINEAGE_GRAPH_24_HOUR_WINDOW - 2))))
              .put(
                  "example.etl_restaurants",
                  ActiveRunMeta.successes(1, LINEAGE_GRAPH_24_HOUR_WINDOW))
              .put(
                  "example.etl_delivery_7_days",
                  ImmutableList.copyOf(
                      Iterables.concat(
                          ImmutableList.of(
                              ActiveRunMeta.successesWith(
                                  2,
                                  ActiveRunMeta.CodeChange.builder()
                                      .jobName("example.etl_delivery_7_days")
                                      .fromUrl(
                                          JOB_META
                                              .get("example.etl_delivery_7_days")
                                              .getLocation()
                                              .orElse(null))
                                      .toUrl(
                                          "https://github.com/example/jobs/blob/c87f2a40553cfa4ae7178083a068bf1d0c6ca3a8/etl_delivery_7_days.py")
                                      .build(),
                                  ActiveRunMeta.SchemaChange.builder()
                                      .datasetName("public.delivery_7_days")
                                      .fieldName("discount_id")
                                      .fromType("INTEGER")
                                      .toType("VARCHAR")
                                      .build())),
                          ActiveRunMeta.successes(2, LINEAGE_GRAPH_24_HOUR_WINDOW - 1))))
              .put(
                  "example.delivery_times_7_days",
                  ImmutableList.copyOf(
                      Iterables.concat(
                          ActiveRunMeta.failures(3, 1),
                          ActiveRunMeta.successes(3, LINEAGE_GRAPH_24_HOUR_WINDOW - 2),
                          ActiveRunMeta.failures(3, 1))))
              .put(
                  "example.email_discounts",
                  ActiveRunMeta.successes(4, LINEAGE_GRAPH_24_HOUR_WINDOW))
              .put(
                  "example.orders_popular_day_of_week",
                  ActiveRunMeta.successes(4, LINEAGE_GRAPH_24_HOUR_WINDOW))
              .build());
}
