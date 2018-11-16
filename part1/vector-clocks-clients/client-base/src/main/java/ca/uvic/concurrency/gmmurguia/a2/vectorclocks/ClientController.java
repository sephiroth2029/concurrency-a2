package ca.uvic.concurrency.gmmurguia.a2.vectorclocks;

import ca.uvic.concurrency.gmmurguia.a2.vectorclocks.model.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
public class ClientController {

    private static Logger logger = LoggerFactory.getLogger(ClientController.class);

    @Autowired
    private int index;

    @Autowired
    @LoadBalanced
    private RestTemplate restTemplate;

    private int[] vector = new int[index];

    private List<int[]> history;

    @Async
    @RequestMapping(value = "/config-messages", method = POST, consumes = APPLICATION_JSON_VALUE)
    public void configMessages(@RequestBody Message[] messages) {
        logger.info("Configuring messages, index {}", index);
        vector = new int[Math.max(vector.length, index + 1)];
        history = new ArrayList<>();
        history.add(Arrays.copyOf(vector, vector.length));
        int counter = 0;
        for (Message message : messages) {
            incrementVector();
            int[] currentVector = Arrays.copyOf(vector, vector.length);
            logger.info("Sending [{}]. Vector: {}", message.toString(), currentVector);
            do {
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {}
            } while (++counter != message.getDelay());

            if (!"self".equals(message.getDest())) {
                String url = String.format("http://%s/receive-messages", message.getDest());
                restTemplate.postForObject(url, currentVector, String.class);
            }
        }
    }

    @RequestMapping(value = "/receive-messages", method = POST, consumes = APPLICATION_JSON_VALUE)
    public String receiveMessage(@RequestBody int[] vector) {
        logger.info("Receiving message. Vector: {}, Received vector: {}", this.vector, vector);
        if (this.vector.length < vector.length) {
            this.vector = Arrays.copyOf(this.vector, vector.length);
        }
        for (int i = 0; i < vector.length; i++) {
            if (i != index) {
                this.vector[i] = Math.max(this.vector[i], vector[i]);
            }
        }
        incrementVector();
        logger.info("Vector at exit: {}", this.vector);

        return Arrays.toString(this.vector);
    }

    @RequestMapping(value = "/get-vector", method = GET)
    public int[] getVector() {
        return vector;
    }

    @RequestMapping(value = "/get-history", method = GET)
    public List<int[]> getHistory() {
        return history;
    }

    public void incrementVector() {
        vector[index] += 1;
        history.add(Arrays.copyOf(vector, vector.length));
        logger.info(history.toString());
    }

}
