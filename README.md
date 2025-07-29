# Finite State Machine Framework

This project implements a flexible, reusable Finite State Machine (FSM) framework in Java. The framework allows for defining states, transitions, and actions using Java's functional programming features.

## Overview

A Finite State Machine consists of:

1. **States**: Represent different modes a system can be in
2. **Events**: Inputs that trigger state transitions
3. **Transitions**: Define valid movements between states based on events
4. **Actions**: Optional operations executed during transitions

This framework is designed to be generic and type-safe, allowing you to define your own state and event types.

## Features

- Generic state and event types
- Dynamic transition definitions
- Support for multiple actions per transition
- Fluent API design
- Graceful handling of invalid transitions
- Comprehensive test coverage

## Project Structure

```
lib/src/main/java/jared/stemen/fsm/
├── FiniteStateMachine.java   # Core interface for the FSM
├── Link.java                 # Interface for state transitions
└── impl/
    ├── FiniteStateMachineImpl.java  # Implementation of the FSM
    ├── LinkImpl.java                # Implementation of transitions
    └── StateAndActions.java         # Helper class for state transitions
```

## How to Use

### 1. Define Your States and Events

First, define your state and event types. Enums work well for this purpose:

```java
public enum DoorState {
    OPEN, CLOSED, LOCKED
}

public enum DoorEvent {
    OPEN_DOOR, CLOSE_DOOR, LOCK_DOOR, UNLOCK_DOOR
}
```

### 2. Create a Finite State Machine Instance

Create an instance of the FSM with an initial state:

```java
FiniteStateMachine<DoorState, DoorEvent> doorController = 
    new FiniteStateMachineImpl<>(DoorState.CLOSED);
```

### 3. Define Transitions

Use the fluent API to define state transitions:

```java
doorController
    .link(
        LinkImpl.<DoorState, DoorEvent>builder()
            .sourceState(DoorState.CLOSED)
            .targetState(DoorState.OPEN)
            .event(DoorEvent.OPEN_DOOR)
            .action(() -> System.out.println("Door is now open"))
            .build()
    )
    .link(
        LinkImpl.<DoorState, DoorEvent>builder()
            .sourceState(DoorState.OPEN)
            .targetState(DoorState.CLOSED)
            .event(DoorEvent.CLOSE_DOOR)
            .action(() -> System.out.println("Door is now closed"))
            .build()
    )
    .link(
        LinkImpl.<DoorState, DoorEvent>builder()
            .sourceState(DoorState.CLOSED)
            .targetState(DoorState.LOCKED)
            .event(DoorEvent.LOCK_DOOR)
            .action(() -> System.out.println("Door is now locked"))
            .build()
    )
    .link(
        LinkImpl.<DoorState, DoorEvent>builder()
            .sourceState(DoorState.LOCKED)
            .targetState(DoorState.CLOSED)
            .event(DoorEvent.UNLOCK_DOOR)
            .action(() -> System.out.println("Door is now unlocked"))
            .build()
    );
```

### 4. Use the State Machine

Trigger transitions by performing events:

```java
// Current state is CLOSED
DoorState newState = doorController.performEvent(DoorEvent.OPEN_DOOR);
// newState is now OPEN, and the action is executed

// Try to perform an invalid transition
try {
    doorController.performEvent(DoorEvent.UNLOCK_DOOR); // Will throw exception
} catch (IllegalStateException e) {
    System.out.println("Cannot unlock an open door!");
}
```

## Example Implementation

The project includes a sample Door Controller implementation demonstrating the framework functionality with basic states (OPEN, CLOSED, LOCKED) and events (OPEN_DOOR, CLOSE_DOOR, LOCK_DOOR, UNLOCK_DOOR).

Additionally, a more complex Laundry Machine example is included in the tests, showcasing a multi-state workflow with various transitions and actions.

## Build and Run

This project uses Gradle as its build system.

### Prerequisites

- Java 21 or higher
- Gradle (wrapper included)

### Building the Project

```bash
./gradlew build
```

### Running Tests

```bash
./gradlew test
```

### Generate Test Coverage Report

```bash
./gradlew jacocoTestReport
```

The report will be available in `lib/build/reports/jacoco/test/html/index.html`

## Dependencies

- Lombok - For reducing boilerplate code
- JUnit 5 - For testing
- AssertJ - For fluent assertions
- SLF4J & Logback - For logging

## Design Decisions

1. **Generic Type Parameters**: The framework uses generic types for states and events, allowing users to define their own domain-specific types.

2. **Builder Pattern**: LinkImpl uses the builder pattern for a fluent, readable API when configuring transitions.

3. **Functional Actions**: Actions are defined as Runnable instances, allowing for lambda expressions and method references.

4. **Immutable Objects**: Link implementations are immutable to ensure thread safety.

5. **Fail-Fast Validation**: The framework throws exceptions for invalid transitions and duplicate configurations, making errors evident early.

## Development History

This project was developed incrementally with a test-driven approach.  Please see the git history for more details, but at a high level, the author did the following:

1. **Initial Setup**: 
   - Created Java project structure with Gradle
   - Added core dependencies (Lombok, JUnit, Mockito, AssertJ)
   - Configured build settings with Spotless and Google Java Format

2. **DSL Planning**:
   - Developed initial test cases demonstrating the planned API usage

3. **Backfilled the test with real logic**: 
   - Created the classes to make the test compile
   - Then added missing functionality to make the test pass

4. **Testing & Refinement**:
      - Created comprehensive test suite for all components
      - Added JaCoCo for code coverage tracking
      - Expanded door FSM test to validate all transitions

4. **Architecture Improvements (Day 1)**:
   - Refactored FSM to use interface-based design
   - Extracted StateAndActions as a separate class
   - Added more complex Laundry Machine example
   - Improved package organization for better separation

## AI Assistance

This project was enhanced with the assistance of Claude 3.7, an AI assistant from Anthropic. 
