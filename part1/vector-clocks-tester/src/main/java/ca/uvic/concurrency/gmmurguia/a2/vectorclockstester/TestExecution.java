package ca.uvic.concurrency.gmmurguia.a2.vectorclockstester;

import ca.uvic.concurrency.gmmurguia.a2.vectorclockstester.model.Event;
import ca.uvic.concurrency.gmmurguia.a2.vectorclockstester.model.OrderedEvent;
import ca.uvic.concurrency.gmmurguia.a2.vectorclockstester.model.Test;
import lombok.NonNull;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

@Component
@ToString
public class TestExecution {

    private static Logger logger = LoggerFactory.getLogger(TestExecution.class);

    @Autowired
    @LoadBalanced
    private RestTemplate restTemplate;

    @Autowired
    private Yaml yaml;

    public void executeTest(@NonNull String configFilePath) throws FileNotFoundException {
        InputStream inputStream = new FileInputStream(configFilePath);
        Test test = yaml.loadAs(inputStream, Test.class);
        HashSet<String> processed = new HashSet<>();
        boolean allProcessed;
        int maxStart = 0;

        do {
            allProcessed = true;
            String current = null;
            ArrayList<Event> events = new ArrayList<>();
            for (OrderedEvent event : test.getEvents()) {
                if (current == null) {
                    if (!processed.contains(event.getSource())) {
                        allProcessed = false;
                        current = event.getSource();
                        processed.add(current);
                    }
                }

                if (current != null){
                    if (current.equals(event.getSource())) {
                        events.add(new Event(event.getDest(), event.getStart()));
                        maxStart = Math.max(event.getStart(), maxStart);
                    }
                }
            }

            if (!events.isEmpty()) {
                restTemplate.postForObject(getBaseUrl(current) + "config-messages", events, String.class);
            }
        } while (!allProcessed);

        try {
            TimeUnit.SECONDS.sleep(maxStart + 10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (String client : processed) {
            logger.info("{} final vector: {}",
                    client,
                    restTemplate.getForObject(getBaseUrl(client) + "get-history", String.class));
        }
    }

    private String getBaseUrl(String client) {
        return String.format("http://%s/", client);
    }

}
