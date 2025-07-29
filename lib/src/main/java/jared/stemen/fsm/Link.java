package jared.stemen.fsm;

public interface Link<STATE, EVENT> {

  STATE getSourceState();

  STATE getTargetState();

  EVENT getEvent();

  java.util.List<Runnable> getActions();
}
