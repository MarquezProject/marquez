/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.ImmutableMap;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;
import java.util.Map;
import java.util.UUID;
import marquez.common.Utils;
import marquez.db.JobFacetsDao;
import marquez.db.JobVersionDao;
import marquez.db.RunFacetsDao;
import marquez.service.RunService;
import marquez.service.ServiceFactory;
import marquez.service.models.JobFacets;
import marquez.service.models.RunFacets;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(DropwizardExtensionsSupport.class)
class JobResourceTest {
  private static ResourceExtension UNDER_TEST;
  private static JobFacets JOB_FACETS;
  private static RunFacets RUN_FACETS;
  private static UUID RUN_UUID = UUID.fromString("a32f2800-7782-3ce3-b77e-eeeeaded3cf3");

  static {
    RunService runService = mock(RunService.class);
    JobVersionDao jobVersionDao = mock(JobVersionDao.class);
    JobFacetsDao jobFacetsDao = mock(JobFacetsDao.class);
    RunFacetsDao runFacetsDao = mock(RunFacetsDao.class);

    ImmutableMap<String, Object> jobFacets =
        Utils.fromJson(
            JobResourceTest.class.getResourceAsStream("/facets/job_facets.json"),
            new TypeReference<>() {});
    ImmutableMap<String, Object> runFacets =
        Utils.fromJson(
            JobResourceTest.class.getResourceAsStream("/facets/run_facets.json"),
            new TypeReference<>() {});
    JOB_FACETS = new JobFacets(RUN_UUID, jobFacets);
    RUN_FACETS = new RunFacets(RUN_UUID, runFacets);
    when(runService.exists(RUN_UUID)).thenReturn(true);
    when(jobFacetsDao.findJobFacetsByRunUuid(any(UUID.class))).thenReturn(JOB_FACETS);
    when(runFacetsDao.findRunFacetsByRunUuid(any(UUID.class))).thenReturn(RUN_FACETS);

    ServiceFactory serviceFactory =
        ApiTestUtils.mockServiceFactory(Map.of(RunService.class, runService));

    UNDER_TEST =
        ResourceExtension.builder()
            .addResource(new JobResource(serviceFactory, jobVersionDao, jobFacetsDao, runFacetsDao))
            .build();
  }

  @Test
  public void testGetJobFacetsByRunUUid() {
    assertThat(
            UNDER_TEST
                .target("/api/v1/jobs/321/runs")
                .queryParam("type", "job")
                .request()
                .get()
                .getStatus())
        .isEqualTo(404);
    final JobFacets testJobFacets =
        UNDER_TEST
            .target("/api/v1/jobs/runs/a32f2800-7782-3ce3-b77e-eeeeaded3cf3/facets")
            .queryParam("type", "job")
            .request()
            .get()
            .readEntity(JobFacets.class);

    assertEquals(testJobFacets, JOB_FACETS);
  }

  @Test
  public void testGetRunFacetsByRunUUid() {
    assertThat(
            UNDER_TEST
                .target("/api/v1/jobs/321/runs")
                .queryParam("type", "run")
                .request()
                .get()
                .getStatus())
        .isEqualTo(404);
    final RunFacets testRunFacets =
        UNDER_TEST
            .target("/api/v1/jobs/runs/a32f2800-7782-3ce3-b77e-eeeeaded3cf3/facets")
            .queryParam("type", "run")
            .request()
            .get()
            .readEntity(RunFacets.class);

    assertEquals(testRunFacets, RUN_FACETS);
  }
}
