package jared.stemen.fsm.impl;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import jared.stemen.fsm.FiniteStateMachine;
import jared.stemen.fsm.SimpleScheduler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@Slf4j
public class SimpleSchedulerImpl<STATE, EVENT> implements SimpleScheduler {

  private final ScheduledExecutorService executor;

  public ScheduledFuture<?> schedule(
      STATE originalState, EVENT event, Duration delay, FiniteStateMachine<STATE, EVENT> fsm) {

    return executor.schedule(
        () -> {
          // In the scheduled task execution
          try {
            synchronized (fsm) {
              if (Objects.equals(fsm.getState(), originalState)) {
                fsm.performEvent(event);
              }
            }
          } catch (Exception e) {
            log.error(
                "Exception during delayed event {} execution in state {}. Event discarded.",
                event,
                originalState,
                e);
            // Don't rethrow - continue scheduler operation
          }
        },
        delay.toMillis(),
        java.util.concurrent.TimeUnit.MILLISECONDS);
  }
}
