package marquez.graphql;

import static graphql.schema.idl.TypeRuntimeWiring.newTypeWiring;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import graphql.TypeResolutionEnvironment;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLSchema;
import graphql.schema.TypeResolver;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.RuntimeWiring.Builder;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import java.net.URL;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import lombok.SneakyThrows;
import marquez.common.models.DatasetName;
import marquez.common.models.DatasetType;
import marquez.common.models.FieldName;
import marquez.common.models.FieldType;
import marquez.common.models.JobName;
import marquez.common.models.JobType;
import marquez.common.models.NamespaceName;
import marquez.common.models.RunId;
import marquez.common.models.RunState;
import marquez.common.models.SourceName;
import marquez.common.models.TagName;
import marquez.db.models.DatasetData;
import marquez.db.models.JobData;
import marquez.graphql.mapper.LineageResultMapper.DatasetResult;
import marquez.graphql.mapper.LineageResultMapper.JobResult;
import marquez.service.models.NodeId;
import marquez.service.models.NodeType;
import org.jdbi.v3.core.Jdbi;

public class GraphqlSchemaBuilder {
  private final Jdbi jdbi;

  public GraphqlSchemaBuilder(Jdbi jdbi) {
    this.jdbi = jdbi;
  }

  @SneakyThrows
  public GraphQLSchema buildSchema() {
    URL url = Resources.getResource("schema.graphqls");
    String sdl = Resources.toString(url, Charsets.UTF_8);
    Builder wiring = RuntimeWiring.newRuntimeWiring();
    buildRuntimeWiring(jdbi, wiring);

    TypeDefinitionRegistry typeDefinitionRegistry = new SchemaParser().parse(sdl);

    SchemaGenerator schemaGenerator = new SchemaGenerator();
    return schemaGenerator.makeExecutableSchema(typeDefinitionRegistry, wiring.build());
  }

  public void buildRuntimeWiring(Jdbi jdbi, Builder wiring) {
    GraphqlDataFetchers dataFetchers = new GraphqlDataFetchers(jdbi);

    wiring
        .type(
            newTypeWiring("Query")
                .dataFetcher("datasets", dataFetchers.getDatasets())
                .dataFetcher("dataset", dataFetchers.getDatasetByNamespaceAndName())
                .dataFetcher("namespace", dataFetchers.getNamespaceByName())
                .dataFetcher("jobs", dataFetchers.getJobs())
                .dataFetcher("job", dataFetchers.getQueryJobByNamespaceAndName())
                .dataFetcher("lineage", dataFetchers.getLineage()))
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
        .type(newTypeWiring("Source").dataFetcher("datasets", dataFetchers.getDatasetsBySource()))
        .type(
            newTypeWiring("RunStateRecord")
                .dataFetcher("run", dataFetchers.getRunByRunStateRecord()))
        .type(
            newTypeWiring("Run")
                .dataFetcher("jobVersion", dataFetchers.getJobVersionByRun())
                .dataFetcher("runArgs", dataFetchers.getRunArgsByRun())
                .dataFetcher("states", dataFetchers.getRunStatesByRun())
                .dataFetcher("startState", dataFetchers.getStartStateByRun())
                .dataFetcher("endState", dataFetchers.getEndStateByRun())
                .dataFetcher("inputs", dataFetchers.getInputsByRun())
                .dataFetcher("outputs", dataFetchers.getOutputsByRun()))
        .type(newTypeWiring("Owner").dataFetcher("namespaces", dataFetchers.getNamespacesByOwner()))
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
        .type(
            newTypeWiring("JobLineageEntry")
                .dataFetcher("data", dataFetchers.getLineageJobsByNamespaceAndName()))
        .type(
            newTypeWiring("DatasetLineageEntry")
                .dataFetcher("data", dataFetchers.getLineageDatasetsByNamespaceAndName()))
        .type(
            newTypeWiring("NodeData")
                .typeResolver(
                    new TypeResolver() {
                      @Override
                      public GraphQLObjectType getType(TypeResolutionEnvironment env) {
                        Object javaObject = env.getObject();
                        if (javaObject instanceof JobData) {
                          return env.getSchema().getObjectType("Job");
                        } else if (javaObject instanceof DatasetData) {
                          return env.getSchema().getObjectType("Dataset");
                        } else {
                          throw new RuntimeException("Lineage type not recognized");
                        }
                      }
                    }))
        .scalar(GraphQLScalarType.newScalar()
            .name("NodeId")
            .coercing(coerce(NodeId::getValue, NodeId::of)).build()
        )
        .scalar(GraphQLScalarType.newScalar()
                .name("NodeType")
                .coercing(coerce(NodeType::toString, NodeType::valueOf)).build())
        .scalar(GraphQLScalarType.newScalar()
            .name("NamespaceName")
            .coercing(coerce(NamespaceName::getValue, NamespaceName::of)).build())
        .scalar(GraphQLScalarType.newScalar()
            .name("DatasetName")
            .coercing(coerce(DatasetName::getValue, DatasetName::of)).build())
        .scalar(GraphQLScalarType.newScalar()
            .name("DatasetType")
            .coercing(coerce(DatasetType::toString, DatasetType::valueOf)).build())
        .scalar(GraphQLScalarType.newScalar()
            .name("SourceName")
            .coercing(coerce(SourceName::getValue, SourceName::of)).build())
        .scalar(GraphQLScalarType.newScalar()
            .name("FieldName")
            .coercing(coerce(FieldName::getValue, FieldName::of)).build())
        .scalar(GraphQLScalarType.newScalar()
            .name("FieldType")
            .coercing(coerce(FieldType::toString, FieldType::valueOf)).build())
        .scalar(GraphQLScalarType.newScalar()
            .name("TagName")
            .coercing(coerce(TagName::getValue, TagName::of)).build())
        .scalar(GraphQLScalarType.newScalar()
            .name("JobName")
            .coercing(coerce(JobName::getValue, JobName::of)).build())
        .scalar(GraphQLScalarType.newScalar()
            .name("JobType")
            .coercing(coerce(JobType::toString, JobType::valueOf)).build())
        .scalar(GraphQLScalarType.newScalar()
            .name("RunId")
            .coercing(coerce((RunId r) -> r.getValue().toString(), s -> RunId.of(UUID.fromString(s)))).build())
        .scalar(GraphQLScalarType.newScalar()
            .name("RunState")
            .coercing(coerce(RunState::toString, RunState::valueOf)).build())
        .scalar(
            GraphQLScalarType.newScalar()
                .name("UUID")
                .coercing(coerce(UUID::toString, UUID::fromString))
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
                      public String parseValue(Object input) throws CoercingParseValueException {
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
                .build());
  }

  private <I> Coercing<I, String> coerce(Function<I, String> ser, Function<String, I> deser) {
    return new Coercing<>() {
      @Override
      public String serialize(Object dataFetcherResult) throws CoercingSerializeException {
        return ser.apply((I) dataFetcherResult);
      }

      @Override
      public I parseValue(Object input) throws CoercingParseValueException {
        return deser.apply(input.toString());
      }

      @Override
      public I parseLiteral(Object input) throws CoercingParseLiteralException {
        return deser.apply(input.toString());
      }
    };
  }
}
