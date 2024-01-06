package client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@EnableFeignClients
@SpringBootApplication
public class ClientExampleApplication {
    public static void main(String[] args) {
        SpringApplication.run(ClientExampleApplication.class, args);
    }
    static  Map<String, String> uriMap = new HashMap<>();
    static {
        uriMap.put("pay", "https://jsonplaceholder.typicode.com");
    }

    @Autowired
    private ObjectFactory<HttpMessageConverters> messageConverters;

    @Bean
    public CommandLineRunner feignRunner() {
        return args -> {
            FeignClientAdaptor adaptor = new FeignClientAdaptor(messageConverters);
            adaptor.setUriMap(uriMap);
            List<Map> r = adaptor
                    .msa("pay")
                    .get()
                    .uri("/albums")
                    .header("contentType", "json")
                    //.param(ImmutableMap.of("nat", "us"))
                    .retrieveTo(new ParameterizedTypeReference<>() {
                    });
            log.debug("r = " + r);
        };
    }
}
