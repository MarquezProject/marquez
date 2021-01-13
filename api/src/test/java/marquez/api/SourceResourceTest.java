package marquez.api;

import static marquez.api.SourceResource.Sources;
import static marquez.common.models.ModelGenerator.newSourceName;
import static marquez.service.models.ModelGenerator.newSource;
import static marquez.service.models.ModelGenerator.newSourceWith;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import java.util.Optional;
import javax.ws.rs.core.Response;
import marquez.UnitTests;
import marquez.api.exceptions.SourceNotFoundException;
import marquez.common.models.SourceName;
import marquez.service.SourceService;
import marquez.service.exceptions.MarquezServiceException;
import marquez.service.models.Source;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

@Category(UnitTests.class)
public class SourceResourceTest {
  private static final SourceName SOURCE_NAME = newSourceName();

  private static final Source SOURCE_0 = newSource();
  private static final Source SOURCE_1 = newSource();
  private static final Source SOURCE_2 = newSource();
  private static final ImmutableList<Source> SOURCES =
      ImmutableList.of(SOURCE_0, SOURCE_1, SOURCE_2);

  @Rule public MockitoRule rule = MockitoJUnit.rule();

  @Mock private SourceService service;
  private SourceResource resource;

  @Before
  public void setUp() {
    resource = new SourceResource(service);
  }

  @Test
  public void testGet() throws MarquezServiceException {
    final Source source = newSourceWith(SOURCE_NAME);
    when(service.get(SOURCE_NAME)).thenReturn(Optional.of(source));

    final Response response = resource.get(SOURCE_NAME);
    assertThat(response.getStatus()).isEqualTo(200);
    assertThat((Source) response.getEntity()).isEqualTo(source);
  }

  @Test
  public void testGet_notFound() throws MarquezServiceException {
    when(service.get(SOURCE_NAME)).thenReturn(Optional.empty());

    assertThatExceptionOfType(SourceNotFoundException.class)
        .isThrownBy(() -> resource.get(SOURCE_NAME))
        .withMessageContaining(String.format("'%s' not found", SOURCE_NAME.getValue()));
  }

  @Test
  public void testList() throws MarquezServiceException {
    when(service.getAll(4, 0)).thenReturn(SOURCES);

    final Response response = resource.list(4, 0);
    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(((Sources) response.getEntity()).getValue())
        .containsOnly(SOURCE_0, SOURCE_1, SOURCE_2);
  }

  @Test
  public void testList_empty() throws MarquezServiceException {
    when(service.getAll(4, 0)).thenReturn(ImmutableList.of());

    final Response response = resource.list(4, 0);
    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(((Sources) response.getEntity()).getValue()).isEmpty();
  }
}
