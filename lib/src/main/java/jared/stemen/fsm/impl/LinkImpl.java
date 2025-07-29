package jared.stemen.fsm.impl;

import java.util.List;

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

  @Singular @NonNull private final List<Runnable> actions;
}
