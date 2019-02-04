package marquez.common;

import org.junit.Test;

public class PreconditionsTest {
  private static final Object NULL_REFERENCE = null;
  private static final Object NON_NULL_REFERENCE = new Object();
  private static final String NULL_ERROR_MESSAGE = null;
  private static final String NON_NULL_ERROR_MESSAGE = "test error message";
  private static final String BLANK_STRING = " ";
  private static final String NON_BLANK_STRING = "test string";
  private static final String NULL_STRING = null;
  private static final String NON_NULL_STRING = NON_BLANK_STRING;
  private static final boolean TRUE = 0 < 1;
  private static final boolean FALSE = 0 > 1;

  @Test
  public void testCheckNotNull_noErrorMessage() {
    Preconditions.checkNotNull(NON_NULL_REFERENCE);
  }

  @Test(expected = NullPointerException.class)
  public void testCheckNotNull_throwsException_noErrorMessage() {
    Preconditions.checkNotNull(NULL_REFERENCE);
  }

  @Test
  public void testCheckNotNull_nullErrorMessage() {
    Preconditions.checkNotNull(NON_NULL_REFERENCE, NULL_ERROR_MESSAGE);
  }

  @Test
  public void testCheckNotNull_withErrorMessage() {
    Preconditions.checkNotNull(NON_NULL_REFERENCE, NON_NULL_ERROR_MESSAGE);
  }

  @Test(expected = NullPointerException.class)
  public void testCheckNotNull_throwsException_nullErrorMessage() {
    Preconditions.checkNotNull(NULL_REFERENCE, NULL_ERROR_MESSAGE);
  }

  @Test(expected = NullPointerException.class)
  public void testCheckNotNull_throwsException_withErrorMessage() {
    Preconditions.checkNotNull(NULL_REFERENCE, NON_NULL_ERROR_MESSAGE);
  }

  @Test
  public void testCheckNotBlank_noErrorMessage() {
    Preconditions.checkNotBlank(NON_BLANK_STRING);
  }

  @Test(expected = NullPointerException.class)
  public void testCheckNotBlank_throwsException_nullString_noErrorMessage() {
    Preconditions.checkNotBlank(NULL_STRING);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCheckNotBlank_throwsException_noErrorMessage() {
    Preconditions.checkNotBlank(BLANK_STRING);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCheckNotBlank_throwsException_nullErrorMessage() {
    Preconditions.checkNotBlank(BLANK_STRING, NULL_ERROR_MESSAGE);
  }

  @Test
  public void testCheckNotBlank_withErrorMessage() {
    Preconditions.checkNotBlank(NON_BLANK_STRING, NON_NULL_ERROR_MESSAGE);
  }

  @Test(expected = NullPointerException.class)
  public void testCheckNotBlank_throwsException_nullString_nullErrorMessage() {
    Preconditions.checkNotBlank(NULL_STRING, NULL_ERROR_MESSAGE);
  }

  @Test(expected = NullPointerException.class)
  public void testCheckNotBlank_throwsException_nullString_withErrorMessage() {
    Preconditions.checkNotBlank(NULL_STRING, NON_NULL_STRING);
  }

  @Test
  public void testCheckNotBlank_nullErrorMessage() {
    Preconditions.checkNotBlank(NON_BLANK_STRING, NULL_ERROR_MESSAGE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void checkNotBlank_throwsException_withErrorMessage() {
    Preconditions.checkNotBlank(BLANK_STRING, NON_NULL_ERROR_MESSAGE);
  }

  @Test
  public void testCheckArgument_trueExpression() {
    Preconditions.checkArgument(TRUE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCheckArgument_throwsException_onFalseExpression() {
    Preconditions.checkArgument(FALSE);
  }

  @Test
  public void testCheckArgument_trueExpression_nullErrorMessage() {
    Preconditions.checkArgument(TRUE, NULL_ERROR_MESSAGE);
  }

  @Test
  public void testCheckArgument_trueExpression_withErrorMessage() {
    Preconditions.checkArgument(TRUE, NON_NULL_ERROR_MESSAGE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCheckArgument_throwsException_onFalseExpression_nullErrorMessage() {
    Preconditions.checkArgument(FALSE, NULL_ERROR_MESSAGE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCheckArgument_throwsException_onFalseExpression_withErrorMessage() {
    Preconditions.checkArgument(FALSE, NON_NULL_ERROR_MESSAGE);
  }
}
