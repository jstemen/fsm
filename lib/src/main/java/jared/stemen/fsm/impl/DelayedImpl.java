package jared.stemen.fsm.impl;

import java.time.Duration;

import jared.stemen.fsm.Delayed;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Builder
@AllArgsConstructor
@Data
public class DelayedImpl<EVENT> implements Delayed<EVENT> {
  @NonNull private final EVENT event;
  @NonNull private final Duration duration;
}
