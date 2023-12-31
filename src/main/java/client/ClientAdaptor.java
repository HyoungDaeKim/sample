package client;

import java.net.URISyntaxException;
import java.util.Map;

public interface ClientAdaptor {
    RequestInfo<?> msa(String msa);

    interface RequestInfo<R extends RequestInfo<R>> {
        R uri(String uri);
        R get();
        R put();
        R delete();
        R param(Object param);
        R header(String headerName, String... headerValues);
        Map<String, Object> retrieve() throws URISyntaxException;
    }
}
