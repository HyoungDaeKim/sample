package config.ds;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.type.DateTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.SqlTimestampTypeHandler;
import org.apache.ibatis.type.StringTypeHandler;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.*;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.util.StringUtils;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

import static config.ds.MultipleDatasourceProperties.BeanType.*;

public class MultipleDatasourceConfigBeanPostProcessor implements BeanDefinitionRegistryPostProcessor {

	private final MultipleDatasourceProperties multipleDatasourceProperties;
	private final Configuration myBatisConfiguration;

	public MultipleDatasourceConfigBeanPostProcessor(MultipleDatasourceProperties multipleDatasourceProperties) {
		this.multipleDatasourceProperties = multipleDatasourceProperties;

		myBatisConfiguration = new Configuration();
		myBatisConfiguration.setMapUnderscoreToCamelCase(true);
		myBatisConfiguration.setLazyLoadingEnabled(true);
		myBatisConfiguration.setMultipleResultSetsEnabled(true);
		myBatisConfiguration.setUseColumnLabel(true);
		myBatisConfiguration.setDefaultExecutorType(ExecutorType.SIMPLE);
		myBatisConfiguration.setCallSettersOnNulls(true);
		myBatisConfiguration.setCacheEnabled(false);
		myBatisConfiguration.setJdbcTypeForNull(JdbcType.NULL);
		myBatisConfiguration.getTypeHandlerRegistry().register(String.class, StringTypeHandler.class);
		myBatisConfiguration.getTypeHandlerRegistry().register(Timestamp.class, SqlTimestampTypeHandler.class);
		myBatisConfiguration.getTypeHandlerRegistry().register(Time.class, DateTypeHandler.class);
		myBatisConfiguration.getTypeHandlerRegistry().register(Date.class, DateTypeHandler.class);

	}

	@Override
	public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry beanFactory) throws BeansException {
		multipleDatasourceProperties.getDatasourceConfig().forEach((v) -> {
			// DataSource 등록
			registerDataSource(beanFactory, v);

			// SqlSessionFactory
			registerSqlSessionFactory(beanFactory, v);

			// SqlSessionTemplate
			registerSqlSessionTemplate(beanFactory, v);

			// TransactionManager
			registerTransactionManager(beanFactory, v);

			// MapperScannerConfigurer
			try {
				registerMapperScanner(beanFactory, v);
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
		});
	}

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {

	}

	private void registerDataSource(
			BeanDefinitionRegistry registry, MultipleDatasourceProperties.DatasourceConfig config
	) {
		// DataSource 등록
		GenericBeanDefinition dataSourceBeanDefinition = new GenericBeanDefinition();
		dataSourceBeanDefinition.setBeanClass(HikariDataSource.class);
		dataSourceBeanDefinition.setPropertyValues(
				new MutablePropertyValues(config.getDatasource())
		);

		registry.registerBeanDefinition(
				config.getBeanName(DATASOURCE),
				dataSourceBeanDefinition
		);
	}

	private void registerSqlSessionFactory(
			BeanDefinitionRegistry registry, MultipleDatasourceProperties.DatasourceConfig config
	) {
		AbstractBeanDefinition sqlSessionFactory = BeanDefinitionBuilder
				.genericBeanDefinition(SqlSessionFactoryBean.class)
				.addPropertyReference("dataSource", config.getBeanName(DATASOURCE))
				.addPropertyValue("configuration", myBatisConfiguration)
				.addPropertyValue("mapperLocations", config.getMybatis().getMapperLocations())
				.getBeanDefinition();

		registry.registerBeanDefinition(
				config.getBeanName(SESSION_FACTORY), sqlSessionFactory
		);
	}

	private void registerSqlSessionTemplate(
			BeanDefinitionRegistry registry, MultipleDatasourceProperties.DatasourceConfig config
	) {
		AbstractBeanDefinition sqlSessionTemplate = BeanDefinitionBuilder
				.genericBeanDefinition(SqlSessionTemplate.class)
				.addConstructorArgReference(config.getBeanName(SESSION_FACTORY))
				.getBeanDefinition();

		registry.registerBeanDefinition(
				config.getBeanName(SESSION_TEMPLATE), sqlSessionTemplate
		);
	}

	private void registerTransactionManager(
			BeanDefinitionRegistry registry, MultipleDatasourceProperties.DatasourceConfig config
	) {
		AbstractBeanDefinition transactionManager = BeanDefinitionBuilder
				.genericBeanDefinition(DataSourceTransactionManager.class)
				.addPropertyReference("dataSource", config.getBeanName(DATASOURCE))
				.getBeanDefinition();

		registry.registerBeanDefinition(
				config.getBeanName(TRANSACTION_MANAGER), transactionManager
		);
	}

	private void registerMapperScanner(
			BeanDefinitionRegistry registry, MultipleDatasourceProperties.DatasourceConfig config
	) throws ClassNotFoundException {
		String annotationClass = config.getMybatis().getAnnotationClass();
		BeanDefinitionBuilder builder = BeanDefinitionBuilder
				.genericBeanDefinition(MapperScannerConfigurer.class)
				.addPropertyValue("sqlSessionFactoryBeanName", config.getBeanName(SESSION_FACTORY))
				.addPropertyValue("basePackage", config.getMybatis().getBasePackage());
		if(StringUtils.hasText(annotationClass)) {
			builder.addPropertyValue("annotationClass", Class.forName(config.getMybatis().getAnnotationClass()));
		}
		AbstractBeanDefinition mapperScannerConfigurer = builder.getBeanDefinition();
		registry.registerBeanDefinition(
				config.getBeanName(MAPPER_SCANNER), mapperScannerConfigurer
		);
	}

}
