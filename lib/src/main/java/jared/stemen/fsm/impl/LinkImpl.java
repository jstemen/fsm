package jared.stemen.fsm.impl;

import java.util.List;
import java.util.Optional;

import jared.stemen.fsm.Delayed;
import jared.stemen.fsm.Link;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.Singular;

@Builder
@Data
public class LinkImpl<STATE, EVENT> implements Link<STATE, EVENT> {
  @NonNull private final STATE sourceState;
  @NonNull private final STATE targetState;
  @NonNull private final EVENT event;
  private final Delayed<EVENT> delayed;

  @Singular @NonNull private final List<Runnable> actions;

  @Override
  public Optional<Delayed<EVENT>> getDelayedOpt() {
    return Optional.ofNullable(delayed);
  }
}
