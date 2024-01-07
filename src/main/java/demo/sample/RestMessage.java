package demo.sample;

import lombok.Data;

@Data
public class RestMessage<T> {
    private T data;
    private String code;
    private String message;
}
