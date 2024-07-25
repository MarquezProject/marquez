/*
 * Copyright 2018-2024 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.logging;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.time.temporal.ChronoUnit;
import marquez.service.DatabaseMetrics;
import org.jdbi.v3.core.extension.ExtensionMethod;
import org.jdbi.v3.core.statement.StatementContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.slf4j.MDC;

public class LabelledSqlLoggerTest {

  private LabelledSqlLogger logger;
  private StatementContext context;

  @BeforeEach
  public void setUp() {
    logger = new LabelledSqlLogger();
    context = mock(StatementContext.class);

    // Setup default mock behavior
    when(context.getElapsedTime(ChronoUnit.NANOS))
        .thenReturn(1000000000L); // 1 second in nanoseconds
  }

  @AfterEach
  public void tearDown() {
    MDC.clear();
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  @Test
  public void testLogAfterExecution() {
    // Prepare MDC context
    MDC.put("method", "GET");
    MDC.put("pathWithParams", "/test/path");

    ExtensionMethod extensionMethod = mock(ExtensionMethod.class);
    when(extensionMethod.getType()).thenReturn((Class) TestClass.class);
    when(extensionMethod.getMethod()).thenReturn(TestClass.class.getMethods()[0]);

    when(context.getExtensionMethod()).thenReturn(extensionMethod);

    // Mock static method
    try (MockedStatic<DatabaseMetrics> mockedDatabaseMetrics =
        Mockito.mockStatic(DatabaseMetrics.class)) {
      // Call the method under test
      logger.logAfterExecution(context);

      // Verify the static method call
      mockedDatabaseMetrics.verify(
          () ->
              DatabaseMetrics.recordDbDuration(
                  "marquez.logging.LabelledSqlLoggerTest$TestClass",
                  "testMethod",
                  "GET",
                  "/test/path",
                  1.0),
          times(1));
    }
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  @Test
  public void testLogException() {
    // Prepare MDC context
    MDC.put("method", "POST");
    MDC.put("pathWithParams", "/test/exception");

    ExtensionMethod extensionMethod = mock(ExtensionMethod.class);
    when(extensionMethod.getType()).thenReturn((Class) TestClass.class);
    when(extensionMethod.getMethod()).thenReturn(TestClass.class.getMethods()[0]);

    when(context.getExtensionMethod()).thenReturn(extensionMethod);

    // Mock static method
    try (MockedStatic<DatabaseMetrics> mockedSqlMetrics =
        Mockito.mockStatic(DatabaseMetrics.class)) {
      // Call the method under test
      logger.logException(context, new SQLException("Test Exception"));

      // Verify the static method call
      mockedSqlMetrics.verify(
          () ->
              DatabaseMetrics.recordDbDuration(
                  "marquez.logging.LabelledSqlLoggerTest$TestClass",
                  "testMethod",
                  "POST",
                  "/test/exception",
                  1.0));
    }
  }

  // Dummy class for the purpose of mocking
  private static class TestClass {
    public void testMethod() {}
  }
}
