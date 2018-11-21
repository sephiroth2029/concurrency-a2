package ca.uvic.concurrency.gmmurguia.a2.byzantinegens;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Objects;
import java.util.Queue;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Slf4j
@RestController
public class GeneralController {

    @Value("${concurrency.a2.byz.rounds}")
    private int rounds;

    @Value("${concurrency.a2.byz.id}")
    private int id;

    @Value("${concurrency.a2.byz.total-generals}")
    private int totalGenerals;

    @Autowired
    private General general;

    @RequestMapping(value = "/heartbeat", method = GET)
    public boolean heartbeat() {
        return true;
    }

    @Async
    @RequestMapping(value = "/receive-message", method = POST, consumes = APPLICATION_JSON_VALUE)
    public String receiveMessage(@RequestBody @NonNull Message message) {
        log.debug("[General {}] Received message: {}", id, message);
        log.debug("[General {}] Rounds: {}", id, rounds);
        if (message.getPath().size() == rounds) {
            if (log.isDebugEnabled()) {
                log.debug("[General {}] Message is at rounds limit. Resulting tree:{}",
                        id, getOrderedTree(general.getMessagesTree()));
            }
        } else {
            general.storeMessage(message);
            general.sendMessage(message.getInput(), Collections.unmodifiableList(message.getPath()));
        }
        return message.toString();
    }

    private String getOrderedTree(Queue<Message>[] messagesTree) {
        StringBuilder sb = new StringBuilder();

        for (Queue<Message> msgs : messagesTree) {
            sb.append(msgs.stream().sorted().map(Objects::toString).collect(Collectors.joining("\n")));
            sb.append("\n");
        }

        return sb.toString();
    }

}
