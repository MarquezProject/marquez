/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.graphql;

import graphql.kickstart.execution.GraphQLQueryInvoker;
import graphql.kickstart.servlet.GraphQLConfiguration;
import graphql.kickstart.servlet.GraphQLHttpServlet;
import graphql.schema.GraphQLSchema;

public class MarquezGraphqlServletBuilder {
  public GraphQLHttpServlet getServlet(final GraphqlSchemaBuilder schemaBuilder) {
    final GraphQLSchema schema = schemaBuilder.buildSchema();

    final GraphQLQueryInvoker queryInvoker = GraphQLQueryInvoker.newBuilder().build();

    final GraphQLConfiguration config =
        GraphQLConfiguration.with(schema).with(queryInvoker).build();

    return GraphQLHttpServlet.with(config);
  }
}
