package jared.stemen.fsm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FiniteStateMachineTest {

  private enum LaundryState {
    IDLE,
    DOOR_OPEN,
    LOADED,
    WASHING,
    RINSING,
    SPINNING,
    DRYING,
    CYCLE_COMPLETE
  }

  private enum LaundryEvent {
    OPEN_DOOR,
    CLOSE_DOOR,
    SELECT_WASH_CYCLE,
    START,
    WASH_COMPLETE,
    RINSE_COMPLETE,
    SPIN_COMPLETE,
    SELECT_DRY_CYCLE,
    DRY_COMPLETE,
    RESET
  }

  private FiniteStateMachine<LaundryState, LaundryEvent> laundryMachine;
  private AtomicInteger waterLevel;
  private AtomicInteger temperature;
  private AtomicBoolean doorLocked;
  private StringBuilder operationLog;

  @BeforeEach
  void setUp() {
    laundryMachine = new FiniteStateMachineImpl<>(LaundryState.IDLE);
    waterLevel = new AtomicInteger(0);
    temperature = new AtomicInteger(0);
    doorLocked = new AtomicBoolean(false);
    operationLog = new StringBuilder();

    // Configure the laundry machine state transitions
    laundryMachine
        .link(
            LinkBuilder.<LaundryState, LaundryEvent>builder()
                .sourceState(LaundryState.IDLE)
                .targetState(LaundryState.DOOR_OPEN)
                .event(LaundryEvent.OPEN_DOOR)
                .action(() -> operationLog.append("Door opened; "))
                .build())
        .link(
            LinkBuilder.<LaundryState, LaundryEvent>builder()
                .sourceState(LaundryState.DOOR_OPEN)
                .targetState(LaundryState.LOADED)
                .event(LaundryEvent.CLOSE_DOOR)
                .action(() -> operationLog.append("Door closed; "))
                .build())
        .link(
            LinkBuilder.<LaundryState, LaundryEvent>builder()
                .sourceState(LaundryState.LOADED)
                .targetState(LaundryState.WASHING)
                .event(LaundryEvent.SELECT_WASH_CYCLE)
                .action(() -> operationLog.append("Wash cycle selected; "))
                .action(() -> waterLevel.set(50))
                .action(() -> temperature.set(40))
                .action(() -> doorLocked.set(true))
                .build())
        .link(
            LinkBuilder.<LaundryState, LaundryEvent>builder()
                .sourceState(LaundryState.WASHING)
                .targetState(LaundryState.RINSING)
                .event(LaundryEvent.WASH_COMPLETE)
                .action(() -> operationLog.append("Washing complete; "))
                .action(() -> waterLevel.set(60))
                .action(() -> temperature.set(20))
                .build())
        .link(
            LinkBuilder.<LaundryState, LaundryEvent>builder()
                .sourceState(LaundryState.RINSING)
                .targetState(LaundryState.SPINNING)
                .event(LaundryEvent.RINSE_COMPLETE)
                .action(() -> operationLog.append("Rinsing complete; "))
                .action(() -> waterLevel.set(0))
                .build())
        .link(
            LinkBuilder.<LaundryState, LaundryEvent>builder()
                .sourceState(LaundryState.SPINNING)
                .targetState(LaundryState.LOADED)
                .event(LaundryEvent.SPIN_COMPLETE)
                .action(() -> operationLog.append("Spinning complete; "))
                .action(() -> doorLocked.set(false))
                .build())
        .link(
            LinkBuilder.<LaundryState, LaundryEvent>builder()
                .sourceState(LaundryState.LOADED)
                .targetState(LaundryState.DRYING)
                .event(LaundryEvent.SELECT_DRY_CYCLE)
                .action(() -> operationLog.append("Dry cycle selected; "))
                .action(() -> temperature.set(70))
                .action(() -> doorLocked.set(true))
                .build())
        .link(
            LinkBuilder.<LaundryState, LaundryEvent>builder()
                .sourceState(LaundryState.DRYING)
                .targetState(LaundryState.CYCLE_COMPLETE)
                .event(LaundryEvent.DRY_COMPLETE)
                .action(() -> operationLog.append("Drying complete; "))
                .action(() -> temperature.set(25))
                .action(() -> doorLocked.set(false))
                .build())
        .link(
            LinkBuilder.<LaundryState, LaundryEvent>builder()
                .sourceState(LaundryState.CYCLE_COMPLETE)
                .targetState(LaundryState.DOOR_OPEN)
                .event(LaundryEvent.OPEN_DOOR)
                .action(() -> operationLog.append("Door opened after cycle; "))
                .build())
        .link(
            LinkBuilder.<LaundryState, LaundryEvent>builder()
                .sourceState(LaundryState.CYCLE_COMPLETE)
                .targetState(LaundryState.IDLE)
                .event(LaundryEvent.RESET)
                .action(() -> operationLog.append("Machine reset; "))
                .build())
        .link(
            LinkBuilder.<LaundryState, LaundryEvent>builder()
                .sourceState(LaundryState.LOADED)
                .targetState(LaundryState.DOOR_OPEN)
                .event(LaundryEvent.OPEN_DOOR)
                .action(() -> operationLog.append("Door reopened; "))
                .build());
  }

  @Test
  @DisplayName("Machine should start in IDLE state")
  void shouldStartInIdleState() {
    assertThat(laundryMachine.getState()).isEqualTo(LaundryState.IDLE);
  }

  @Test
  @DisplayName("Should transition from IDLE to DOOR_OPEN when door is opened")
  void shouldOpenDoorFromIdleState() {
    // When
    LaundryState newState = laundryMachine.performEvent(LaundryEvent.OPEN_DOOR);

    // Then
    assertThat(newState).isEqualTo(LaundryState.DOOR_OPEN);
    assertThat(laundryMachine.getState()).isEqualTo(LaundryState.DOOR_OPEN);
    assertThat(operationLog.toString()).isEqualTo("Door opened; ");
  }

  @Test
  @DisplayName("Should complete full wash cycle with correct state transitions")
  void shouldCompleteFullWashCycle() {
    // When - Complete wash cycle
    laundryMachine.performEvent(LaundryEvent.OPEN_DOOR);
    laundryMachine.performEvent(LaundryEvent.CLOSE_DOOR);
    laundryMachine.performEvent(LaundryEvent.SELECT_WASH_CYCLE);
    laundryMachine.performEvent(LaundryEvent.WASH_COMPLETE);
    laundryMachine.performEvent(LaundryEvent.RINSE_COMPLETE);
    laundryMachine.performEvent(LaundryEvent.SPIN_COMPLETE);

    // Then
    assertThat(laundryMachine.getState()).isEqualTo(LaundryState.LOADED);
    assertThat(operationLog.toString())
        .isEqualTo(
            "Door opened; Door closed; Wash cycle selected; Washing complete; Rinsing complete; Spinning complete; ");
    assertThat(waterLevel.get()).isEqualTo(0);
    assertThat(temperature.get()).isEqualTo(20);
    assertThat(doorLocked.get()).isFalse();
  }

  @Test
  @DisplayName("Should complete full wash and dry cycle with correct state transitions")
  void shouldCompleteFullWashAndDryCycle() {
    // When - Complete wash and dry cycle
    laundryMachine.performEvent(LaundryEvent.OPEN_DOOR);
    laundryMachine.performEvent(LaundryEvent.CLOSE_DOOR);
    laundryMachine.performEvent(LaundryEvent.SELECT_WASH_CYCLE);
    laundryMachine.performEvent(LaundryEvent.WASH_COMPLETE);
    laundryMachine.performEvent(LaundryEvent.RINSE_COMPLETE);
    laundryMachine.performEvent(LaundryEvent.SPIN_COMPLETE);
    laundryMachine.performEvent(LaundryEvent.SELECT_DRY_CYCLE);
    laundryMachine.performEvent(LaundryEvent.DRY_COMPLETE);

    // Then
    assertThat(laundryMachine.getState()).isEqualTo(LaundryState.CYCLE_COMPLETE);
    assertThat(operationLog.toString()).contains("Wash cycle selected");
    assertThat(operationLog.toString()).contains("Dry cycle selected");
    assertThat(operationLog.toString()).contains("Drying complete");
    assertThat(temperature.get()).isEqualTo(25);
    assertThat(doorLocked.get()).isFalse();
  }

  @Test
  @DisplayName("Should lock door during washing cycle")
  void shouldLockDoorDuringWashingCycle() {
    // When
    laundryMachine.performEvent(LaundryEvent.OPEN_DOOR);
    laundryMachine.performEvent(LaundryEvent.CLOSE_DOOR);

    // Then - Door should be unlocked before wash cycle
    assertThat(doorLocked.get()).isFalse();

    // When
    laundryMachine.performEvent(LaundryEvent.SELECT_WASH_CYCLE);

    // Then - Door should be locked during wash cycle
    assertThat(doorLocked.get()).isTrue();
  }

  @Test
  @DisplayName("Should adjust water level and temperature during wash cycle")
  void shouldAdjustWaterLevelAndTemperatureDuringWashCycle() {
    // When
    laundryMachine.performEvent(LaundryEvent.OPEN_DOOR);
    laundryMachine.performEvent(LaundryEvent.CLOSE_DOOR);
    laundryMachine.performEvent(LaundryEvent.SELECT_WASH_CYCLE);

    // Then - Check initial wash settings
    assertThat(waterLevel.get()).isEqualTo(50);
    assertThat(temperature.get()).isEqualTo(40);

    // When
    laundryMachine.performEvent(LaundryEvent.WASH_COMPLETE);

    // Then - Check rinse settings
    assertThat(waterLevel.get()).isEqualTo(60);
    assertThat(temperature.get()).isEqualTo(20);
  }

  @Test
  @DisplayName("Should allow reopening door after cycle is complete")
  void shouldAllowReopeningDoorAfterCycleComplete() {
    // Given - Complete wash and dry cycle
    laundryMachine.performEvent(LaundryEvent.OPEN_DOOR);
    laundryMachine.performEvent(LaundryEvent.CLOSE_DOOR);
    laundryMachine.performEvent(LaundryEvent.SELECT_WASH_CYCLE);
    laundryMachine.performEvent(LaundryEvent.WASH_COMPLETE);
    laundryMachine.performEvent(LaundryEvent.RINSE_COMPLETE);
    laundryMachine.performEvent(LaundryEvent.SPIN_COMPLETE);
    laundryMachine.performEvent(LaundryEvent.SELECT_DRY_CYCLE);
    laundryMachine.performEvent(LaundryEvent.DRY_COMPLETE);

    // When
    laundryMachine.performEvent(LaundryEvent.OPEN_DOOR);

    // Then
    assertThat(laundryMachine.getState()).isEqualTo(LaundryState.DOOR_OPEN);
    assertThat(operationLog.toString()).contains("Door opened after cycle");
  }

  @Test
  @DisplayName("Should allow resetting machine after cycle is complete")
  void shouldAllowResettingMachineAfterCycleComplete() {
    // Given - Complete wash and dry cycle
    laundryMachine.performEvent(LaundryEvent.OPEN_DOOR);
    laundryMachine.performEvent(LaundryEvent.CLOSE_DOOR);
    laundryMachine.performEvent(LaundryEvent.SELECT_WASH_CYCLE);
    laundryMachine.performEvent(LaundryEvent.WASH_COMPLETE);
    laundryMachine.performEvent(LaundryEvent.RINSE_COMPLETE);
    laundryMachine.performEvent(LaundryEvent.SPIN_COMPLETE);
    laundryMachine.performEvent(LaundryEvent.SELECT_DRY_CYCLE);
    laundryMachine.performEvent(LaundryEvent.DRY_COMPLETE);

    // When
    laundryMachine.performEvent(LaundryEvent.RESET);

    // Then
    assertThat(laundryMachine.getState()).isEqualTo(LaundryState.IDLE);
    assertThat(operationLog.toString()).contains("Machine reset");
  }

  @Test
  @DisplayName("Should throw exception when performing illegal event")
  void shouldThrowExceptionWhenPerformingIllegalEvent() {
    // When/Then - Cannot select wash cycle from IDLE state
    assertThatThrownBy(() -> laundryMachine.performEvent(LaundryEvent.SELECT_WASH_CYCLE))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining(LaundryEvent.SELECT_WASH_CYCLE.toString())
        .hasMessageContaining(LaundryState.IDLE.toString());
  }

  @Test
  @DisplayName("Should throw exception when adding duplicate transition")
  void shouldThrowExceptionWhenAddingDuplicateTransition() {
    // When/Then - Cannot add another OPEN_DOOR transition from IDLE
    assertThatThrownBy(
            () ->
                laundryMachine.link(
                    LinkBuilder.<LaundryState, LaundryEvent>builder()
                        .sourceState(LaundryState.IDLE)
                        .targetState(LaundryState.LOADED) // Different target
                        .event(LaundryEvent.OPEN_DOOR) // Same event already defined
                        .build()))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Event " + LaundryEvent.OPEN_DOOR)
        .hasMessageContaining("already linked");
  }

  @Test
  @DisplayName("Should allow skipping wash and going directly to dry cycle")
  void shouldAllowSkippingWashAndGoingDirectlyToDryCycle() {
    // When - Skip wash cycle, go directly to dry
    laundryMachine.performEvent(LaundryEvent.OPEN_DOOR);
    laundryMachine.performEvent(LaundryEvent.CLOSE_DOOR);
    laundryMachine.performEvent(LaundryEvent.SELECT_DRY_CYCLE);
    laundryMachine.performEvent(LaundryEvent.DRY_COMPLETE);

    // Then
    assertThat(laundryMachine.getState()).isEqualTo(LaundryState.CYCLE_COMPLETE);
    assertThat(operationLog.toString())
        .isEqualTo("Door opened; Door closed; Dry cycle selected; Drying complete; ");
    assertThat(temperature.get()).isEqualTo(25);
  }

  @Test
  @DisplayName("Should allow reopening door after loading but before starting cycle")
  void shouldAllowReopeningDoorAfterLoadingButBeforeStartingCycle() {
    // When
    laundryMachine.performEvent(LaundryEvent.OPEN_DOOR);
    laundryMachine.performEvent(LaundryEvent.CLOSE_DOOR);
    laundryMachine.performEvent(LaundryEvent.OPEN_DOOR);

    // Then
    assertThat(laundryMachine.getState()).isEqualTo(LaundryState.DOOR_OPEN);
    assertThat(operationLog.toString()).isEqualTo("Door opened; Door closed; Door reopened; ");
  }
}
