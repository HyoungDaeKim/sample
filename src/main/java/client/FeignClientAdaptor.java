package client;

import feign.Feign;
import feign.Target;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Map;

@Setter
public class FeignClientAdaptor extends AbstractClientAdaptor {
    private MsaFeignClient msaFeignClient;
    @Override
    public RequestInfo<?> msa(String msa) {
        return new FeignClientRequestInfo(msaFeignClient, getUriMap().get(msa));
    }

    @Getter
    @Setter
    static class FeignClientRequestInfo implements RequestInfo<FeignClientAdaptor.FeignClientRequestInfo> {
        private MsaFeignClient delegator;
        private String baseUrl;
        private Object param;
        private String uri;
        private HttpMethod httpMethod;
        private HttpHeaders httpHeaders;

        public FeignClientRequestInfo(MsaFeignClient delegator, String baseUrl) {
            this.delegator = delegator;
            this.baseUrl = baseUrl;
        }

        @Override
        public FeignClientRequestInfo uri(String uri) {
            this.uri = uri;
            return this;
        }

        @Override
        public FeignClientRequestInfo get() {
            this.httpMethod = HttpMethod.GET;
            return this;
        }

        @Override
        public FeignClientRequestInfo put() {
            return null;
        }

        @Override
        public FeignClientRequestInfo delete() {
            return null;
        }

        @Override
        public FeignClientRequestInfo param(Object param) {
            this.param = param;
            return this;
        }

        @Override
        public FeignClientRequestInfo header(String headerName, String... headerValues) {
            return null;
        }

        @Override
        public Map<String, Object> retrieve() throws URISyntaxException {
            Map<String, Object> result = Collections.emptyMap();
            String url = baseUrl;
            if (!url.endsWith("/")) {
                url += "/";
            }
            url += uri;
            URI _uri = new URI(url);
            if (httpMethod == HttpMethod.GET) {
                result = delegator.get(_uri, param);
            }
            return result;
        }
    }
}
