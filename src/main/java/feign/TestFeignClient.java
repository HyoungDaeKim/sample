package feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

@FeignClient(name = "feign", url = "https://randomuser.me/api/?nat=us")
public interface TestFeignClient {
    @GetMapping
    Map<String, Object> test();
}
