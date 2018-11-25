package ca.uvic.concurrency.gmmurguia.a2.chaosmonkeypoc;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class TestClient {

    @Autowired
    private RestTemplate restTemplate;

    @HystrixCommand(fallbackMethod = "getFallbackString")
    public String getStringFromService() {
        return restTemplate.getForObject("http://127.0.0.1:1234/get-string", String.class);
    }

    public String getFallbackString() {
        return "Sorry, no String available at the moment.";
    }

}
