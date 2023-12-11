package marquez.api.filter.exclusions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ExclusionsFilterTest {

  private static final String TEST_FILTER = "test-filter";

  @BeforeEach
  public void setUp() {
    ExclusionsFilter.setNamespacesReadFilter(null);
  }

  @Test
  public void testSetAndGetNamespacesReadFilter() {
    ExclusionsFilter.setNamespacesReadFilter(TEST_FILTER);
    String result = ExclusionsFilter.getNamespacesReadFilter();
    assertEquals(TEST_FILTER, result);
  }
}
