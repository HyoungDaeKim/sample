package prop;

import client.ClientExampleApplication;
import client.FeignClientAdaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;

import java.util.List;
import java.util.Map;

@EnableConfigurationProperties(PropProperties.class)
@SpringBootApplication
public class PropApplication {
    public static void main(String[] args) {
        SpringApplication.run(PropApplication.class, args);
    }

    @Autowired
    PropProperties properties;

    @Bean
    public CommandLineRunner feignRunner() {
        return args -> {
            System.out.println("properties = " + properties.getTest());
        };
    }
}
