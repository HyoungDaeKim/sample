package client;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;

import java.util.Map;
@Getter
@Setter
public abstract class AbstractClientAdaptor implements ClientAdaptor {
    @Value("${pybc.urlMap}")
    private Map<String, String> uriMap;
}
