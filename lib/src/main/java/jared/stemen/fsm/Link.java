package jared.stemen.fsm;

import java.util.List;

/**
 * Represents a transition link in a Finite State Machine (FSM).
 *
 * <p>A link defines how a specific event causes a transition from a source state to a target state,
 * and what actions should be executed during that transition.
 *
 * @param <STATE> The type representing states in the FSM
 * @param <EVENT> The type representing events that can trigger transitions
 */
public interface Link<STATE, EVENT> {
  /**
   * Gets the source state of this transition link.
   *
   * <p>The source state is the state in which the FSM must be for this link to be applicable.
   *
   * @return The source state
   */
  STATE getSourceState();

  /**
   * Gets the target state of this transition link.
   *
   * <p>The target state is the state to which the FSM will transition when this link is followed.
   *
   * @return The target state
   */
  STATE getTargetState();

  /**
   * Gets the event that triggers this transition link.
   *
   * <p>When the FSM receives this event while in the source state, it will follow this link.
   *
   * @return The triggering event
   */
  EVENT getEvent();

  Delayed<EVENT> getDelayed();

  /**
   * Gets the list of actions to execute when this transition link is followed.
   *
   * <p>Actions are executed in the order they appear in the list, before the state changes.
   *
   * @return The list of actions as Runnable objects
   */
  List<Runnable> getActions();
}
