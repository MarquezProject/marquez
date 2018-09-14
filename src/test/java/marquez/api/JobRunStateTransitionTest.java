package marquez.api;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Parameterized.class)
public class JobRunStateTransitionTest {

  private JobRunState.State startingState;
  private JobRunState.State endingState;
  private Boolean validTransition;

  @Parameterized.Parameters(name = "Transition from {0} to {1} supported: {2}")
  public static Iterable<Object[]> validTransitions() {
    return Arrays.asList(
        new Object[][] {
          {JobRunState.State.NEW, JobRunState.State.RUNNING, true},
          {JobRunState.State.RUNNING, JobRunState.State.ABORTED, true},
          {JobRunState.State.RUNNING, JobRunState.State.COMPLETED, true},
          {JobRunState.State.RUNNING, JobRunState.State.FAILED, true},
          {JobRunState.State.RUNNING, JobRunState.State.NEW, false},
          {JobRunState.State.NEW, JobRunState.State.ABORTED, false},
          {JobRunState.State.FAILED, JobRunState.State.COMPLETED, false},
          {JobRunState.State.COMPLETED, JobRunState.State.NEW, false},
          {JobRunState.State.NEW, JobRunState.State.NEW, false},
          {JobRunState.State.COMPLETED, JobRunState.State.COMPLETED, false}
        });
  }

  public JobRunStateTransitionTest(
      JobRunState.State startingState, JobRunState.State endingState, boolean validTransition) {
    this.startingState = startingState;
    this.endingState = endingState;
    this.validTransition = validTransition;
  }

  @Test
  public void testTransitions() {
    assertThat(validTransition.equals(JobRun.isValidJobTransition(startingState, endingState)));
  }
}
