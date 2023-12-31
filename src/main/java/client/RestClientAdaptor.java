package client;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.util.Assert;
import org.springframework.web.client.RestClient;

@RequiredArgsConstructor
public class RestClientAdaptor extends AbstractClientAdaptor {
    private final RestClient.Builder restClientBuilder;

    @Override
    public RequestInfo<?> msa(String msa) {
        Assert.notNull(getUriMap(), "'uriMap' not null!");
        Assert.hasText(msa, "'msa' must not be emptys!");
        restClientBuilder.baseUrl(getUriMap().get(msa));
        return new RestClientRequestInfo(restClientBuilder.build());
    }

    @Override
    public RequestInfo<?> baseUrl(String baseUrl) {
        restClientBuilder.baseUrl(baseUrl);
        return new RestClientRequestInfo(restClientBuilder.build());
    }

    @Getter
    @Setter
    static class RestClientRequestInfo extends AbstractRequestInfo<RestClientRequestInfo> {
        private RestClient delegator;
        private RestClient.RequestHeadersUriSpec<?> requestHeadersUriSpec;
        private Object param;

        public RestClientRequestInfo(RestClient delegator) {
            this.delegator = delegator;
        }
        @Override
        public RestClientRequestInfo get() {
            this.requestHeadersUriSpec = delegator.get();
            return this;
        }
        @Override
        public RestClientRequestInfo post() {
            this.requestHeadersUriSpec = delegator.post();
            return this;
        }
        @Override
        public RestClientRequestInfo uri(String uri) {
            requestHeadersUriSpec.uri(uri);
            return this;
        }
        @Override
        public RestClientRequestInfo header(String headerName, String... headerValues) {
            requestHeadersUriSpec.header(headerName, headerValues);
            return this;
        }
        @Override
        public RestClientRequestInfo param(Object param) {
            this.param = param;
            return this;
        }

        @Override
        public <T> T retrieveTo(Class<T> type) {
            return requestHeadersUriSpec.retrieve().body(type);
        }

        @Override
        public <T> T retrieveTo() {
            return requestHeadersUriSpec.retrieve().body(new ParameterizedTypeReference<>() {
            });
        }
    }
}
