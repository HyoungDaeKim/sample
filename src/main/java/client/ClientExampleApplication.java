package client;

import feign.Contract;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@EnableFeignClients
@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class
})
public class ClientExampleApplication {
    public static void main(String[] args) {
        SpringApplication.run(ClientExampleApplication.class, args);
    }
    static  Map<String, String> uriMap = new HashMap<>();
    static {
        uriMap.put("pay", "https://jsonplaceholder.typicode.com");
    }
    @Bean
    public Contract useFeignAnnotations() {
        return new Contract.Default();
    }
    @Bean
    public FeignClientNameCircuitBreakerNameResolver defaultCircuitBreakerNameResolver() {
        return new FeignClientNameCircuitBreakerNameResolver();
    }
    @Bean
    public MsaFeignClient.FallbackTest fallbackTest() {
        return new MsaFeignClient.FallbackTest();
    }

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private ObjectFactory<HttpMessageConverters> messageConverters;

    @Autowired
    private RestClientAdaptor restClientAdaptor;

    @Bean
    public CommandLineRunner feignRunner() {
        return args -> {
            FeignClientAdaptor feignClientAdaptor = new FeignClientAdaptor(applicationContext, messageConverters);
            feignClientAdaptor.setUriMap(uriMap);
            List<Map> r = feignClientAdaptor
                    .msa("pay")
                    .get()
                    .uri("/albums")
                    .header("contentType", "json")
                    //.param(ImmutableMap.of("nat", "us"))
                    .retrieveTo();
            log.debug("r = " + r);
            log.debug("***********************************");
            List<Map> r1 = restClientAdaptor.baseUrl(uriMap.get("pay")).get().uri("/albums")
                    .retrieveTo();
            log.debug("r1 = " + r1);
        };
    }
}
