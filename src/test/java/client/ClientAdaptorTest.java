package client;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.springframework.web.client.RestClient;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class ClientAdaptorTest {
    static  Map<String, String> uriMap = new HashMap<>();
    static {
        uriMap.put("pay", "https://randomuser.me");
    }

    @Test
    public void testRestClientAdaptor() throws URISyntaxException {
        RestClientAdaptor adaptor = new RestClientAdaptor(RestClient.builder());
        adaptor.setUriMap(uriMap);
        String r = adaptor
                .msa("pay")
                .post()
                .uri("/api")
                //.param(ImmutableMap.of("nat", "us"))
                .retrieve();
        System.out.println("r = " + r);
    }
    /*@Test
    public void testFeignClientAdaptor() throws URISyntaxException {
        Map<String, Object> param = new HashMap<>();
        param.put("nat", "us");
        FeignClientAdaptor adaptor = new FeignClientAdaptor();
        adaptor.setUriMap(uriMap);
        Map<String, Object> r = adaptor
                .msa("pay")
                .get()
                .uri("/api")
                .param(param)
                .retrieve();
        System.out.println("r = " + r);
    }*/
}