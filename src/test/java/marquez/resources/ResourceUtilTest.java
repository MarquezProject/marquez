package marquez.resources;

import static org.junit.Assert.assertEquals;

import java.net.URI;
import org.junit.Test;

public class ResourceUtilTest {
  @Test
  public void testBuildLocation() {
    final URI expectedLocaton = URI.create("/foos/bar");
    final URI location = ResourceUtil.buildLocation(Foo.class, "bar");
    assertEquals(expectedLocaton, location);
  }

  private class Foo {}
}
