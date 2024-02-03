package feign;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.openfeign.FeignClientProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@ConfigurationProperties("test")
public class TestProperties {
    private Map<String, TestConfiguration> config = new HashMap<>();

    public Map<String, TestConfiguration> getConfig() {
        return config;
    }

    public void setConfig(Map<String, TestConfiguration> config) {
        this.config = config;
    }

    public static class TestConfiguration {
        private Integer count;
    }
}
