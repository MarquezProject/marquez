/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.logging;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jdbi3.strategies.StatementNameStrategy;
import org.jdbi.v3.core.extension.ExtensionMethod;
import org.jdbi.v3.core.statement.StatementContext;
import org.slf4j.MDC;

public class EndpointNameStrategy implements StatementNameStrategy {

  @Override
  public String getStatementName(StatementContext statementContext) {
    ExtensionMethod extensionMethod = statementContext.getExtensionMethod();
    if (extensionMethod != null) {
      if (MDC.get("method") != null && MDC.get("pathWithParams") != null) {
        StringBuilder builder =
            new StringBuilder()
                .append(extensionMethod.getMethod().getName())
                .append(".")
                .append(MDC.get("method"))
                .append(".")
                .append(MDC.get("pathWithParams"));
        return MetricRegistry.name(extensionMethod.getType(), builder.toString());
      }
    }
    return null;
  }
}
