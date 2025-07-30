package jared.stemen.fsm;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import jared.stemen.fsm.impl.FiniteStateMachineImpl;
import jared.stemen.fsm.impl.LinkImpl;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FailedActionContinuationTest {

  private enum TestState {
    INITIAL,
    INTERMEDIATE,
    FINAL
  }

  private enum TestEvent {
    EVENT_WITH_FAILING_ACTION,
    NORMAL_EVENT
  }

  @Test
  @DisplayName(
      "Should continue executing actions and complete state transition when one action fails")
  void shouldContinueExecutionWhenActionFails() {
    // Given
    AtomicInteger firstActionExecuted = new AtomicInteger(0);
    AtomicBoolean failingActionCalled = new AtomicBoolean(false);
    AtomicInteger lastActionExecuted = new AtomicInteger(0);
    AtomicInteger stateTransitionCounter = new AtomicInteger(0);

    FiniteStateMachine<TestState, TestEvent> fsm = new FiniteStateMachineImpl<>(TestState.INITIAL);

    fsm.link(
        LinkImpl.<TestState, TestEvent>builder()
            .sourceState(TestState.INITIAL)
            .targetState(TestState.INTERMEDIATE)
            .event(TestEvent.EVENT_WITH_FAILING_ACTION)
            .action(
                () -> {
                  log.info("First action executed");
                  firstActionExecuted.incrementAndGet();
                })
            .action(
                () -> {
                  log.info("Failing action called");
                  failingActionCalled.set(true);
                  throw new RuntimeException("This action deliberately fails");
                })
            .action(
                () -> {
                  log.info("Last action executed");
                  lastActionExecuted.incrementAndGet();
                })
            .build());

    fsm.link(
        LinkImpl.<TestState, TestEvent>builder()
            .sourceState(TestState.INTERMEDIATE)
            .targetState(TestState.FINAL)
            .event(TestEvent.NORMAL_EVENT)
            .action(
                () -> {
                  log.info("State transition action executed");
                  stateTransitionCounter.incrementAndGet();
                })
            .build());

    // When
    TestState newState = fsm.performEvent(TestEvent.EVENT_WITH_FAILING_ACTION);

    // Then
    assertThat(newState).isEqualTo(TestState.INTERMEDIATE);
    assertThat(fsm.getState()).isEqualTo(TestState.INTERMEDIATE);
    assertThat(firstActionExecuted.get()).isEqualTo(1);
    assertThat(failingActionCalled.get()).isTrue();
    assertThat(lastActionExecuted.get()).isEqualTo(1);

    // Verify we can continue with the next transition
    TestState finalState = fsm.performEvent(TestEvent.NORMAL_EVENT);
    assertThat(finalState).isEqualTo(TestState.FINAL);
    assertThat(fsm.getState()).isEqualTo(TestState.FINAL);
    assertThat(stateTransitionCounter.get()).isEqualTo(1);
  }

  @Test
  @DisplayName("Should handle multiple failing actions in the same transition")
  void shouldHandleMultipleFailingActions() {
    // Given
    AtomicInteger successfulActionsCount = new AtomicInteger(0);
    AtomicInteger failingActionsCount = new AtomicInteger(0);

    FiniteStateMachine<TestState, TestEvent> fsm = new FiniteStateMachineImpl<>(TestState.INITIAL);

    fsm.link(
        LinkImpl.<TestState, TestEvent>builder()
            .sourceState(TestState.INITIAL)
            .targetState(TestState.FINAL)
            .event(TestEvent.EVENT_WITH_FAILING_ACTION)
            .action(
                () -> {
                  log.info("First successful action");
                  successfulActionsCount.incrementAndGet();
                })
            .action(
                () -> {
                  log.info("First failing action");
                  failingActionsCount.incrementAndGet();
                  throw new RuntimeException("First failure");
                })
            .action(
                () -> {
                  log.info("Second successful action");
                  successfulActionsCount.incrementAndGet();
                })
            .action(
                () -> {
                  log.info("Second failing action");
                  failingActionsCount.incrementAndGet();
                  throw new IllegalArgumentException("Second failure");
                })
            .action(
                () -> {
                  log.info("Final successful action");
                  successfulActionsCount.incrementAndGet();
                })
            .build());

    // When
    TestState newState = fsm.performEvent(TestEvent.EVENT_WITH_FAILING_ACTION);

    // Then
    assertThat(newState).isEqualTo(TestState.FINAL);
    assertThat(fsm.getState()).isEqualTo(TestState.FINAL);
    assertThat(successfulActionsCount.get()).isEqualTo(3);
    assertThat(failingActionsCount.get()).isEqualTo(2);
  }
}
