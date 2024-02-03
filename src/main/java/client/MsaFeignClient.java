package client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import demo.sample.RestMessage;
import feign.HeaderMap;
import feign.RequestLine;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.MultiValueMap;

import java.net.URI;

public interface MsaFeignClient {
    @RequestLine(value = "GET")
    String get(URI uri, @HeaderMap MultiValueMap<String, String> headers);

    @RequestLine(value = "POST")
    String post(URI uri, @HeaderMap MultiValueMap<String, String> headers, Object param);

    @RequestLine(value = "DELETE")
    String delete(URI uri, @HeaderMap MultiValueMap<String, String> headers, Object param);

    @Slf4j
    class FallbackTest implements MsaFeignClient {
        private ObjectMapper objectMapper = new ObjectMapper();
        @Override
        public String get(URI uri, MultiValueMap<String, String> headers) {
            log.debug("Fallback occurred for get");
            RestMessage<Object> r = RestMessage.builder().code("400").message("get fallback").build();
            try {
                return objectMapper.writeValueAsString(r);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public String post(URI uri, MultiValueMap<String, String> headers, Object param) {
            log.debug("Fallback occurred for get");
            RestMessage<Object> r = RestMessage.builder().code("400").message("post fallback").build();
            try {
                return objectMapper.writeValueAsString(r);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public String delete(URI uri, MultiValueMap<String, String> headers, Object param) {
            log.debug("Fallback occurred for get");
            RestMessage<Object> r = RestMessage.builder().code("400").message("delete fallback").build();
            try {
                return objectMapper.writeValueAsString(r);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
