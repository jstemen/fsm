package jared.stemen.fsm;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class FSM<STATE, EVENT> {
  private STATE state;
  private EVENT event;

  private final List<LinkBuilder<STATE, EVENT>> linkBuilders = new ArrayList<>();

  public FSM(STATE state) {
    this.state = state;
  }

  public FSM<STATE, EVENT> link(LinkBuilder<STATE, EVENT> builder) {
    linkBuilders.add(builder);
    return this;
  }

  public STATE performEvent(EVENT doorEvent) {
    return state;
  }
}
