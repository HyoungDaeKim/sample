package config;

import config.ds.MultipleDatasourceConfigBeanPostProcessor;
import config.ds.MultipleDatasourceProperties;
import feign.TestFeignClient;
import feign.TestProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@EnableConfigurationProperties(MultipleDatasourceProperties.class)
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class ConfigApplication {
    public static void main(String[] args) {
        SpringApplication.run(ConfigApplication.class, args);
    }

    @Autowired
    MultipleDatasourceProperties multipleDatasourceProperties;

    @Bean
    public CommandLineRunner feignRunner() {
        return args -> {
            System.out.println(multipleDatasourceProperties.getTest());
            System.out.println(multipleDatasourceProperties.getDatasourceConfig());
        };
    }
}
