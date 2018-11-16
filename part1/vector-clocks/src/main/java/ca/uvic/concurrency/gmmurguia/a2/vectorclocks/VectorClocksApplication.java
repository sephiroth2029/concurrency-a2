package ca.uvic.concurrency.gmmurguia.a2.vectorclocks;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class VectorClocksApplication {

    public static void main(String[] args) {
        SpringApplication.run(VectorClocksApplication.class, args);
    }
}
