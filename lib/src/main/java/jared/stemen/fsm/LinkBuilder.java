package jared.stemen.fsm;

import java.util.List;

import lombok.*;

@Builder
@Data
public class LinkBuilder<STATE, EVENT> {
  @NonNull private final STATE sourceState;
  @NonNull private final STATE targetState;
  @NonNull private final EVENT event;

  @NonNull @Singular private final List<Runnable> actions;
}
