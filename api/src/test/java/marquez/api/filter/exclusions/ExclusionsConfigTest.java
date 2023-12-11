package marquez.api.filter.exclusions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import marquez.api.filter.exclusions.ExclusionsConfig.NamespaceExclusion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ExclusionsConfigTest {

  private ExclusionsConfig exclusionsConfig;
  private NamespaceExclusion namespaceExclusion;

  @BeforeEach
  void setUp() {
    exclusionsConfig = new ExclusionsConfig();
    namespaceExclusion = new NamespaceExclusion();
  }

  @Test
  void testNamespaceExclusionOnRead() {
    namespaceExclusion.onRead = true;
    assertEquals(true, namespaceExclusion.isOnRead());
  }

  @Test
  void testNamespaceExclusionOnWrite() {
    namespaceExclusion.onWrite = true;
    assertEquals(true, namespaceExclusion.isOnWrite());
  }

  @Test
  void testNamespaceExclusionPatterns() {
    String patterns = "test-pattern";
    namespaceExclusion.patterns = patterns;
    assertEquals(patterns, namespaceExclusion.getPatterns());
  }

  @Test
  void testExclusionsConfigNamespaces() {
    exclusionsConfig.namespaces = namespaceExclusion;
    assertNotNull(exclusionsConfig.getNamespaces());
    assertEquals(namespaceExclusion, exclusionsConfig.getNamespaces());
  }
}
