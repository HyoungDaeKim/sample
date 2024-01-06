package client;

import feign.HeaderMap;
import feign.RequestLine;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.net.URI;
import java.util.Map;

public interface MsaFeignClient {
    @RequestLine(value = "GET")
    public String get(URI uri, @HeaderMap Map<String, Object> headers);
    @RequestLine(value = "POST")
    public String post(URI uri, @HeaderMap Map<String, Object> headers, Object param);
    @RequestLine(value = "DELETE")
    public String delete(URI uri, @HeaderMap Map<String, Object> headers, Object param);
}
