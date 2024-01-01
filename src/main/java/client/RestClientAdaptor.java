package client;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;

import java.util.Map;

@RequiredArgsConstructor
public class RestClientAdaptor extends AbstractClientAdaptor {
    private final RestClient.Builder restClientBuilder;

    public RequestInfo<?> msa(String msa) {
        restClientBuilder.baseUrl(getUriMap().get(msa));
        return new RestClientRequestInfo(restClientBuilder.build());
    }

    @Getter
    @Setter
    static class RestClientRequestInfo extends AbstractRequestInfo<RestClientRequestInfo> {
        private RestClient delegator;
        private HttpHeaders headers;
        private RestClient.RequestBodyUriSpec requestBodyUriSpec;

        public RestClientRequestInfo(RestClient delegator) {
            this.delegator = delegator;
        }
        @Override
        public RestClientRequestInfo get() {
            this.requestBodyUriSpec = (RestClient.RequestBodyUriSpec)delegator.get();
            return this;
        }
        @Override
        public RestClientRequestInfo post() {
            this.requestBodyUriSpec = delegator.post();
            return this;
        }
        @Override
        public RestClientRequestInfo uri(String uri) {
            requestBodyUriSpec.uri(uri);
            return this;
        }
        @Override
        public RestClientRequestInfo header(String headerName, String... headerValues) {
            requestBodyUriSpec.header(headerName, headerValues);
            return this;
        }
        @Override
        public RestClientRequestInfo param(Object param) {
            requestBodyUriSpec.body(param);
            return this;
        }

        @Override
        public Map<String, Object> retrieve() {
            return requestBodyUriSpec.retrieve().body(
                    new ParameterizedTypeReference<>() {
                    }
            );
        }
    }
}
