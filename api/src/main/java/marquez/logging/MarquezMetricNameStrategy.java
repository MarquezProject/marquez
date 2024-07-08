/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.logging;

import com.codahale.metrics.jdbi3.strategies.DefaultNameStrategy;
import com.codahale.metrics.jdbi3.strategies.DelegatingStatementNameStrategy;

public class MarquezMetricNameStrategy extends DelegatingStatementNameStrategy {

  public MarquezMetricNameStrategy() {
    super(
        DefaultNameStrategy.CHECK_EMPTY,
        new EndpointNameStrategy(),
        DefaultNameStrategy.SQL_OBJECT,
        DefaultNameStrategy.CONSTANT_SQL_RAW);
  }
}
