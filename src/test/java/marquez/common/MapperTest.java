package marquez.common;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class MapperTest {
  @Test
  public void testMapList() {
    List<A> listA = Arrays.asList(new A());
    List<B> listB = B_MAPPER.map(listA);
    assertEquals(1, listB.size());
  }

  @Test
  public void testMapEmptyList() {
    List<A> emptyListA = new ArrayList<A>();
    List<B> listB = B_MAPPER.map(emptyListA);
    assertEquals(0, listB.size());
  }

  @Test(expected = NullPointerException.class)
  public void testMapThrowOnNull() {
    List<A> nullListA = null;
    B_MAPPER.map(nullListA);
  }

  private static final Mapper<A, B> B_MAPPER =
      new Mapper<A, B>() {
        @Override
        public B map(A value) {
          return value == null ? null : new B();
        }
      };

  private static final class A {}

  private static final class B {}
}
