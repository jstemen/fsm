package jared.stemen.fsm;

/**
 * Represents a Finite State Machine (FSM) that manages state transitions based on events.
 *
 * <p>A Finite State Machine maintains a current state and allows transitions between states based
 * on defined links. Each link defines how an event, when received in a specific state, transitions
 * the machine to a target state and optionally executes associated actions.
 *
 * @param <STATE> The type representing the states in this FSM
 * @param <EVENT> The type representing the events that can trigger state transitions
 */
public interface FiniteStateMachine<STATE, EVENT> {
  /**
   * Adds a new transition link to this FSM.
   *
   * <p>A link defines a valid transition from a source state to a target state, triggered by a
   * specific event, and optionally executes one or more actions during the transition.
   *
   * @param link The link defining the transition to add
   * @return This FSM instance, allowing for method chaining
   * @throws IllegalStateException If a link with the same source state and event already exists
   */
  FiniteStateMachine<STATE, EVENT> link(Link<STATE, EVENT> link);

  /**
   * Performs a state transition in response to the specified event.
   *
   * <p>If the current state has a valid transition for the given event, the FSM will:
   *
   * <ol>
   *   <li>Execute all actions associated with the transition
   *   <li>Change the current state to the target state
   *   <li>Return the new state
   * </ol>
   *
   * <p>Note on exception handling: If any of the actions (Runnables) throws an exception during
   * execution, that exception will be logged but will not interrupt the transition. The FSM will
   * continue executing any remaining actions and will complete the state transition as defined.
   * This ensures that one failing action does not prevent the entire transition from occurring.
   *
   * @param event The event to process
   * @return The new state after the transition
   * @throws IllegalStateException If the event is not valid for the current state
   */
  STATE performEvent(EVENT event);

  /**
   * Gets the current state of this FSM.
   *
   * @return The current state
   */
  STATE getState();
}
