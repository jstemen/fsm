package jared.stemen.fsm;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

@AllArgsConstructor
@Data
class StateAndActions<STATE> {
  @NonNull private final STATE state;
  @NonNull private final List<Runnable> actions;
}
