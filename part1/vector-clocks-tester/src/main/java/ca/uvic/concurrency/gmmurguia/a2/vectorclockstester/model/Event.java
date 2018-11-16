package ca.uvic.concurrency.gmmurguia.a2.vectorclockstester.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class Event {

    private String dest;

    private int delay;

}
