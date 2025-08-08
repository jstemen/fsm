package jared.stemen.fsm.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import jared.stemen.fsm.FiniteStateMachine;
import jared.stemen.fsm.Link;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FiniteStateMachineImpl<STATE, EVENT> implements FiniteStateMachine<STATE, EVENT> {
  @NonNull private STATE state;

  @NonNull private final Optional<SimpleSchedulerImpl<STATE, EVENT>> scheduler;

  private final Map<STATE, Map<EVENT, StateAndActions<STATE, EVENT>>> stateTransitionsMap =
      new HashMap<>();

  // priority Queue future scheduled time, other need to track trasnistion
  // on kick state, enqueue the follow up action into priority queue

  // separate thread
  // priority queue. peek() if < current time, then remove item and process
  // else sleep 1 second

  /**
   * Creates a new Finite State Machine with the specified initial state.
   *
   * <p>The FSM is initialized with no transition links. Links must be added using the {@link
   * #link(Link) link} method before events can be processed.
   *
   * @param state The initial state of the FSM
   * @throws NullPointerException if the provided state is null
   */
  public FiniteStateMachineImpl(STATE state, SimpleSchedulerImpl<STATE, EVENT> scheduler) {
    this.state = state;
    this.scheduler = Optional.ofNullable(scheduler);
  }

  public FiniteStateMachineImpl(STATE state) {
    this.state = state;
    this.scheduler = null;
  }

  @Override
  public synchronized FiniteStateMachine<STATE, EVENT> link(Link<STATE, EVENT> link) {
    val eventToStateActions =
        stateTransitionsMap.computeIfAbsent(link.getSourceState(), (k) -> new HashMap<>());
    if (eventToStateActions.containsKey(link.getEvent())) {
      throw new IllegalStateException(
          "Event %s already linked to state %s"
              .formatted(link.getEvent(), eventToStateActions.get(link.getEvent()).getState()));
    }
    eventToStateActions.put(
        link.getEvent(),
        new StateAndActions<>(link.getTargetState(), link.getActions(), link.getDelayedOpt()));
    link.getDelayedOpt()
        .ifPresent(
            delayed -> {
              scheduler.ifPresentOrElse(
                  scheduler -> {
                    if (!stateTransitionsMap
                        .computeIfAbsent(link.getTargetState(), (k) -> new HashMap<>())
                        .containsKey(delayed.getEvent())) {
                      throw new IllegalStateException(
                          "Delayed event %s does not exist for state %s"
                              .formatted(delayed, link.getSourceState()));
                    }
                  },
                  () -> {
                    throw new IllegalStateException(
                        "Scheduler is not initialized, so cannot use delayed events");
                  });
            });
    return this;
  }

  @Override
  public synchronized STATE performEvent(EVENT event) {
    val eventToStateActions = stateTransitionsMap.getOrDefault(state, Map.of());
    val stateAndActions = eventToStateActions.get(event);
    if (stateAndActions == null) {
      throw new IllegalStateException(
          "%s is not a legal event for state %s legal events for this state are: %s"
              .formatted(event, state, eventToStateActions.keySet()));
    }
    stateAndActions
        .getActions()
        .forEach(
            runnable -> {
              try {
                runnable.run();
              } catch (Exception e) {
                log.error(
                    "Exception thrown during action execution for event {} in state {}:. Execution will continue.",
                    event,
                    state,
                    e);
              }
            });
    state = stateAndActions.getState();

    stateAndActions
        .getDelayed()
        .ifPresent(
            delayed -> {
              scheduler.ifPresentOrElse(
                  s -> s.schedule(state, delayed.getEvent(), delayed.getDuration(), this),
                  () -> {
                    throw new IllegalStateException("Scheduler is not initialized");
                  });
            });
    return state;
  }

  @Override
  public synchronized STATE getState() {
    return state;
  }
}
