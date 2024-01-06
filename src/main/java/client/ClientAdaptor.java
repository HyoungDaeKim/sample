package client;

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
        String retrieve();
        <T> T retreiveTo(Class<T> type);
        <T> T retrieveTo(ParameterizedTypeReference<T> bodyType);
    }
}
