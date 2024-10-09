/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.logging;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.sql.SQLException;
import org.jdbi.v3.core.statement.SqlLogger;
import org.jdbi.v3.core.statement.StatementContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DelegatingSqlLoggerTest {
  private SqlLogger logger1;
  private SqlLogger logger2;
  private DelegatingSqlLogger delegatingSqlLogger;
  private StatementContext statementContext;
  private SQLException sqlException;

  @BeforeEach
  public void setUp() {
    logger1 = mock(SqlLogger.class);
    logger2 = mock(SqlLogger.class);
    delegatingSqlLogger = new DelegatingSqlLogger(logger1, logger2);
    statementContext = mock(StatementContext.class);
    sqlException = new SQLException("Test SQL Exception");
  }

  @Test
  public void testLogAfterExecution() {
    // Act
    delegatingSqlLogger.logAfterExecution(statementContext);

    // Assert
    verify(logger1, times(1)).logAfterExecution(statementContext);
    verify(logger2, times(1)).logAfterExecution(statementContext);
  }

  @Test
  public void testLogException() {
    // Act
    delegatingSqlLogger.logException(statementContext, sqlException);

    // Assert
    verify(logger1, times(1)).logException(statementContext, sqlException);
    verify(logger2, times(1)).logException(statementContext, sqlException);
  }
}
