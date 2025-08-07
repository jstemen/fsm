package jared.stemen.fsm;

public interface Delayed<EVENT> {
  EVENT getEvent();

  java.time.Duration getDuration();
}
