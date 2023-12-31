package client;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpHeaders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Map;

@Getter
@Setter
public abstract class AbstractClientAdaptor implements ClientAdaptor {
    private Map<String, String> uriMap;

    static abstract class AbstractRequestInfo<R extends AbstractRequestInfo<R>> implements RequestInfo<R> {
        private MultiValueMap<String, String> headers;

        protected MultiValueMap<String, String> getHeaders() {
            if (this.headers == null) {
                this.headers = new LinkedMultiValueMap<>();
            }
            return this.headers;
        }

        @SuppressWarnings("unchecked")
        @Override
        public R header(String headerName, String... headerValues) {
            for (String headerValue : headerValues) {
                getHeaders().add(headerName, headerValue);
            }
            return (R) this;
        }
    }
}
