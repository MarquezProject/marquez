/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.api.filter;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import lombok.extern.slf4j.Slf4j;
import marquez.common.models.JobId;
import marquez.db.models.JobRow;
import marquez.service.JobService;
import marquez.service.models.Job;
import org.glassfish.jersey.server.ExtendedUriInfo;
import org.glassfish.jersey.uri.UriComponent;
import org.glassfish.jersey.uri.UriComponent.Type;
import org.glassfish.jersey.uri.UriTemplate;

/**
 * Filters requests that reference a job that has been symlinked to another job. This filter
 * redirects such requests to the URL with the symlink target's name using a 301 status code.
 */
@Slf4j
public class JobRedirectFilter implements ContainerRequestFilter {

  public static final String JOB_PATH_PARAM = "job";
  public static final String NAMESPACE_PATH_PARAM = "namespace";
  private final JobService jobService;

  public JobRedirectFilter(JobService jobService) {
    this.jobService = jobService;
  }

  @Override
  public void filter(ContainerRequestContext requestContext) throws IOException {
    MultivaluedMap<String, String> pathParams = requestContext.getUriInfo().getPathParameters();
    if (!pathParams.containsKey(NAMESPACE_PATH_PARAM) || !pathParams.containsKey(JOB_PATH_PARAM)) {
      return;
    }
    List<String> namespaceParams = pathParams.get(NAMESPACE_PATH_PARAM);
    List<String> jobParams = pathParams.get(JOB_PATH_PARAM);
    if (namespaceParams.isEmpty() || jobParams.isEmpty()) {
      return;
    }
    Optional<Job> job = jobService.findJobByName(namespaceParams.get(0), jobParams.get(0));
    job.ifPresent(
        j -> {
          if (!j.getName().getValue().equals(jobParams.get(0))) {
            log.info(
                "Job {}.{} has been redirected to {}.{}",
                namespaceParams.get(0),
                jobParams.get(0),
                j.getNamespace().getValue(),
                j.getName().getValue());
            URI location = buildLocationFor(requestContext, j.getId());
            log.debug("Redirecting to url {}", location);
            requestContext.abortWith(Response.status(301).location(location).build());
          }
        });
  }

  /**
   * Construct a URI from a Request's matched resource, replacing the {@value #JOB_PATH_PARAM} and
   * {@value #NAMESPACE_PATH_PARAM} parameters with the fully-qualified values from the provided
   * {@link JobRow}.
   *
   * @param ctx
   * @param jobId
   * @return
   */
  private URI buildLocationFor(ContainerRequestContext ctx, JobId jobId) {
    Object resource = ctx.getUriInfo().getMatchedResources().get(0);
    MultivaluedMap<String, String> pathParameters = ctx.getUriInfo().getPathParameters();
    MultivaluedHashMap<String, String> copy = new MultivaluedHashMap<>(pathParameters);
    copy.putSingle(
        JOB_PATH_PARAM, UriComponent.encode(jobId.getName().getValue(), Type.PATH_SEGMENT));
    copy.putSingle(
        NAMESPACE_PATH_PARAM,
        UriComponent.encode(jobId.getNamespace().getValue(), Type.PATH_SEGMENT));
    Map<String, String> singletonMap = new HashMap<>();
    copy.forEach((k, v) -> singletonMap.put(k, v.get(0)));
    UriTemplate pathTemplate = ((ExtendedUriInfo) ctx.getUriInfo()).getMatchedTemplates().get(0);
    String newPath = pathTemplate.createURI(singletonMap);
    return UriBuilder.fromResource(resource.getClass()).path(newPath).buildFromEncodedMap(copy);
  }
}
