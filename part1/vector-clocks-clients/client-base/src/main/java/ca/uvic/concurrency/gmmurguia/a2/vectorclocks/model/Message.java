package ca.uvic.concurrency.gmmurguia.a2.vectorclocks.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Message {

    private String dest;

    private int delay;

}
