package marquez.common.models;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class FieldTypeTest {

  @ParameterizedTest
  @CsvSource(
      delimiter = '|',
      value = {
        "VARCHAR(64)|VARCHAR",
        "DECIMAL(10,2)|DECIMAL",
        "char(100)|CHAR",
        "char|CHAR",
        "BOOLEAN|BOOLEAN",
        "TEST|UNKNOWN",
      })
  void testFromString(String input, String expected) {
    assertEquals(expected, FieldType.fromString(input).name());
  }

  @Test
  void testNullType() {
    assertEquals(FieldType.UNKNOWN, FieldType.fromString(null));
  }
}
