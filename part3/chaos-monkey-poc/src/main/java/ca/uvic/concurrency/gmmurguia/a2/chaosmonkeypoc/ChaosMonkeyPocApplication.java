package ca.uvic.concurrency.gmmurguia.a2.chaosmonkeypoc;

import feign.Contract;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.cloud.netflix.turbine.EnableTurbine;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableHystrix
@EnableFeignClients
@EnableTurbine
public class ChaosMonkeyPocApplication {

    @Value("${poc.chaosmonkey.console}")
    private boolean console;

    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = SpringApplication.run(ChaosMonkeyPocApplication.class, args);
        ChaosMonkeyPocApplication app = ctx.getBean(ChaosMonkeyPocApplication.class);
        if (app.console) {
            FeignTestClient feignTestClient = ctx.getBean(FeignTestClient.class);

            for (int i = 0; i < 1000; i++) {
                System.out.println(feignTestClient.getString());
            }
        }
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public Contract feignContract() {
        return new Contract.Default();
    }

}
