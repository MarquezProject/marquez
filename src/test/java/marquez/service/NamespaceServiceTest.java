package marquez.service;

import static marquez.common.models.CommonModelGenerator.newDescription;
import static marquez.common.models.CommonModelGenerator.newNamespaceName;
import static marquez.common.models.CommonModelGenerator.newOwnerName;
import static marquez.service.models.ServiceModelGenerator.newNamespace;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import marquez.UnitTests;
import marquez.common.models.Description;
import marquez.common.models.NamespaceName;
import marquez.common.models.OwnerName;
import marquez.db.NamespaceDao;
import marquez.db.models.NamespaceRow;
import marquez.service.exceptions.MarquezServiceException;
import marquez.service.mappers.NamespaceMapper;
import marquez.service.models.Namespace;
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTests.class)
public class NamespaceServiceTest {
  private final NamespaceName NAMESPACE_NAME = newNamespaceName();
  private final Description DESCRIPTION = newDescription();
  private final OwnerName CURRENT_OWNER_NAME = newOwnerName();
  private final Namespace NEW_NAMESPACE =
      new Namespace(
          null, NAMESPACE_NAME.getValue(), CURRENT_OWNER_NAME.getValue(), DESCRIPTION.getValue());

  private NamespaceDao namespaceDao;
  private NamespaceService namespaceService;

  @Before
  public void setUp() throws MarquezServiceException {
    namespaceDao = mock(NamespaceDao.class);
    namespaceService = new NamespaceService(namespaceDao);
  }

  @Test
  public void testNewDatasetService_throwsException_onNullNamespaceDao() {
    final NamespaceDao nullNamespaceDao = null;
    assertThatNullPointerException().isThrownBy(() -> new NamespaceService(nullNamespaceDao));
  }

  @Test
  public void testCreate_success() throws MarquezServiceException {
    reset(namespaceDao);

    namespaceService.create(NEW_NAMESPACE);

    verify(namespaceDao, times(1)).insert(any(NamespaceRow.class));
  }

  @Test
  public void testCreate_failed() throws MarquezServiceException {
    reset(namespaceDao);

    doThrow(UnableToExecuteStatementException.class)
        .when(namespaceDao)
        .insert(any(NamespaceRow.class));

    assertThatExceptionOfType(MarquezServiceException.class)
        .isThrownBy(() -> namespaceService.create(newNamespace()));

    verify(namespaceDao, times(1)).insert(any(NamespaceRow.class));
  }

  @Test
  public void testCreateOrUpdate_success() throws MarquezServiceException {
    final NamespaceRow namespaceRow =
        NamespaceRow.builder()
            .uuid(UUID.randomUUID())
            .createdAt(Instant.now())
            .name(NAMESPACE_NAME.getValue())
            .description(DESCRIPTION.getValue())
            .currentOwnerName(CURRENT_OWNER_NAME.getValue())
            .build();
    when(namespaceDao.insertAndGet(any(NamespaceRow.class))).thenReturn(Optional.of(namespaceRow));

    final Namespace expected = NamespaceMapper.map(namespaceRow);
    final Namespace actual = namespaceService.createOrUpdate(NEW_NAMESPACE);
    assertThat(actual).isEqualTo(expected);

    verify(namespaceDao, times(1)).insertAndGet(any(NamespaceRow.class));
  }
}
