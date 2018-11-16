package ca.uvic.concurrency.gmmurguia.a2.vectorclockstester.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class Test {

//    private List<Client> clients;

    private List<OrderedEvent> events;

}
