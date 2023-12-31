package feign;

import client.MsaFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;

@EnableFeignClients
@EnableConfigurationProperties(TestProperties.class)
@SpringBootApplication
public class FeignExampleApplication {
    public static void main(String[] args) {
        SpringApplication.run(FeignExampleApplication.class, args);
    }

    @Autowired
    TestProperties properties;
    @Autowired
    TestFeignClient testFeignClient;

    @Bean
    public CommandLineRunner feignRunner() {
        return args -> {
            System.out.println(properties.getConfig());
            System.out.println(testFeignClient.test());
        };
    }
}
