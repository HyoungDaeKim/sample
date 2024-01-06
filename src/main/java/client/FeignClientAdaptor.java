package client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Feign;
import feign.Logger;
import feign.Target;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Setter
public class FeignClientAdaptor extends AbstractClientAdaptor {
    private MsaFeignClient msaFeignClient;

    public FeignClientAdaptor(ObjectFactory<HttpMessageConverters> messageConverters) {
        msaFeignClient = Feign.builder()
                .logLevel(Logger.Level.FULL)
                .encoder(new SpringEncoder(messageConverters))
                .decoder(new SpringDecoder(messageConverters))
                .target(Target.EmptyTarget.create(MsaFeignClient.class));
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
        public String retrieve() {
            String url = baseUrl;
            if (!url.endsWith("/")) {
                url += uri;
            }
            else {
                url = url.substring(0, url.length()-1) + uri;
            }
            URI uri = null;
            try {
                uri = new URI(url);
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
            String result;
            MultiValueMap<String, String> headers = getHeaders();
            if (httpMethod == HttpMethod.GET) {
                result = delegator.get(uri, headers);
            }
            else if (httpMethod == HttpMethod.POST) {
                result = delegator.post(uri, headers, param);
            }
            else if (httpMethod == HttpMethod.DELETE) {
                result = delegator.delete(uri, headers, param);
            }
            else {
                result = delegator.post(uri, headers, param);
            }
            return result;
        }

        @Override
        public <T> T retrieveTo(ParameterizedTypeReference<T> bodyType) {
            T result;
            String value = retrieve();
            TypeReference<?> tr = new TypeReference<>() {
                public Type getType() {
                    return bodyType.getType();
                }
            };
            ObjectMapper mapper = new ObjectMapper();
            try {
                result = (T) mapper.readValue(value, tr);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            return result;
        }
    }
}
