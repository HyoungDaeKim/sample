package client;

import feign.FeignException;
import feign.RetryableException;

import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;

public class DefaultExceptionRecordFailurePredicate implements Predicate<Throwable> {
    @Override
    public boolean test(Throwable throwable) {
        // occurs in @CircuitBreaker TimeLimiter
        if (throwable instanceof TimeoutException) {
            return true;
        }

        // occurs in @OpenFeign
        if (throwable instanceof RetryableException) {
            return true;
        }

        return throwable instanceof FeignException.FeignServerException;
    }
}
