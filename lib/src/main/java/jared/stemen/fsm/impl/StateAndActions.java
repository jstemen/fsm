package jared.stemen.fsm.impl;

import java.util.List;

import jared.stemen.fsm.Delayed;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

@AllArgsConstructor
@Data
class StateAndActions<STATE, EVENT> {
  @NonNull private final STATE state;
  @NonNull private final List<Runnable> actions;
  private final Delayed<EVENT> delayed;

  StateAndActions(STATE state, List<Runnable> actions) {
    this(state, actions, null);
  }
}
