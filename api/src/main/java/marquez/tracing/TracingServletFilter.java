/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.tracing;

import io.sentry.ITransaction;
import io.sentry.Sentry;
import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

public class TracingServletFilter implements Filter {

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    ITransaction transaction = transaction(request, response);
    Sentry.configureScope(
        scope -> {
          scope.setTransaction(transaction);
        });
    try {
      chain.doFilter(request, response);
    } finally {
      transaction.finish();
    }
  }

  private ITransaction transaction(ServletRequest request, ServletResponse response) {
    String transactionName = request.getProtocol();
    String taskName = request.getProtocol();
    String description = "";
    if (request instanceof HttpServletRequest) {
      HttpServletRequest httpRequest = (HttpServletRequest) request;
      transactionName += " " + httpRequest.getMethod();
      taskName = httpRequest.getMethod() + " " + httpRequest.getPathInfo();
      description =
          (httpRequest.getQueryString() == null ? "" : httpRequest.getQueryString() + "\n")
              + "Input size: "
              + request.getContentLengthLong();
    }
    return Sentry.startTransaction(transactionName, taskName, description);
  }
}
