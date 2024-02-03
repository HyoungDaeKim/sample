package config.ds;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Map;

@Getter
@ConfigurationProperties(prefix = "multiple-datasource")
public class MultipleDatasourceProperties {
    @Setter
    private String test;
    @Setter
    private List<DatasourceConfig> datasourceConfig;

    @Getter
    @Setter
    @ToString
    public static class DatasourceConfig {
        private String name;
        private Map<String, String> datasource;
        private Mybatis mybatis;

        public String getBeanName(BeanType beanType) {
            return this.name + beanType.getSuffix();
        }
    }

    @Getter
    @Setter
    @ToString
    public static class Mybatis {
        private String configLocation;
        private String mapperLocations;
        private String basePackage;
        private String annotationClass;
    }

    public enum BeanType {
      DATASOURCE("DataSource"),
      SESSION_FACTORY("SessionFactory"),
      SESSION_TEMPLATE("SessionTemplate"),
      TRANSACTION_MANAGER("TransactionManager"),
      MAPPER_SCANNER("MapperScanner");

      private final String suffix;

      BeanType(String suffix) {
        this.suffix = suffix;
      }

      public String getSuffix() {
        return this.suffix;
      }

    }
}
