package client;

import demo.sample.RestMessage;
import org.springframework.core.ParameterizedTypeReference;

import java.lang.reflect.Type;
import java.net.URISyntaxException;

public interface ClientAdaptor {
    RequestInfo<?> msa(String msa);
    RequestInfo<?> baseUrl(String baseUrl);

    interface RequestInfo<R extends RequestInfo<R>> {
        R uri(String uri);
        R get();
        R post();
        R param(Object param);
        R header(String headerName, String... headerValues);
        default <T> RestMessage<T> retrieve() {
            return retrieveTo();
        }
        <T> T retrieveTo(Class<T> type);
        <T> T retrieveTo();
    }
}
