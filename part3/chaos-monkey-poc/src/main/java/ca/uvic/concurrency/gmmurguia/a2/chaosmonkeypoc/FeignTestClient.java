package ca.uvic.concurrency.gmmurguia.a2.chaosmonkeypoc;

import feign.RequestLine;
import feign.hystrix.FallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;

@FeignClient(value = "string", fallbackFactory = FeignTestClient.HystrixClientFallbackFactory.class, url = "http://localhost:1234")
public interface FeignTestClient {

    @RequestLine("GET /get-string")
    String getString();

    @Component
    class HystrixClientFallbackFactory implements FallbackFactory<FeignTestClient> {
        @Override
        public FeignTestClient create(Throwable cause) {
            return () -> "Fallback";
        }
    }

}
