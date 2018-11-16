package ca.uvic.concurrency.gmmurguia.a2.vectorclocks.clientc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableEurekaClient
@EnableDiscoveryClient
@ComponentScan(basePackages = "ca.uvic.concurrency.gmmurguia.a2.vectorclocks")
public class ClientC {

    public static void main(String[] args) {
        SpringApplication.run(ClientC.class, args);
    }

    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public int index() {
        return 2;
    }

}
