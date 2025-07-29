package jared.stemen.fsm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;

@Slf4j
class StateAndActionsTest {

  private enum TestState {
    STATE_A,
    STATE_B
  }

  @Test
  void shouldCreateStateAndActionsWithValidParameters() {
    // Given
    TestState state = TestState.STATE_A;
    List<Runnable> actions = new ArrayList<>();
    actions.add(() -> log.info("Action 1"));
    actions.add(() -> log.info("Action 2"));

    // When
    StateAndActions<TestState> stateAndActions = new StateAndActions<>(state, actions);

    // Then
    assertThat(stateAndActions.getState()).isEqualTo(state);
    assertThat(stateAndActions.getActions()).isSameAs(actions);
    assertThat(stateAndActions.getActions()).hasSize(2);
  }

  @Test
  void shouldCreateStateAndActionsWithEmptyActionsList() {
    // Given
    TestState state = TestState.STATE_A;
    List<Runnable> actions = Collections.emptyList();

    // When
    StateAndActions<TestState> stateAndActions = new StateAndActions<>(state, actions);

    // Then
    assertThat(stateAndActions.getState()).isEqualTo(state);
    assertThat(stateAndActions.getActions()).isSameAs(actions);
    assertThat(stateAndActions.getActions()).isEmpty();
  }

  @Test
  void shouldThrowExceptionWhenStateIsNull() {
    // Given
    List<Runnable> actions = new ArrayList<>();

    // When/Then
    assertThatThrownBy(() -> new StateAndActions<>(null, actions))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("state");
  }

  @Test
  void shouldThrowExceptionWhenActionsIsNull() {
    // Given
    TestState state = TestState.STATE_A;

    // When/Then
    assertThatThrownBy(() -> new StateAndActions<>(state, null))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("actions");
  }
}
