package client;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.springframework.web.client.RestClient;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class ClientTest {
    RestClientAdaptor adaptor = new RestClientAdaptor(RestClient.builder());

    @Test
    public void testRestClientAdaptor() throws URISyntaxException {
        Map<String, Object> param = new HashMap<>();
        param.put("nat", "us");
        String r = adaptor
                .msa("pay")
                .get()
                .uri("/api")
                .param(param)
                .retrieve();
        System.out.println("r = " + r);
    }
}
