package prop;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties(prefix = "prop")
@Getter
@Setter
public class PropProperties {
    private Map<String, TestProperties> test;
    @Data
    public static class TestProperties {
        private String name;
        private String value;
    }
}
