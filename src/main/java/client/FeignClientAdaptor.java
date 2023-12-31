package client;

import feign.Feign;
import feign.Target;
import feign.form.FormEncoder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

@Setter
public class FeignClientAdaptor extends AbstractClientAdaptor {
    private MsaFeignClient msaFeignClient;

    public FeignClientAdaptor(ObjectFactory<HttpMessageConverters> messageConverters) {
        msaFeignClient = Feign.builder()
                .encoder(new SpringEncoder(messageConverters))
                .target(Target.HardCodedTarget.EmptyTarget.create(MsaFeignClient.class));
    }

    @Override
    public RequestInfo<?> msa(String msa) {
        return new FeignClientRequestInfo(msaFeignClient, getUriMap().get(msa));
    }

    @Getter
    @Setter
    static class FeignClientRequestInfo extends AbstractRequestInfo<FeignClientRequestInfo> {
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
        public FeignClientRequestInfo post() {
            this.httpMethod = HttpMethod.POST;
            return this;
        }

        @Override
        public FeignClientRequestInfo param(Object param) {
            this.param = param;
            return this;
        }

        @Override
        public Map<String, Object> retrieve() throws URISyntaxException {
            Map<String, Object> result;
            String url = baseUrl;
            if (!url.endsWith("/")) {
                url += uri;
            }
            else {
                url = url.substring(0, url.length()-1) + uri;
            }
            URI _uri = new URI(url);
            if (httpMethod == HttpMethod.GET) {
                result = delegator.get(_uri, param);
            }
            else if (httpMethod == HttpMethod.POST) {
                result = delegator.post(_uri, param);
            }
            else if (httpMethod == HttpMethod.DELETE) {
                result = delegator.delete(_uri, param);
            }
            else {
                result = delegator.post(_uri, param);
            }
            return result;
        }
    }
}
