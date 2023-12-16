package marquez.api.filter.exclusions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class ExclusionsConfigTest {
  @Test
  public void testNamespaceExclusionsOnRead() {
    ExclusionsConfig exclusionsConfig = new ExclusionsConfig();
    ExclusionsConfig.NamespaceExclusions namespaceExclusions =
        new ExclusionsConfig.NamespaceExclusions();
    ExclusionsConfig.OnRead onRead = new ExclusionsConfig.OnRead();
    onRead.enabled = true;
    onRead.pattern = "readPattern";
    namespaceExclusions.onRead = onRead;

    exclusionsConfig.namespaces = namespaceExclusions;

    assertEquals(true, exclusionsConfig.namespaces.onRead.enabled);
    assertEquals("readPattern", exclusionsConfig.namespaces.onRead.pattern);
  }

  @Test
  public void testNamespaceExclusionsOnWrite() {
    ExclusionsConfig exclusionsConfig = new ExclusionsConfig();
    ExclusionsConfig.NamespaceExclusions namespaceExclusions =
        new ExclusionsConfig.NamespaceExclusions();
    ExclusionsConfig.OnWrite onWrite = new ExclusionsConfig.OnWrite();
    onWrite.enabled = false;
    onWrite.pattern = "writePattern";
    namespaceExclusions.onWrite = onWrite;

    exclusionsConfig.namespaces = namespaceExclusions;

    assertEquals(false, exclusionsConfig.namespaces.onWrite.enabled);
    assertEquals("writePattern", exclusionsConfig.namespaces.onWrite.pattern);
  }
}
