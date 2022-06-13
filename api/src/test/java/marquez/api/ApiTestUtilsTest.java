/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.Collections;
import java.util.Map;
import marquez.service.DatasetService;
import marquez.service.ServiceFactory;
import org.junit.jupiter.api.Test;

class ApiTestUtilsTest {

  @Test
  void testDefaultServiceFactory() {
    ServiceFactory serviceFactory = ApiTestUtils.mockServiceFactory(Collections.emptyMap());
    assertThat(serviceFactory).isNotNull().hasNoNullFieldsOrProperties();
  }

  @Test
  void testCustomServiceFactory() {
    DatasetService customMock = mock(DatasetService.class);
    ServiceFactory serviceFactory =
        ApiTestUtils.mockServiceFactory(Map.of(DatasetService.class, customMock));
    assertThat(serviceFactory).isNotNull().extracting("datasetService").isEqualTo(customMock);
  }
}
