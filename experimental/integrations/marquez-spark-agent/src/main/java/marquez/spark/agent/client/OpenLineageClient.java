package marquez.spark.agent.client;

import static org.apache.hc.core5.http.HttpHeaders.AUTHORIZATION;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ok2c.hc5.json.http.JsonRequestProducers;
import com.ok2c.hc5.json.http.JsonResponseConsumers;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.async.methods.BasicHttpRequests;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.cookie.StandardCookieSpec;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.client5.http.ssl.ClientTlsStrategyBuilder;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.Message;
import org.apache.hc.core5.http.ssl.TLS;
import org.apache.hc.core5.reactor.IOReactorConfig;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.util.Timeout;

@Slf4j
public class OpenLineageClient {
  private final CloseableHttpAsyncClient http;
  private final Optional<String> apiKey;
  @Getter protected static final ObjectMapper objectMapper = createMapper();

  public OpenLineageClient(CloseableHttpAsyncClient http, Optional<String> apiKey) {
    this.http = http;
    this.http.start();
    this.apiKey = apiKey;
  }

  public static OpenLineageClient create(final Optional<String> apiKey) {
    final CloseableHttpAsyncClient http =
        HttpAsyncClients.customHttp2()
            .setTlsStrategy(
                ClientTlsStrategyBuilder.create()
                    .setSslContext(SSLContexts.createSystemDefault())
                    .setTlsVersions(TLS.V_1_3, TLS.V_1_2)
                    .build())
            .setIOReactorConfig(IOReactorConfig.custom().setSoTimeout(Timeout.ofSeconds(5)).build())
            .setDefaultRequestConfig(
                RequestConfig.custom()
                    .setConnectTimeout(Timeout.ofSeconds(5))
                    .setResponseTimeout(Timeout.ofSeconds(5))
                    .setCookieSpec(StandardCookieSpec.STRICT)
                    .build())
            .setUserAgent(getUserAgent())
            .build();
    return new OpenLineageClient(http, apiKey);
  }

  public <T> void post(URI uri, Object obj) throws MarquezHttpException {
    post(uri, obj, Void.class);
  }

  public <T> T post(URI uri, Object obj, Class<T> clazz) throws MarquezHttpException {
    return post(uri, obj, getTypeReference(clazz));
  }

  public <T> T post(URI uri, Object obj, TypeReference<T> ref) throws MarquezHttpException {
    return executeSync(BasicHttpRequests.post(uri), obj, ref);
  }

  public <T> T executeSync(HttpRequest request, Object obj, TypeReference<T> ref)
      throws MarquezHttpException {
    CompletableFuture<ResponseMessage<T>> future = executeAsync(request, obj, ref);
    try {
      ResponseMessage<T> message = future.get();
      if (message.completedSuccessfully()) {
        return message.getBody();
      }
      throw new MarquezHttpException(message.getError());
    } catch (ExecutionException | InterruptedException ignored) {
      throw new MarquezHttpException();
    }
  }

  public CompletableFuture<ResponseMessage<Void>> postAsync(URI uri, Object obj) {
    return postAsync(uri, obj, Void.class);
  }

  public <T> CompletableFuture<ResponseMessage<T>> postAsync(URI uri, Object obj, Class<T> clazz) {
    return postAsync(uri, obj, getTypeReference(clazz));
  }

  public <T> CompletableFuture<ResponseMessage<T>> postAsync(
      URI uri, Object obj, TypeReference<T> ref) {
    return executeAsync(BasicHttpRequests.post(uri), obj, ref);
  }

  protected <T> CompletableFuture<ResponseMessage<T>> executeAsync(
      HttpRequest request, Object obj, TypeReference<T> ref) {
    addAuthToReqIfKeyPresent(request);

    Future<Message<HttpResponse, JsonNode>> future =
        http.execute(
            JsonRequestProducers.create(request, obj, objectMapper),
            JsonResponseConsumers.create(objectMapper.getFactory()),
            null);

    return CompletableFuture.supplyAsync(
        () -> {
          try {
            Message<HttpResponse, JsonNode> message = future.get();
            return createMessage(message, ref);
          } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
          }
        });
  }

  private <T> ResponseMessage<T> createMessage(
      Message<HttpResponse, JsonNode> message, TypeReference<T> ref) {
    if (!completedSuccessfully(message)) {
      return new ResponseMessage<>(
          message.getHead().getCode(),
          null,
          objectMapper.convertValue(message.getBody(), HttpError.class));
    }

    return new ResponseMessage<>(
        message.getHead().getCode(), objectMapper.convertValue(message.getBody(), ref), null);
  }

  private boolean completedSuccessfully(Message<HttpResponse, JsonNode> message) {
    final int code = message.getHead().getCode();
    if (code >= 400 && code < 600) { // non-2xx
      return false;
    }
    return true;
  }

  public static ObjectMapper createMapper() {
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());
    mapper.setSerializationInclusion(Include.NON_NULL);
    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    mapper.disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);
    return mapper;
  }

  private void addAuthToReqIfKeyPresent(final HttpRequest request) {
    if (apiKey.isPresent()) {
      request.addHeader(AUTHORIZATION, "Bearer " + apiKey.get());
    }
  }

  protected static String getUserAgent() {
    return "openlineage-java" + "/1.0";
  }

  private <T> TypeReference<T> getTypeReference(Class<T> clazz) {
    return new TypeReference<T>() {
      @Override
      public Type getType() {
        return clazz;
      }
    };
  }

  public void close() {
    try {
      http.close();
    } catch (IOException e) {
      e.printStackTrace(System.out);
    }
  }
}
