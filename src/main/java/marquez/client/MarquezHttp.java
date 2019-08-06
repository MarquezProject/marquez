package marquez.client;

import static marquez.client.Preconditions.checkNotBlank;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import marquez.client.exceptions.InternalServerException;
import marquez.client.exceptions.InvalidRequestException;
import marquez.client.exceptions.MarquezException;
import marquez.client.exceptions.RequestNotFoundException;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;

@AllArgsConstructor
public class MarquezHttp {
  private static final String API_PATH = "/api/v1";

  private final String baseUrl;

  /**
   * HTTP GET request wrapper for Marquez Core API calls.
   *
   * @param url A valid URL (with query parameters if required), use {@code url} method to generate
   * @return {@code String} JSON response for GET request
   * @throws IOException
   * @throws MarquezException
   */
  public String get(@NonNull final URL url) throws IOException, MarquezException {
    final HttpURLConnection con = (HttpURLConnection) url.openConnection();
    con.setRequestMethod("GET");
    con.setRequestProperty("Accept", "application/json");
    con.setDoOutput(true);
    return getResponse(con);
  }

  /**
   * HTTP POST request wrapper for Marquez Core API calls.
   *
   * @param url A valid URL, use {@code url} method to generate
   * @param payload A valid JSON payload
   * @return {@code String} JSON response for POST request
   * @throws IOException
   * @throws MarquezException
   */
  public String post(@NonNull final URL url, @NonNull final String payload)
      throws IOException, MarquezException {
    checkNotBlank(payload);

    final HttpURLConnection con = (HttpURLConnection) url.openConnection();
    con.setRequestMethod("POST");
    setupPayload(con, payload);
    return getResponse(con);
  }

  /**
   * HTTP PUT request wrapper for Marquez Core API calls.
   *
   * @param url A valid URL, use {@code url} method to generate
   * @param payload A valid JSON payload
   * @return {@code String} JSON response for PUT request
   * @throws IOException
   * @throws MarquezException
   */
  public String put(@NonNull final URL url, @NonNull final String payload)
      throws IOException, MarquezException {
    checkNotBlank(payload);

    final HttpURLConnection con = (HttpURLConnection) url.openConnection();
    con.setRequestMethod("PUT");
    setupPayload(con, payload);
    return getResponse(con);
  }

  /**
   * HTTP PUT request wrapper for Marquez Core API calls.
   *
   * @param url A valid URL, use {@code url} method to generate
   * @return {@code String} JSON response for PUT request
   * @throws IOException
   * @throws MarquezException
   */
  public String put(@NonNull final URL url) throws IOException, MarquezException {
    final HttpURLConnection con = (HttpURLConnection) url.openConnection();
    con.setRequestMethod("PUT");
    con.setRequestProperty("Accept", "application/json");
    con.setDoOutput(true);
    return getResponse(con);
  }

  private void setupPayload(final HttpURLConnection con, final String payload) throws IOException {
    con.setRequestProperty("Content-Type", "application/json");
    con.setRequestProperty("Accept", "application/json");
    con.setDoOutput(true);
    OutputStream os = con.getOutputStream();
    byte[] input = payload.getBytes("utf-8");
    os.write(input, 0, input.length);
  }

  private String getResponse(final HttpURLConnection con) throws IOException, MarquezException {
    final BufferedReader bufferedReader =
        new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"));

    final Integer responseCode = con.getResponseCode();
    if (responseCode == HttpURLConnection.HTTP_BAD_REQUEST) {
      throw new InvalidRequestException();
    } else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
      throw new RequestNotFoundException();
    } else if (responseCode == HttpURLConnection.HTTP_BAD_METHOD) {
      throw new RequestNotFoundException();
    } else if (responseCode >= 500) {
      throw new InternalServerException();
    } else if (!(responseCode >= 200 && responseCode < 400)) {
      throw new MarquezException();
    }

    final StringBuilder response = new StringBuilder();
    String responseLine = null;
    while ((responseLine = bufferedReader.readLine()) != null) {
      response.append(responseLine.trim());
    }
    return response.toString();
  }

  /**
   * Generates a {@link URL} for Marquez Core API calls based on the given path.
   *
   * @param path path for the API call
   * @return a {@link URL} object
   * @throws URISyntaxException
   * @throws MalformedURLException
   */
  public URL url(final String path) throws URISyntaxException, MalformedURLException {
    return url(path, Collections.emptyMap());
  }

  /**
   * Generates a URL for Marquez Core API calls based on the given path and query parameters.
   *
   * @param path path for the API call
   * @return a {@link URL} object
   * @throws URISyntaxException
   * @throws MalformedURLException
   */
  public URL url(@NonNull final String path, @NonNull final Map<String, Object> params)
      throws URISyntaxException, MalformedURLException {
    checkNotBlank(path);

    final List<NameValuePair> nvps = new ArrayList<>();
    for (Map.Entry<String, Object> entry : params.entrySet()) {
      final BasicNameValuePair nameValuePair =
          new BasicNameValuePair(entry.getKey(), String.valueOf(entry.getValue()));
      nvps.add(nameValuePair);
    }

    return new URIBuilder(baseUrl).setPath(API_PATH + path).setParameters(nvps).build().toURL();
  }
}
