package jared.stemen.fsm.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jared.stemen.fsm.FiniteStateMachine;

@ExtendWith(MockitoExtension.class)
class SimpleSchedulerImplTest {

  private enum TestState {
    INITIAL,
    PROCESSING,
    COMPLETED
  }

  private enum TestEvent {
    START,
    FINISH,
    TIMEOUT
  }

  @Mock private ScheduledExecutorService mockExecutor;
  @Mock private FiniteStateMachine<TestState, TestEvent> mockFsm;

  private SimpleSchedulerImpl<TestState, TestEvent> scheduler;

  @BeforeEach
  void setUp() {
    scheduler = new SimpleSchedulerImpl<>(mockExecutor);
  }

  @Test
  void shouldScheduleTaskWithCorrectDelay() {
    // Given
    TestState originalState = TestState.INITIAL;
    TestEvent event = TestEvent.TIMEOUT;
    Duration delay = Duration.ofSeconds(5);

    // When
    scheduler.schedule(originalState, event, delay, mockFsm);

    // Then
    verify(mockExecutor).schedule(any(Runnable.class), eq(5000L), eq(TimeUnit.MILLISECONDS));
  }

  @Test
  void shouldScheduleTaskWithZeroDelay() {
    // Given
    TestState originalState = TestState.INITIAL;
    TestEvent event = TestEvent.START;
    Duration delay = Duration.ZERO;

    // When
    scheduler.schedule(originalState, event, delay, mockFsm);

    // Then
    verify(mockExecutor).schedule(any(Runnable.class), eq(0L), eq(TimeUnit.MILLISECONDS));
  }

  @Test
  void shouldScheduleTaskWithMillisecondPrecision() {
    // Given
    TestState originalState = TestState.INITIAL;
    TestEvent event = TestEvent.TIMEOUT;
    Duration delay = Duration.ofMillis(1500);

    // When
    scheduler.schedule(originalState, event, delay, mockFsm);

    // Then
    verify(mockExecutor).schedule(any(Runnable.class), eq(1500L), eq(TimeUnit.MILLISECONDS));
  }

  @Test
  void shouldPerformEventWhenFsmInOriginalState() {
    // Given
    TestState originalState = TestState.INITIAL;
    TestEvent event = TestEvent.TIMEOUT;
    Duration delay = Duration.ofSeconds(1);

    when(mockFsm.getState()).thenReturn(originalState);
    ArgumentCaptor<Runnable> taskCaptor = ArgumentCaptor.forClass(Runnable.class);

    // When
    scheduler.schedule(originalState, event, delay, mockFsm);
    verify(mockExecutor).schedule(taskCaptor.capture(), anyLong(), any(TimeUnit.class));

    // Execute the scheduled task
    taskCaptor.getValue().run();

    // Then
    verify(mockFsm).getState();
    verify(mockFsm).performEvent(event);
  }

  @Test
  void shouldNotPerformEventWhenFsmInDifferentState() {
    // Given
    TestState originalState = TestState.INITIAL;
    TestState currentState = TestState.PROCESSING;
    TestEvent event = TestEvent.TIMEOUT;
    Duration delay = Duration.ofSeconds(1);

    when(mockFsm.getState()).thenReturn(currentState);
    ArgumentCaptor<Runnable> taskCaptor = ArgumentCaptor.forClass(Runnable.class);

    // When
    scheduler.schedule(originalState, event, delay, mockFsm);
    verify(mockExecutor).schedule(taskCaptor.capture(), anyLong(), any(TimeUnit.class));

    // Execute the scheduled task
    taskCaptor.getValue().run();

    // Then
    verify(mockFsm).getState();
    verify(mockFsm, never()).performEvent(any());
  }

  @Test
  void shouldHandleMultipleScheduledEvents() {
    // Given
    TestState originalState1 = TestState.INITIAL;
    TestState originalState2 = TestState.PROCESSING;
    TestEvent event1 = TestEvent.START;
    TestEvent event2 = TestEvent.FINISH;
    Duration delay1 = Duration.ofSeconds(1);
    Duration delay2 = Duration.ofSeconds(2);

    // When
    scheduler.schedule(originalState1, event1, delay1, mockFsm);
    scheduler.schedule(originalState2, event2, delay2, mockFsm);

    // Then
    verify(mockExecutor).schedule(any(Runnable.class), eq(1000L), eq(TimeUnit.MILLISECONDS));
    verify(mockExecutor).schedule(any(Runnable.class), eq(2000L), eq(TimeUnit.MILLISECONDS));
  }

  @Test
  void shouldHandleExceptionFromFsmPerformEvent() {
    // Given
    TestState originalState = TestState.INITIAL;
    TestEvent event = TestEvent.START;
    Duration delay = Duration.ofSeconds(1);

    when(mockFsm.getState()).thenReturn(originalState);
    doThrow(new RuntimeException("FSM error")).when(mockFsm).performEvent(event);
    ArgumentCaptor<Runnable> taskCaptor = ArgumentCaptor.forClass(Runnable.class);

    // When
    scheduler.schedule(originalState, event, delay, mockFsm);
    verify(mockExecutor).schedule(taskCaptor.capture(), anyLong(), any(TimeUnit.class));

    // Should not throw exception when FSM throws
    taskCaptor.getValue().run();

    // Then
    verify(mockFsm).getState();
    verify(mockFsm).performEvent(event);
  }

  @Test
  void shouldHandleNullStatesAndEvents() {
    // Given
    TestState originalState = null;
    TestEvent event = null;
    Duration delay = Duration.ofSeconds(1);

    when(mockFsm.getState()).thenReturn(null);
    ArgumentCaptor<Runnable> taskCaptor = ArgumentCaptor.forClass(Runnable.class);

    // When
    scheduler.schedule(originalState, event, delay, mockFsm);
    verify(mockExecutor).schedule(taskCaptor.capture(), anyLong(), any(TimeUnit.class));

    // Execute the scheduled task
    taskCaptor.getValue().run();

    // Then
    verify(mockFsm).getState();
    verify(mockFsm).performEvent(null);
  }

  @Test
  void shouldSynchronizeOnFsmDuringExecution() {
    // Given
    TestState originalState = TestState.INITIAL;
    TestEvent event = TestEvent.START;
    Duration delay = Duration.ofSeconds(1);

    when(mockFsm.getState()).thenReturn(originalState);
    ArgumentCaptor<Runnable> taskCaptor = ArgumentCaptor.forClass(Runnable.class);

    // When
    scheduler.schedule(originalState, event, delay, mockFsm);
    verify(mockExecutor).schedule(taskCaptor.capture(), anyLong(), any(TimeUnit.class));

    // Execute the scheduled task
    taskCaptor.getValue().run();

    // Then
    verify(mockFsm).getState();
    verify(mockFsm).performEvent(event);
  }
}
