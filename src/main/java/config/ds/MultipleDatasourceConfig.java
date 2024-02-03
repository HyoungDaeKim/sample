package config.ds;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.NoSuchElementException;

@Configuration
public class MultipleDatasourceConfig {
    @Bean
   	public BeanDefinitionRegistryPostProcessor postProcessor(Environment environment) {
   		try {
   			return new MultipleDatasourceConfigBeanPostProcessor(
                    Binder.get(environment).bind("multiple-datasource", MultipleDatasourceProperties.class).get()
   			);
   		} catch (NoSuchElementException e) {
   			e.printStackTrace();
   		}
   		return null;
   	}
}
