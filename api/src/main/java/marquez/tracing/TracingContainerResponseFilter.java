/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.tracing;

import io.sentry.Sentry;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.List;
import org.glassfish.jersey.server.ExtendedUriInfo;
import org.glassfish.jersey.uri.UriTemplate;

@Provider
public class TracingContainerResponseFilter implements ContainerResponseFilter {
  @Context UriInfo uriInfo;
  @Context ExtendedUriInfo extendedUriInfo;

  @Override
  public void filter(
      ContainerRequestContext requestContext, ContainerResponseContext responseContext)
      throws IOException {

    List<UriTemplate> matchedTemplates = extendedUriInfo.getMatchedTemplates();
    String path = "";
    for (UriTemplate uriTemplate : matchedTemplates) {
      path = uriTemplate.toString() + path;
    }
    Sentry.setTransaction(requestContext.getMethod() + " " + path);
  }
}
