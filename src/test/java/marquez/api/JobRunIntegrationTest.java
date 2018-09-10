package marquez.api;

import com.fasterxml.jackson.core.JsonEncoding;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import marquez.MarquezApp;
import marquez.MarquezConfig;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.net.URI;

import static org.junit.Assert.assertEquals;

public class JobRunIntegrationTest {
    @ClassRule
    public static final DropwizardAppRule<MarquezConfig> APP =
            new DropwizardAppRule<>(
                    MarquezApp.class, ResourceHelpers.resourceFilePath("config.test.yml"));

    @Test
    public void testJobRunCreation() {
        final Response res =
                APP.client()
                        .target(URI.create("http://localhost:" + APP.getLocalPort()))
                        .path("/ping")
                        .request()
                        .get();
        assertEquals(res.getStatus(), 200);
        assertEquals(res.readEntity(String.class), "pong");
    }

    @Test
    public void testJobRunStateCreatedUponJobRunCreation() {
        final String path = "/job_runs";
        final Response res =
                APP.client()
                        .target(URI.create("http://localhost:" + APP.getLocalPort()))
                        .path(path)
                        .request().post(Entity.json("{'k':'v'}"));
        assertEquals(res.getStatus(), 200);
        assertEquals(res.readEntity(String.class), "pong");
    }

    @Test
    public void testJobRunGetter() {
        final String path = "/job_runs";
        final Response res =
                APP.client()
                        .target(URI.create("http://localhost:" + APP.getLocalPort()))
                        .path(path)
                        .request().post(Entity.json("{'k':'v'}"));
        assertEquals(res.getStatus(), 200);
        assertEquals(res.readEntity(String.class), "pong");
    }

    @Test
    public void testJobRunStateGetter() {
        final String path = "/job_run_states";
        final Response res =
                APP.client()
                        .target(URI.create("http://localhost:" + APP.getLocalPort()))
                        .path(path)
                        .request().post(Entity.json("{'k':'v'}"));
        assertEquals(res.getStatus(), 200);
        assertEquals(res.readEntity(String.class), "pong");
    }
}
