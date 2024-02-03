package client;

import feign.Target;
import org.springframework.cloud.openfeign.CircuitBreakerNameResolver;

import java.lang.reflect.Method;

public class FeignClientNameCircuitBreakerNameResolver implements CircuitBreakerNameResolver {
    @Override
    public String resolveCircuitBreakerName(String feignClientName, Target<?> target, Method method) {
        return feignClientName;
    }
}
