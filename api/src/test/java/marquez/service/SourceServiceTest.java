package marquez.service;

import static marquez.common.models.ModelGenerator.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.net.URI;
import java.time.Instant;
import java.util.UUID;
import marquez.UnitTests;
import marquez.common.models.SourceName;
import marquez.common.models.SourceType;
import marquez.db.SourceDao;
import marquez.db.models.SourceRow;
import marquez.service.models.Source;
import marquez.service.models.SourceMeta;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

@Category(UnitTests.class)
public class SourceServiceTest {
  private static final UUID ROW_UUID = UUID.randomUUID();
  private static final Instant NOW = Instant.now();
  private static final SourceType TYPE = newDbSourceType();
  private static final SourceName NAME = newSourceName();
  private static final URI CONNECTION_URL = newConnectionUrlFor(TYPE);
  private static final String DESCRIPTION = newDescription();

  // SOURCE META
  private static final SourceMeta META = new SourceMeta(CONNECTION_URL, DESCRIPTION);

  // SOURCE
  private static final Source SOURCE =
      new Source(TYPE, NAME, NOW, NOW, CONNECTION_URL, DESCRIPTION);

  // SOURCE ROW
  private static final SourceRow ROW =
      new SourceRow(
          ROW_UUID,
          TYPE.getValue(),
          NOW,
          NOW,
          NAME.getValue(),
          CONNECTION_URL.toASCIIString(),
          DESCRIPTION);

  @Rule public MockitoRule rule = MockitoJUnit.rule();

  @Mock private SourceDao dao;
  private SourceService service;

  @Before
  public void setUp() {
    service = new SourceService(dao);
  }

  @Test
  public void testCreateOrUpdate() {
    doReturn(ROW)
        .when(dao)
        .upsert(
            any(UUID.class),
            eq(TYPE.getValue()),
            any(Instant.class),
            any(Instant.class),
            eq(NAME.getValue()),
            eq(CONNECTION_URL.toASCIIString()),
            eq(DESCRIPTION));

    final Source source = service.createOrUpdate(NAME, META);
    assertThat(source).isEqualTo(SOURCE);

    verify(dao, times(1))
        .upsert(
            any(UUID.class),
            eq(TYPE.getValue()),
            any(Instant.class),
            any(Instant.class),
            eq(NAME.getValue()),
            eq(CONNECTION_URL.toASCIIString()),
            eq(DESCRIPTION));
  }

  @Test
  public void testExists() {
    when(dao.exists(NAME.getValue())).thenReturn(true);

    final boolean exists = service.exists(NAME);
    assertThat(exists).isTrue();
  }
}
