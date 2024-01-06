package client;

import feign.RequestLine;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.net.URI;
import java.util.Map;

public interface MsaFeignClient {
    @RequestLine(value = "GET")
    public String get(URI uri);
    @RequestLine(value = "POST")
    public String post(URI uri, Object param);
    @RequestLine(value = "DELETE")
    public String delete(URI uri, Object param);
}
