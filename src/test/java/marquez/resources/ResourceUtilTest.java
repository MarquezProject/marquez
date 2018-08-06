package marquez.resources;

import java.net.URI;
import org.junit.Assert;
import org.junit.Test;

public class ResourceUtilTest {
  @Test
  public void testBuildLocation() {
    final URI location = ResourceUtil.buildLocation(Foo.class, "bar");
    Assert.assertEquals(URI.create("/foos/bar"), location);
  }

  private class Foo {}
}
