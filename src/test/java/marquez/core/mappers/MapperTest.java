package marquez.core.mappers;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Optional;
import org.junit.Test;

public class MapperTest {
  @Test
  public void testMapAsOptional() {
    Optional<B> optB = B_MAPPER.mapAsOptional(new A());
    assertTrue(optB.isPresent());
  }

  @Test
  public void testMapAsOptionalFromNull() {
    A nullA = null;
    Optional<B> optB = B_MAPPER.mapAsOptional(nullA);
    assertFalse(optB.isPresent());
  }

  @Test
  public void testMapFromOptional() {
    Optional<A> optA = Optional.of(new A());
    Optional<B> optB = B_MAPPER.mapIfPresent(optA);
    assertTrue(optB.isPresent());
  }

  @Test(expected = NullPointerException.class)
  public void testMapThrowOnNull() {
    Optional<A> nullOptA = null;
    B_MAPPER.mapIfPresent(nullOptA);
  }

  private static final Mapper<A, B> B_MAPPER =
      new Mapper<A, B>() {
        public B map(A value) {
          return value == null ? null : new B();
        }
      };

  private static final class A {}

  private static final class B {}
}
