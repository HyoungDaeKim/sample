package demo.sample;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class RestMessage<T> {
    private T data;
    private String code;
    private String message;
}
