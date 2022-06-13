/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import marquez.client.models.Namespace;
import marquez.client.models.NamespaceMeta;
import org.junit.jupiter.api.Test;

@org.junit.jupiter.api.Tag("IntegrationTests")
public class NamespaceIntegrationTest extends BaseIntegrationTest {

  @Test
  public void testApp_createNoDescription() {
    NamespaceMeta namespaceMeta = NamespaceMeta.builder().ownerName(OWNER_NAME).build();
    Namespace namespace = client.createNamespace("NO_DESCRIPTION", namespaceMeta);
    assertThat(namespace.getName()).isEqualTo("NO_DESCRIPTION");
    assertThat(namespace.getOwnerName()).isEqualTo(OWNER_NAME);
    assertThat(namespace.getUpdatedAt()).isNotNull();
    assertThat(namespace.getCreatedAt()).isNotNull();
    assertThat(namespace.getDescription()).isEqualTo(Optional.empty());
  }

  @Test
  public void testApp_changeOwner() {
    NamespaceMeta namespaceMeta =
        NamespaceMeta.builder().ownerName("daniel").description(NAMESPACE_DESCRIPTION).build();
    Namespace namespace = client.createNamespace("HOME_NAMESPACE", namespaceMeta);
    assertThat(namespace.getName()).isEqualTo("HOME_NAMESPACE");
    assertThat(namespace.getOwnerName()).isEqualTo("daniel");
    assertThat(namespace.getUpdatedAt()).isNotNull();
    assertThat(namespace.getCreatedAt()).isNotNull();
    assertThat(namespace.getDescription().get()).isEqualTo(NAMESPACE_DESCRIPTION);

    NamespaceMeta newOwner =
        NamespaceMeta.builder().ownerName("willy").description(NAMESPACE_DESCRIPTION).build();
    Namespace ownerChange = client.createNamespace("HOME_NAMESPACE", newOwner);
    assertThat(namespace.getName()).isEqualTo("HOME_NAMESPACE");
    assertThat(ownerChange.getOwnerName()).isEqualTo("willy");
    assertThat(ownerChange.getUpdatedAt()).isNotNull();
    assertThat(ownerChange.getCreatedAt()).isNotNull();
    assertThat(ownerChange.getDescription().get()).isEqualTo(NAMESPACE_DESCRIPTION);

    // change it back to assure that it can be change again
    Namespace revertChange = client.createNamespace("HOME_NAMESPACE", namespaceMeta);
    assertThat(revertChange.getName()).isEqualTo("HOME_NAMESPACE");
    assertThat(revertChange.getOwnerName()).isEqualTo("daniel");
    assertThat(revertChange.getUpdatedAt()).isNotNull();
    assertThat(revertChange.getCreatedAt()).isNotNull();
    assertThat(revertChange.getDescription().get()).isEqualTo(NAMESPACE_DESCRIPTION);
  }

  @Test
  public void testApp_getNamespace() {
    NamespaceMeta namespaceMeta =
        NamespaceMeta.builder().ownerName(OWNER_NAME).description(NAMESPACE_DESCRIPTION).build();
    client.createNamespace(NAMESPACE_NAME, namespaceMeta);
    Namespace namespace = client.getNamespace(NAMESPACE_NAME);
    assertThat(namespace.getName()).isEqualTo(NAMESPACE_NAME);
    assertThat(namespace.getOwnerName()).isEqualTo(OWNER_NAME);
    assertThat(namespace.getUpdatedAt()).isNotNull();
    assertThat(namespace.getCreatedAt()).isNotNull();
    assertThat(namespace.getDescription().get()).isEqualTo(NAMESPACE_DESCRIPTION);
  }
}
