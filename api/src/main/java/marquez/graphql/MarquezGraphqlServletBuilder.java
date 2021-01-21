package marquez.graphql;

import static graphql.schema.idl.TypeRuntimeWiring.newTypeWiring;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.smoketurner.dropwizard.graphql.CachingPreparsedDocumentProvider;
import com.smoketurner.dropwizard.graphql.GraphQLFactory;
import graphql.execution.preparsed.PreparsedDocumentProvider;
import graphql.kickstart.execution.GraphQLQueryInvoker;
import graphql.kickstart.servlet.GraphQLHttpServlet;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import java.net.URL;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.UUID;
import lombok.SneakyThrows;
import org.jdbi.v3.core.Jdbi;

public class MarquezGraphqlServletBuilder {

  public GraphQLHttpServlet getServlet(final Jdbi jdbi) {
    final GraphQLFactory factory = getGraphQLFactory(jdbi);

    final PreparsedDocumentProvider provider =
        new CachingPreparsedDocumentProvider(factory.getQueryCache(), new MetricRegistry());

    final GraphQLSchema schema = factory.build();

    final GraphQLQueryInvoker queryInvoker =
        GraphQLQueryInvoker.newBuilder()
            .withPreparsedDocumentProvider(provider)
            .withInstrumentation(factory.getInstrumentations())
            .build();

    final graphql.kickstart.servlet.GraphQLConfiguration config =
        graphql.kickstart.servlet.GraphQLConfiguration.with(schema).with(queryInvoker).build();

    return GraphQLHttpServlet.with(config);
  }

