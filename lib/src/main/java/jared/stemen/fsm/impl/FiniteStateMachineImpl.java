package jared.stemen.fsm.impl;

import java.util.HashMap;
import java.util.Map;

import jared.stemen.fsm.FiniteStateMachine;
import jared.stemen.fsm.Link;
import lombok.*;

public class FiniteStateMachineImpl<STATE, EVENT> implements FiniteStateMachine<STATE, EVENT> {
  @Getter @NonNull private STATE state;

  private final Map<STATE, Map<EVENT, StateAndActions<STATE>>> stateTransitionsMap =
      new HashMap<>();

  /**
   * Creates a new Finite State Machine with the specified initial state.
   *
   * <p>The FSM is initialized with no transition links. Links must be added using the {@link
   * #link(Link) link} method before events can be processed.
   *
   * @param state The initial state of the FSM
   * @throws NullPointerException if the provided state is null
   */
  public FiniteStateMachineImpl(STATE state) {
    this.state = state;
  }

  @Override
  public FiniteStateMachine<STATE, EVENT> link(Link<STATE, EVENT> builder) {
    val eventToStateActions =
        stateTransitionsMap.computeIfAbsent(builder.getSourceState(), (k) -> new HashMap<>());
    if (eventToStateActions.containsKey(builder.getEvent())) {
      throw new IllegalStateException(
          "Event %s already linked to state %s"
              .formatted(
                  builder.getEvent(), eventToStateActions.get(builder.getEvent()).getState()));
    }
    eventToStateActions.put(
        builder.getEvent(), new StateAndActions<>(builder.getTargetState(), builder.getActions()));
    return this;
  }

  @Override
  public STATE performEvent(EVENT event) {
    val eventToStateActions = stateTransitionsMap.getOrDefault(state, Map.of());
    val stateAndActions = eventToStateActions.get(event);
    if (stateAndActions == null) {
      throw new IllegalStateException(
          "%s is not a legal event for state %s legal events for this state are: %s"
              .formatted(event, state, eventToStateActions.keySet()));
    }
    stateAndActions.getActions().forEach(Runnable::run);
    state = stateAndActions.getState();
    return state;
  }
}
