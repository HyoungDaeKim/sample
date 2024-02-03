package client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Logger;
import feign.slf4j.Slf4jLogger;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.FeignClientBuilder;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;

@Slf4j
@Setter
public class FeignClientAdaptor extends AbstractClientAdaptor {
    private MsaFeignClient msaFeignClient;
    private ApplicationContext applicationContext;
    private ObjectFactory<HttpMessageConverters> messageConverters;

    public FeignClientAdaptor(ApplicationContext applicationContext, ObjectFactory<HttpMessageConverters> messageConverters) {
        this(applicationContext, messageConverters, "default");
    }

    public FeignClientAdaptor(ApplicationContext applicationContext, ObjectFactory<HttpMessageConverters> messageConverters, String name) {
        /*CircuitBreaker circuitBreaker = CircuitBreaker.ofDefaults("sample");
        circuitBreaker.getEventPublisher().onEvent(event -> log.info("State change {}", event));;
        RateLimiter rateLimiter = RateLimiter.ofDefaults("sample");
        FeignDecorators decorators = FeignDecorators.builder()
                                         .withRateLimiter(rateLimiter)
                                         .withCircuitBreaker(circuitBreaker)
                                         .build();
        msaFeignClient = Resilience4jFeign.builder(decorators)
                .logger(new Slf4jLogger(MsaFeignClient.class))
                .logLevel(Logger.Level.FULL)
                .encoder(new SpringEncoder(messageConverters))
                .decoder(new SpringDecoder(messageConverters))
                .target(Target.EmptyTarget.create(MsaFeignClient.class));*/

        this.applicationContext = applicationContext;
        this.messageConverters = messageConverters;
        msaFeignClient = new FeignClientBuilder(applicationContext).forType(MsaFeignClient.class, name)
                .customize(builder -> {
                    builder.logLevel(Logger.Level.FULL);
                    builder.logger(new Slf4jLogger(MsaFeignClient.class));
                    builder.encoder(new SpringEncoder(messageConverters));
                    builder.decoder(new SpringDecoder(messageConverters));
                })
                .fallback(MsaFeignClient.FallbackTest.class)
                .build();
    }

    public FeignClientAdaptor create(String name) {
        return new FeignClientAdaptor(this.applicationContext, this.messageConverters, name);
    }

    @Override
    public RequestInfo<?> msa(String msa) {
        Assert.notNull(getUriMap(), "'uriMap' not null!");
        Assert.hasText(msa, "'msa' must not be emptys!");
        return new FeignClientRequestInfo(msaFeignClient, getUriMap().get(msa));
    }

    @Override
    public RequestInfo<?> baseUrl(String baseUrl) {
        return new FeignClientRequestInfo(msaFeignClient, baseUrl);
    }

    @Getter
    @Setter
    static class FeignClientRequestInfo extends AbstractRequestInfo<FeignClientRequestInfo> {
        private MsaFeignClient delegator;
        private String baseUrl;
        private Object param;
        private String uri;
        private HttpMethod httpMethod;
        private ObjectMapper mapper = new ObjectMapper();

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

        protected String retrieveString() {
            String url = baseUrl;
            if (!url.endsWith("/")) {
                url += uri;
            }
            else {
                url = url.substring(0, url.length()-1) + uri;
            }
            URI uri;
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
        public <T> T retrieveTo(Class<T> type) {
            String value = retrieveString();
            return mapper.convertValue(value, type);
        }

        @Override
        public <T> T retrieveTo() {
            T result;
            String value = retrieveString();
            ParameterizedTypeReference<T> bodyType = new ParameterizedTypeReference<T>() {};
            TypeReference<?> tr = new TypeReference<>() {
                public Type getType() {
                    return bodyType.getType();
                }
            };
            try {
                result = (T) mapper.readValue(value, tr);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            return result;
        }
    }
}