  @SneakyThrows
  public GraphQLFactory getGraphQLFactory(Jdbi jdbi) {
    GraphQLDataFetchers dataFetchers = new GraphQLDataFetchers(jdbi);

    final GraphQLFactory factory = new GraphQLFactory();
    // the RuntimeWiring must be configured prior to the run()
    // methods being called so the schema is connected properly.
    URL url = Resources.getResource("schema.graphqls");
    String sdl = Resources.toString(url, Charsets.UTF_8);
    RuntimeWiring wiring =
        RuntimeWiring.newRuntimeWiring()
            .type(
                newTypeWiring("Query")
                    .dataFetcher("datasets", dataFetchers.getDatasets())
                    .dataFetcher("namespace", dataFetchers.getNamespaceByName()))
            .type(
                newTypeWiring("Dataset")
                    .dataFetcher("source", dataFetchers.getSourcesByDataset())
                    .dataFetcher("namespace", dataFetchers.getNamespaceByDataset())
                    .dataFetcher("currentVersion", dataFetchers.getCurrentVersionByDataset())
                    .dataFetcher("fields", dataFetchers.getFieldsByDataset())
                    .dataFetcher("jobVersionAsInput", dataFetchers.getJobVersionAsInputByDataset())
                    .dataFetcher("jobVersionAsOutput", dataFetchers.getVersionAsOutputByDataset())
                    .dataFetcher("tags", dataFetchers.getTagsByDataset())
                    .dataFetcher("versions", dataFetchers.getVersionsByDataset()))
            .type(
                newTypeWiring("Tag")
                    .dataFetcher("fields", dataFetchers.getDatasetFieldsByTag())
                    .dataFetcher("datasets", dataFetchers.getDatasetsByTag()))
            .type(
                newTypeWiring("Source").dataFetcher("datasets", dataFetchers.getDatasetsBySource()))
            .type(
                newTypeWiring("RunStateRecord")
                    .dataFetcher("run", dataFetchers.getRunByRunStateRecord()))
            .type(
                newTypeWiring("RunArgs")
                    .dataFetcher("run", dataFetchers.getRunsByRunArgs())
                    .dataFetcher("args", dataFetchers.convertRunArgs()))
            .type(
                newTypeWiring("Run")
                    .dataFetcher("jobVersion", dataFetchers.getJobVersionByRun())
                    .dataFetcher("runArgs", dataFetchers.getRunArgsByRun())
                    .dataFetcher("states", dataFetchers.getRunStatesByRun())
                    .dataFetcher("startState", dataFetchers.getStartStateByRun())
                    .dataFetcher("endState", dataFetchers.getEndStateByRun())
                    .dataFetcher("inputs", dataFetchers.getInputsByRun())
                    .dataFetcher("outputs", dataFetchers.getOutputsByRun()))
            .type(
                newTypeWiring("Owner")
                    .dataFetcher("namespaces", dataFetchers.getNamespacesByOwner()))
            .type(
                newTypeWiring("Namespace")
                    .dataFetcher("owners", dataFetchers.getOwnersByNamespace())
                    .dataFetcher("currentOwner", dataFetchers.getCurrentOwnerByNamespace())
                    .dataFetcher("jobs", dataFetchers.getJobsByNamespace())
                    .dataFetcher("datasets", dataFetchers.getDatasetsByNamespace()))
            .type(
                newTypeWiring("JobVersion")
                    .dataFetcher("jobContext", dataFetchers.getJobContextByJobVersion())
                    .dataFetcher("latestRun", dataFetchers.getLatestRunByJobVersion())
                    .dataFetcher("job", dataFetchers.getJobByJobVersion())
                    .dataFetcher("inputs", dataFetchers.getInputsByJobVersion())
                    .dataFetcher("outputs", dataFetchers.getOutputsByJobVersion()))
            .type(
                newTypeWiring("JobContext")
                    .dataFetcher("jobVersion", dataFetchers.getJobVersionsByJobContext()))
            .type(
                newTypeWiring("Job")
                    .dataFetcher("versions", dataFetchers.getVersionsByJob())
                    .dataFetcher("namespace", dataFetchers.getNamespaceByJob())
                    .dataFetcher("currentVersion", dataFetchers.getCurrentVersionByJob()))
            .type(
                newTypeWiring("DatasetVersion")
                    .dataFetcher("fields", dataFetchers.getFieldsByDatasetVersion())
                    .dataFetcher("run", dataFetchers.getRunByDatasetVersion())
                    .dataFetcher("dataset", dataFetchers.getDatasetByDatasetVersion()))
            .type(
                newTypeWiring("DatasetField")
                    .dataFetcher("dataset", dataFetchers.getDatasetByDatasetField())
                    .dataFetcher("versions", dataFetchers.getVersionsByDatasetField())
                    .dataFetcher("tags", dataFetchers.getTagsByDatasetField()))
            .scalar(
                GraphQLScalarType.newScalar()
                    .name("UUID")
                    .coercing(
                        new Coercing<UUID, String>() {

                          @Override
                          public String serialize(Object dataFetcherResult)
                              throws CoercingSerializeException {
                            return dataFetcherResult.toString();
                          }

                          @Override
                          public UUID parseValue(Object input) throws CoercingParseValueException {
                            return UUID.fromString(input.toString());
                          }

                          @Override
                          public UUID parseLiteral(Object input)
                              throws CoercingParseLiteralException {
                            return UUID.fromString(input.toString());
                          }
                        })
                    .build())
            .scalar(
                GraphQLScalarType.newScalar()
                    .name("Json")
                    .coercing(
                        new Coercing<String, Map>() {
                          ObjectMapper mapper = new ObjectMapper();

                          @Override
                          @SneakyThrows
                          public Map serialize(Object dataFetcherResult)
                              throws CoercingSerializeException {
                            return (Map) dataFetcherResult;
                          }

                          @Override
                          @SneakyThrows
                          public String parseValue(Object input)
                              throws CoercingParseValueException {
                            return mapper.writeValueAsString(input);
                          }

                          @Override
                          @SneakyThrows
                          public String parseLiteral(Object input)
                              throws CoercingParseLiteralException {
                            return mapper.writeValueAsString(input);
                          }
                        })
                    .build())
            .scalar(
                GraphQLScalarType.newScalar()
                    .name("DateTime")
                    .coercing(
                        new Coercing<ZonedDateTime, String>() {

                          @Override
                          public String serialize(Object dataFetcherResult)
                              throws CoercingSerializeException {
                            return dataFetcherResult.toString();
                          }

                          @Override
                          public ZonedDateTime parseValue(Object input)
                              throws CoercingParseValueException {
                            return ZonedDateTime.parse(input.toString());
                          }

                          @Override
                          public ZonedDateTime parseLiteral(Object input)
                              throws CoercingParseLiteralException {
                            return ZonedDateTime.parse(input.toString());
                          }
                        })
                    .build())
            .build();
    GraphQLSchema graphQLSchema = buildSchema(sdl, wiring);
    factory.setGraphQLSchema(graphQLSchema);

    factory.setRuntimeWiring(wiring);
    return factory;
  }

  private GraphQLSchema buildSchema(String sdl, RuntimeWiring wiring) {
    TypeDefinitionRegistry typeRegistry = new SchemaParser().parse(sdl);
    RuntimeWiring runtimeWiring = wiring;
    SchemaGenerator schemaGenerator = new SchemaGenerator();
    return schemaGenerator.makeExecutableSchema(typeRegistry, runtimeWiring);
  }
}
