package client;

import feign.RequestLine;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.net.URI;
import java.util.Map;

@FeignClient(name="msa-api", url = "$")
public interface MsaFeignClient {
    @GetMapping
    public Map<String, Object> get(URI uri, Object param);
    @PostMapping
    public Map<String, Object> post(URI uri, Object param);
    @DeleteMapping
    public Map<String, Object> delete(URI uri, Object param);
}
