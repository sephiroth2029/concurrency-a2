package ca.uvic.concurrency.gmmurguia.a2.byzantinegens;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@RequiredArgsConstructor
public class Message implements Comparable<Message> {

    @NonNull
    private Order input;

    @NonNull
    private List<Integer> path;

    private Order output;

    private List<Message> children = new LinkedList<>();

    @Override
    public int compareTo(Message o) {
        return path.toString().compareTo(o.path.toString());
    }

    public void addChild(Message child) {
        children.add(child);
    }

    public void obtainOutput() {
        HashMap<Order, AtomicInteger> counters = new HashMap<>();
        Order output = null;
        int max = 0;

        for (Order order : Order.values()) {
            counters.put(order, new AtomicInteger());
        }

        for (Message child : children) {
            AtomicInteger counter = counters.get(child.output);
            if (counter != null) {
                int current = counter.incrementAndGet();
                if (max < current) {
                    max = current;
                    output = child.getInput();
                }
            }
        }

        this.output = output;
    }

    public Optional<Message> findParent(Queue<Message> messages) {
        LinkedList<Integer> parentPath = new LinkedList<>(path);
        parentPath.removeLast();
        return messages.stream().filter(msg->msg.path.equals(parentPath)).findFirst();
    }

    public List<Integer> getParentPath() {
        LinkedList<Integer> parentPath = new LinkedList<>(path);
        parentPath.removeLast();
        return parentPath;
    }
}
