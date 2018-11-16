package ca.uvic.concurrency.gmmurguia.a2.vectorclockstester;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.web.client.RestTemplate;
import org.yaml.snakeyaml.Yaml;

import java.io.FileNotFoundException;

import static java.lang.System.exit;

@SpringBootApplication
@EnableDiscoveryClient
public class VectorClocksTesterApplication implements CommandLineRunner {

    @Autowired
    private TestExecution testExecution;

    public static void main(String[] args) throws FileNotFoundException {
        SpringApplication.run(VectorClocksTesterApplication.class, args);
    }

    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    @Scope("prototype")
    public Yaml yaml() {
        return new Yaml();
    }

    @Override
    public void run(String... args) throws Exception {
        testExecution.executeTest(args[0]);
        exit(0);
    }
}
