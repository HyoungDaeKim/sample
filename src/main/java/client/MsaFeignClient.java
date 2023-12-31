package client;

import feign.RequestLine;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.net.URI;
import java.util.Map;

public interface MsaFeignClient {
    @RequestLine("GET")
    public Map<String, Object> get(URI uri, Object param);
    @RequestLine("POST")
    public Map<String, Object> post(URI uri, Object param);
    @RequestLine("DELETE")
    public Map<String, Object> delete(URI uri, Object param);
}
