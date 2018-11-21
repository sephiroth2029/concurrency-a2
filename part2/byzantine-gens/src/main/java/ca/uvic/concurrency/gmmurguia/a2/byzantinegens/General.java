package ca.uvic.concurrency.gmmurguia.a2.byzantinegens;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.time.StopWatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;

@Slf4j
@Getter
@Setter
@ToString
@Component
public class General {

    @Value("${concurrency.a2.byz.rounds}")
    private int rounds;

    @Value("${concurrency.a2.byz.traitor}")
    private boolean traitor;

    @Value("${concurrency.a2.byz.total-generals}")
    private int totalGenerals;

    @Value("${concurrency.a2.byz.id}")
    private int id;

    @Value("${concurrency.a2.byz.order}")
    private Order order;

    @Value("${concurrency.a2.byz.max-timeout}")
    private int maxTimeout;

    @Autowired
    @LoadBalanced
    private RestTemplate restTemplate;

    private Queue<Message>[] messagesTree;

    private StopWatch stopWatch;

    private boolean initialTimeout;

    private StringBuilder errors;

    public void start() {
        log.info("[General {}] Starting...", id);
        initMessagesTree();
        stopWatch = new StopWatch();
        stopWatch.start();
        initialTimeout = true;
        errors = new StringBuilder();

        if (isCommander()) {
            log.info("[General {}] I'm commander, sending order ({})", id, order);
            waitForGenerals();
            sendMessage(order, new LinkedList<>());
            log.error("Errors: {}", errors);
        }
    }

    private void waitForGenerals() {
        Boolean[] generalsReady = new Boolean[totalGenerals - 1];

        do {
            for (int i = 1; i < generalsReady.length; i++) {
                if (generalsReady[i - 1] == null) {
                    generalsReady[i - 1] = false;
                }
                String url = format("http://general-%d/heartbeat", i);
                try {
                    restTemplate.getForObject(url, String.class);
                    generalsReady[i - 1] = true;
                } catch (Exception e) {
                    // Ignore, general not up yet
                }
            }
            try {
                // Wait for all generals to be up
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                // Don't care about interruptions
            }
        } while (Arrays.stream(generalsReady).noneMatch(ready->!ready));
    }

    private void initMessagesTree() {
        messagesTree = new Queue[rounds];
        for (int i = 0; i < rounds; i++) {
            messagesTree[i] = new ConcurrentLinkedQueue<>();
        }
    }

    public void sendMessage(@NonNull Order order, @NonNull List<Integer> path) {
        if (!path.contains(id)) {
            Order[] orders = getOrders(order);
            List<Integer> newPath = new LinkedList<>(path);
            newPath.add(id);
            log.debug("[General {}] Sending message. Order: {}, path: {}", id, order, newPath);

            for (int i = 1; i < totalGenerals; i++) {
                Message msg = new Message(orders[i % orders.length], newPath);
                String url = format("http://general-%d/receive-message", i);
                boolean completed = false;
                while (!completed) {
                    try {
                        restTemplate.postForObject(url, msg, String.class);
                        completed = true;
                    } catch (Exception e) {
                        errors.append(e);
                        errors.append(", ");
                        try {
                            TimeUnit.MILLISECONDS.sleep(200);
                        } catch (InterruptedException e1) {}
                    }
                }
            }
        }
    }

    private boolean isCommander() {
        return id == 0;
    }

    private Order[] getOrders(@NonNull Order theOrder) {
        if (!traitor) {
            return new Order[]{theOrder};
        }
        Order[] orders = new Order[Order.values().length];
        int pos = 1;
        for (Order o : Order.values()) {
            if (o == theOrder) {
                orders[0] = o;
            } else {
                orders[pos++] = o;
            }
        }
        return orders;
    }

    public Order evaluateTree() {

        evaluateLeaves(messagesTree[messagesTree.length - 1]);
        for (int i = messagesTree.length - 2; i > 0; i--) {
            evaluateRank(messagesTree[i], messagesTree[i + 1]);
        }
        log.info("[General {}] Tree: {}", id, messagesTree[1].toString());
        if (messagesTree[1] == null) {
            return null;
        }
        return messagesTree[1].peek().getOutput();
    }

    private void evaluateRank(Queue<Message> rankMsgs, Queue<Message> children) {
        Iterator<Message> rankIterator = rankMsgs.stream().sorted().iterator();
        Iterator<Message> childrenIterator = children.stream().sorted().iterator();
        List<Integer> currentParentPath = new ArrayList<>();
        Message currentParent = null;

        while (childrenIterator.hasNext()) {
            Message child = childrenIterator.next();
            if (!currentParentPath.equals(child.getParentPath())) {
                currentParent = rankIterator.next();
                currentParentPath = currentParent.getPath();
            }

            currentParent.addChild(child);
        }

        for (Message msg : rankMsgs) {
            msg.obtainOutput();
        }
    }

    private void evaluateLeaves(Queue<Message> messages) {
        for (Message msg : messages) {
            msg.setOutput(msg.getInput());
        }
    }

    public void storeMessage(@NonNull Message message) {
        int pos = message.getPath().size();
        messagesTree[pos].add(message);
        synchronized (stopWatch) {
            stopWatch.reset();
            stopWatch.start();
        }
    }

    @Scheduled(fixedDelay = 5000)
    public void evaluateSecondPhase() {
        if (stopWatch != null) {
            if (stopWatch.getTime() > maxTimeout) {
                stopWatch.reset();

                if (!initialTimeout) {
                    Order finalOrder = evaluateTree();
                    log.info("[General {}] Decided on {}", id, finalOrder);
                    log.error("Errors: {}", errors.toString());
                } else {
                    initialTimeout = false;
                }
            }
        }
    }
}
