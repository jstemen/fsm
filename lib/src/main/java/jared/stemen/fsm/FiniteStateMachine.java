package jared.stemen.fsm;

public interface FiniteStateMachine<STATE, EVENT> {
  FiniteStateMachine<STATE, EVENT> link(Link<STATE, EVENT> builder);

  STATE performEvent(EVENT event);

  STATE getState();
}
