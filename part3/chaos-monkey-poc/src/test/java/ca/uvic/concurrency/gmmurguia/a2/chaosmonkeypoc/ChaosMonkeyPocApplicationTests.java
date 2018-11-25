package ca.uvic.concurrency.gmmurguia.a2.chaosmonkeypoc;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.LinkedList;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ChaosMonkeyPocApplication.class})
public class ChaosMonkeyPocApplicationTests {

    @Autowired
    private TestClient testClient;

    @Autowired
    private FeignTestClient feignTestClient;

    @Test
    public void contextLoads() {
        LinkedList<String> lst = new LinkedList<>();
        for (int i = 0; i < 1000; i++) {
            lst.add(testClient.getStringFromService());
        }
        System.out.println(lst);
    }

    @Test
    public void feignClient() {
        LinkedList<String> lst = new LinkedList<>();
        for (int i = 0; i < 1000; i++) {
            lst.add(feignTestClient.getString());
        }
        System.out.println(lst);
    }
}
