/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.client;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;
import marquez.client.models.DatasetFieldId;
import marquez.client.models.DatasetFieldVersionId;
import marquez.client.models.DatasetId;
import marquez.client.models.DatasetVersionId;
import marquez.client.models.JobId;
import marquez.client.models.JobVersionId;
import marquez.client.models.NodeId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

@org.junit.jupiter.api.Tag("UnitTests")
public class MarquezUrlTest {
  static String basePath = "http://marquez:5000";
  static MarquezUrl marquezUrl;
  static String version = UUID.randomUUID().toString();

  @BeforeAll
  static void beforeAll() throws MalformedURLException {
    marquezUrl = MarquezUrl.create(Utils.toUrl(basePath));
  }

  @Test
  void testBasicMarquezUrl() {
    URL url = marquezUrl.from("/namespace/nname/job/jname");
    Assertions.assertEquals("http://marquez:5000/namespace/nname/job/jname", url.toString());
  }

  @Test
  void testEncodedMarquezUrl() {
    URL url = marquezUrl.from("/namespace/s3:%2F%2Fbucket/job/jname");
    Assertions.assertEquals(
        "http://marquez:5000/namespace/s3:%2F%2Fbucket/job/jname", url.toString());
  }

  @Test
  void testToLineageUrl() {
    Assertions.assertEquals(
        "http://marquez:5000/api/v1/lineage?nodeId=dataset%3Anamespace%3Adataset&depth=20",
        marquezUrl.toLineageUrl(NodeId.of(new DatasetId("namespace", "dataset")), 20).toString());

    Assertions.assertEquals(
        "http://marquez:5000/api/v1/lineage?nodeId=datasetField%3Anamespace%3Adataset%3Afield&depth=20",
        marquezUrl
            .toLineageUrl(NodeId.of(new DatasetFieldId("namespace", "dataset", "field")), 20)
            .toString());

    Assertions.assertEquals(
        "http://marquez:5000/api/v1/lineage?nodeId=job%3Anamespace%3Ajob&depth=20",
        marquezUrl.toLineageUrl(NodeId.of(new JobId("namespace", "job")), 20).toString());

    Assertions.assertEquals(
        "http://marquez:5000/api/v1/lineage?nodeId=dataset%3Anamespace%3Adataset%23"
            + version
            + "&depth=20",
        marquezUrl
            .toLineageUrl(
                NodeId.of(new DatasetVersionId("namespace", "dataset", UUID.fromString(version))),
                20)
            .toString());

    Assertions.assertEquals(
        "http://marquez:5000/api/v1/lineage?nodeId=datasetField%3Anamespace%3Adataset%3Afield%23"
            + version
            + "&depth=20",
        marquezUrl
            .toLineageUrl(
                NodeId.of(
                    new DatasetFieldVersionId(
                        "namespace", "dataset", "field", UUID.fromString(version))),
                20)
            .toString());

    Assertions.assertEquals(
        "http://marquez:5000/api/v1/lineage?nodeId=job%3Anamespace%3Ajob%23"
            + version
            + "&depth=20",
        marquezUrl
            .toLineageUrl(
                NodeId.of(new JobVersionId("namespace", "job", UUID.fromString(version))), 20)
            .toString());
  }

  @Test
  void testToColumnLineageUrl() {
    Assertions.assertEquals(
        "http://marquez:5000/api/v1/column-lineage?nodeId=dataset%3Anamespace%3Adataset&depth=20&withDownstream=true",
        marquezUrl
            .toColumnLineageUrl(NodeId.of(new DatasetId("namespace", "dataset")), 20, true)
            .toString());

    Assertions.assertEquals(
        "http://marquez:5000/api/v1/column-lineage?nodeId=datasetField%3Anamespace%3Adataset%3Afield&depth=20&withDownstream=true",
        marquezUrl
            .toColumnLineageUrl(
                NodeId.of(new DatasetFieldId("namespace", "dataset", "field")), 20, true)
            .toString());

    Assertions.assertEquals(
        "http://marquez:5000/api/v1/column-lineage?nodeId=job%3Anamespace%3Ajob&depth=20&withDownstream=true",
        marquezUrl
            .toColumnLineageUrl(NodeId.of(new JobId("namespace", "job")), 20, true)
            .toString());

    Assertions.assertEquals(
        "http://marquez:5000/api/v1/column-lineage?nodeId=dataset%3Anamespace%3Adataset%23"
            + version
            + "&depth=20&withDownstream=true",
        marquezUrl
            .toColumnLineageUrl(
                NodeId.of(new DatasetVersionId("namespace", "dataset", UUID.fromString(version))),
                20,
                true)
            .toString());

    Assertions.assertEquals(
        "http://marquez:5000/api/v1/column-lineage?nodeId=datasetField%3Anamespace%3Adataset%3Afield%23"
            + version
            + "&depth=20&withDownstream=true",
        marquezUrl
            .toColumnLineageUrl(
                NodeId.of(
                    new DatasetFieldVersionId(
                        "namespace", "dataset", "field", UUID.fromString(version))),
                20,
                true)
            .toString());

    Assertions.assertEquals(
        "http://marquez:5000/api/v1/column-lineage?nodeId=job%3Anamespace%3Ajob%23"
            + version
            + "&depth=20&withDownstream=true",
        marquezUrl
            .toColumnLineageUrl(
                NodeId.of(new JobVersionId("namespace", "job", UUID.fromString(version))), 20, true)
            .toString());
  }
}
