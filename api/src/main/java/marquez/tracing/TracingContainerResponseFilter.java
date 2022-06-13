/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.tracing;

import io.sentry.Sentry;
import java.io.IOException;
import java.util.List;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;
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
