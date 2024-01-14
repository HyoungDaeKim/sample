package client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
public class ClientTestController {
    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private ObjectFactory<HttpMessageConverters> messageConverters;

    @GetMapping("/test")
    public String test() {
        return "test";
    }
    @GetMapping("/testCall")
    public List<Map> testCall() {
        FeignClientAdaptor feignClientAdaptor = new FeignClientAdaptor(applicationContext, messageConverters, "test");
        feignClientAdaptor.setUriMap(Collections.emptyMap());
        return feignClientAdaptor
                .baseUrl("http://localhost:8080")
                .get()
                .uri("/test")
                .header("contentType", "json")
                //.param(ImmutableMap.of("nat", "us"))
                .retrieveTo();
    }
}
