package jared.stemen.fsm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.val;

@Data
public class FSM<STATE, EVENT> {
  private STATE state;
  private EVENT event;

  @AllArgsConstructor
  @Data
  private static class StateAndActions<STATE> {
    @NonNull private final STATE state;
    @NonNull private final List<Runnable> actions;
  }

  private final Map<STATE, Map<EVENT, StateAndActions<STATE>>> stateMapMap = new HashMap<>();

  public FSM(STATE state) {
    this.state = state;
  }

  public FSM<STATE, EVENT> link(LinkBuilder<STATE, EVENT> builder) {
    val eventToStateActions =
        stateMapMap.computeIfAbsent(builder.getSourceState(), (k) -> new HashMap<>());
    if (eventToStateActions.containsKey(builder.getEvent())) {
      throw new IllegalStateException(
          "Event "
              + builder.getEvent()
              + " already linked to state "
              + eventToStateActions.get(builder.getEvent()).getState());
    }
    eventToStateActions.put(
        builder.getEvent(), new StateAndActions<>(builder.getTargetState(), builder.getActions()));
    return this;
  }

  public STATE performEvent(EVENT event) {
    val eventToStateActions = stateMapMap.getOrDefault(state, Map.of());
    val stateAndActions = eventToStateActions.get(event);
    if (stateAndActions == null) {
      throw new IllegalStateException(
          event
              + " is not a legal event for state "
              + state
              + " legal events for this state are: "
              + eventToStateActions.keySet());
    }
    stateAndActions.getActions().forEach(Runnable::run);
    state = stateAndActions.getState();
    return state;
  }
}
