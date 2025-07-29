package jared.stemen.fsm.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

class LinkImplTest {

  private enum TestState {
    STATE_A,
    STATE_B
  }

  private enum TestEvent {
    EVENT_1,
    EVENT_2
  }

  @Test
  void shouldBuildLinkWithRequiredParameters() {
    // Given
    TestState sourceState = TestState.STATE_A;
    TestState targetState = TestState.STATE_B;
    TestEvent event = TestEvent.EVENT_1;

    // When
    LinkImpl<TestState, TestEvent> linkImpl =
        LinkImpl.<TestState, TestEvent>builder()
            .sourceState(sourceState)
            .targetState(targetState)
            .event(event)
            .build();

    // Then
    assertThat(linkImpl.getSourceState()).isEqualTo(sourceState);
    assertThat(linkImpl.getTargetState()).isEqualTo(targetState);
    assertThat(linkImpl.getEvent()).isEqualTo(event);
    assertThat(linkImpl.getActions()).isNotNull().isEmpty();
  }

  @Test
  void shouldBuildLinkWithSingleAction() {
    // Given
    TestState sourceState = TestState.STATE_A;
    TestState targetState = TestState.STATE_B;
    TestEvent event = TestEvent.EVENT_1;
    AtomicInteger counter = new AtomicInteger(0);
    Runnable action = counter::incrementAndGet;

    // When
    LinkImpl<TestState, TestEvent> linkImpl =
        LinkImpl.<TestState, TestEvent>builder()
            .sourceState(sourceState)
            .targetState(targetState)
            .event(event)
            .action(action)
            .build();

    // Then
    assertThat(linkImpl.getSourceState()).isEqualTo(sourceState);
    assertThat(linkImpl.getTargetState()).isEqualTo(targetState);
    assertThat(linkImpl.getEvent()).isEqualTo(event);
    assertThat(linkImpl.getActions()).hasSize(1);

    // When actions are executed
    linkImpl.getActions().forEach(Runnable::run);

    // Then counter should be incremented
    assertThat(counter.get()).isEqualTo(1);
  }

  @Test
  void shouldBuildLinkWithMultipleActions() {
    // Given
    TestState sourceState = TestState.STATE_A;
    TestState targetState = TestState.STATE_B;
    TestEvent event = TestEvent.EVENT_1;
    AtomicInteger counter1 = new AtomicInteger(0);
    AtomicInteger counter2 = new AtomicInteger(10);

    // When
    LinkImpl<TestState, TestEvent> linkImpl =
        LinkImpl.<TestState, TestEvent>builder()
            .sourceState(sourceState)
            .targetState(targetState)
            .event(event)
            .action(counter1::incrementAndGet)
            .action(counter2::incrementAndGet)
            .build();

    // Then
    assertThat(linkImpl.getSourceState()).isEqualTo(sourceState);
    assertThat(linkImpl.getTargetState()).isEqualTo(targetState);
    assertThat(linkImpl.getEvent()).isEqualTo(event);
    assertThat(linkImpl.getActions()).hasSize(2);

    // When actions are executed
    linkImpl.getActions().forEach(Runnable::run);

    // Then counters should be incremented
    assertThat(counter1.get()).isEqualTo(1);
    assertThat(counter2.get()).isEqualTo(11);
  }

  @Test
  void shouldBuildLinkWithActionsList() {
    // Given
    TestState sourceState = TestState.STATE_A;
    TestState targetState = TestState.STATE_B;
    TestEvent event = TestEvent.EVENT_1;
    List<Runnable> actionsList = new ArrayList<>();
    AtomicInteger counter1 = new AtomicInteger(0);
    AtomicInteger counter2 = new AtomicInteger(10);
    actionsList.add(counter1::incrementAndGet);
    actionsList.add(counter2::incrementAndGet);

    // When
    LinkImpl<TestState, TestEvent> linkImpl =
        LinkImpl.<TestState, TestEvent>builder()
            .sourceState(sourceState)
            .targetState(targetState)
            .event(event)
            .actions(actionsList)
            .build();

    // Then
    assertThat(linkImpl.getSourceState()).isEqualTo(sourceState);
    assertThat(linkImpl.getTargetState()).isEqualTo(targetState);
    assertThat(linkImpl.getEvent()).isEqualTo(event);
    assertThat(linkImpl.getActions()).hasSize(2);

    // When actions are executed
    linkImpl.getActions().forEach(Runnable::run);

    // Then counters should be incremented
    assertThat(counter1.get()).isEqualTo(1);
    assertThat(counter2.get()).isEqualTo(11);
  }

  @Test
  void shouldThrowExceptionWhenSourceStateIsNull() {
    // Given
    TestState targetState = TestState.STATE_B;
    TestEvent event = TestEvent.EVENT_1;

    // When/Then
    assertThatThrownBy(
            () ->
                LinkImpl.<TestState, TestEvent>builder()
                    .sourceState(null)
                    .targetState(targetState)
                    .event(event)
                    .build())
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("sourceState");
  }

  @Test
  void shouldThrowExceptionWhenTargetStateIsNull() {
    // Given
    TestState sourceState = TestState.STATE_A;
    TestEvent event = TestEvent.EVENT_1;

    // When/Then
    assertThatThrownBy(
            () ->
                LinkImpl.<TestState, TestEvent>builder()
                    .sourceState(sourceState)
                    .targetState(null)
                    .event(event)
                    .build())
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("targetState");
  }

  @Test
  void shouldThrowExceptionWhenEventIsNull() {
    // Given
    TestState sourceState = TestState.STATE_A;
    TestState targetState = TestState.STATE_B;

    // When/Then
    assertThatThrownBy(
            () ->
                LinkImpl.<TestState, TestEvent>builder()
                    .sourceState(sourceState)
                    .targetState(targetState)
                    .event(null)
                    .build())
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("event");
  }
}
