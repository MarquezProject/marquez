/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.logging;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import javax.ws.rs.container.ContainerRequestContext;
import org.glassfish.jersey.server.ExtendedUriInfo;
import org.glassfish.jersey.uri.UriTemplate;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.MDC;

public class LoggingMdcFilterTest {

  @Mock private ContainerRequestContext requestContext;

  @Mock private ExtendedUriInfo uriInfo;

  private LoggingMdcFilter filter;

  @Before
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    filter = new LoggingMdcFilter();
    Mockito.when(requestContext.getUriInfo()).thenReturn(uriInfo);
  }

  @Test
  public void testPathWithPlaceholders()
      throws IOException,
          NoSuchFieldException,
          SecurityException,
          IllegalArgumentException,
          IllegalAccessException {
    String expectedPath = "/api/users/{userId}";
    List<UriTemplate> templates = Collections.singletonList(new UriTemplate(expectedPath));

    Field uriInfoField = LoggingMdcFilter.class.getDeclaredField("uriInfo");
    uriInfoField.setAccessible(true);
    uriInfoField.set(filter, uriInfo);

    Mockito.when(requestContext.getUriInfo()).thenReturn(uriInfo);
    Mockito.when(uriInfo.getMatchedTemplates()).thenReturn(templates);

    filter.filter(requestContext);

    assertEquals(expectedPath, MDC.get("pathWithParams"));
    MDC.clear(); // Clean up MDC
  }
}
