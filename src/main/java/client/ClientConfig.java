package client;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class ClientConfig {
    @Bean
    public RestClientAdaptor restClientAdaptor(RestClient.Builder restClientBuilder) {
        return new RestClientAdaptor(restClientBuilder);
    }

}
