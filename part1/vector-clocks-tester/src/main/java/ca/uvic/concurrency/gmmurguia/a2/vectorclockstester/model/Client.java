package ca.uvic.concurrency.gmmurguia.a2.vectorclockstester.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Client {

    private String name;
    private Event[] events;

}
