package client;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = ClientExampleApplication.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MyFirstJUnitJupiterTests {
    @Test
    void test() {
        System.out.println("test");
    }

    @Value("${tpd.topic-name}")
    private String topicName;

    @Test
    @Order(1)
    void topicNameTest() {
        System.out.println("topicName = " + topicName);
    }

}
