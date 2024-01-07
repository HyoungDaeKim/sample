package client;

import feign.HeaderMap;
import feign.RequestLine;
import org.springframework.util.MultiValueMap;

import java.net.URI;

public interface MsaFeignClient {
    @RequestLine(value = "GET")
    String get(URI uri, @HeaderMap MultiValueMap<String, String> headers);
    @RequestLine(value = "POST")
    String post(URI uri, @HeaderMap MultiValueMap<String, String> headers, Object param);
    @RequestLine(value = "DELETE")
    String delete(URI uri, @HeaderMap MultiValueMap<String, String> headers, Object param);
}
