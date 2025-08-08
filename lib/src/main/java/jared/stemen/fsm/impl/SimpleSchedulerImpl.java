package jared.stemen.fsm.impl;

import java.time.Duration;
import java.time.Instant;
import java.util.PriorityQueue;

import jared.stemen.fsm.FiniteStateMachine;
import jared.stemen.fsm.SimpleScheduler;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
public class SimpleSchedulerImpl<STATE, EVENT> implements SimpleScheduler {

  private final PriorityQueue<Task<STATE, EVENT>> pq = new PriorityQueue<>();

  // todo the single thread should outsource task to a worker thread pool to reduce the impact of
  // long running tasks from blocking other tasks
  public SimpleSchedulerImpl() {
    Thread t =
        new Thread(
            () -> {
              while (true) {
                synchronized (SimpleSchedulerImpl.this) {
                  while (!pq.isEmpty() && !pq.peek().due.isBefore(Instant.now())) {
                    Task<STATE, EVENT> task = pq.poll();

                    log.info(
                        "Performing delayed event {} in state {}",
                        task.getDelayedEvent(),
                        task.getOriginalState());
                    val fsm = task.getFsm();
                    synchronized (fsm) {
                      if (fsm.getState().equals(task.getOriginalState())) {
                        task.getFsm().performEvent(task.getDelayedEvent());
                      } else {
                        log.debug(
                            "Delayed event {} in state {} was ignored because the FSM was not in original state",
                            task.getDelayedEvent(),
                            task.getOriginalState());
                      }
                    }
                  }
                }

                log.info("Waiting for next delayed event");
                try {
                  Thread.sleep(1000L);
                } catch (InterruptedException e) {
                  log.error("Scheduler thread interrupted", e);
                }
              }
            });
    t.start();
  }

  @Data
  public static class Task<STATE, EVENT> implements Comparable<Task<STATE, EVENT>> {
    @NonNull private final FiniteStateMachine<STATE, EVENT> fsm;
    @NonNull private final STATE originalState;
    @NonNull private final EVENT delayedEvent;
    @NonNull private Instant due;

    @Override
    public int compareTo(Task o) {
      return due.compareTo(o.due);
    }
  }

  public synchronized void schedule(
      STATE originalState, EVENT event, Duration delay, FiniteStateMachine<STATE, EVENT> fsm) {
    Instant due = Instant.now().plus(delay);
    pq.offer(new Task<>(fsm, originalState, event, due));
  }
}
