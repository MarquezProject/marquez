package marquez.resources;

import org.junit.Assert;
import org.junit.Test;
import java.net.URI;

public class ResourceUtilTest {
  @Test
  public void testBuildLocation() {
    final URI location = ResourceUtil.buildLocation(Foo.class, "bar");

    Assert.assertEquals(URI.create("/foos/bar"), location);
  }

  private class Foo {}
} 
