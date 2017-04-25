package io.pivotal;

import javax.sql.DataSource;

import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.client.ClientCache;
import com.gemstone.gemfire.cache.client.ClientCacheFactory;
import com.gemstone.gemfire.cache.client.ClientRegionFactory;
import com.gemstone.gemfire.cache.client.ClientRegionShortcut;
import com.gemstone.gemfire.pdx.ReflectionBasedAutoSerializer;
import io.pivotal.domain.Customer;
import io.pivotal.spring.cloud.service.gemfire.GemfireServiceConnectorConfig;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.Cloud;
import org.springframework.cloud.CloudFactory;
import org.springframework.cloud.config.java.AbstractCloudConfig;
import org.springframework.cloud.service.ServiceConnectorConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.gemfire.support.GemfireCacheManager;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;


import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Properties;

@Configuration
@EnableCaching
public class DemoConfig extends AbstractCloudConfig {
	
	@Bean
	public DataSource dataSource() {
		DataSource dataSource = connectionFactory().dataSource();
		
		ResourceDatabasePopulator databasePopulator = new ResourceDatabasePopulator();
        databasePopulator.addScript(new ClassPathResource("sql/create_table.sql"));
        
        DatabasePopulatorUtils.execute(databasePopulator, dataSource);
        
        return dataSource;
	}
	
	public ServiceConnectorConfig createGemfireConnectorConfig() {

        GemfireServiceConnectorConfig gemfireConfig = new GemfireServiceConnectorConfig();
        gemfireConfig.setPoolSubscriptionEnabled(true);
        gemfireConfig.setPdxSerializer(new ReflectionBasedAutoSerializer(".*"));
        gemfireConfig.setPdxReadSerialized(false);


        return gemfireConfig;
    }
    
	@Bean(name = "gemfireCache")
    public ClientCache getGemfireClientCache() throws Exception {
		
//		Cloud cloud = new CloudFactory().getCloud();
//
//		ClientCache clientCache = cloud.getSingletonServiceConnector(ClientCache.class,  createGemfireConnectorConfig());

		Properties props = new Properties();
		props.setProperty("security-client-auth-init", "io.pivotal.ClientAuthInitialize.create");
		ClientCacheFactory ccf = new ClientCacheFactory(props);

		ClientCache clientCache = null;
		try {
			List<URI> locatorList = EnvParser.getInstance().getLocators();
			for (URI locator : locatorList) {
				ccf.addPoolLocator(locator.getHost(), locator.getPort());
			}
			clientCache = ccf.create();
		} catch (IOException | URISyntaxException e) {
			throw new RuntimeException("Could not deploy Application", e);
		}
        return clientCache;
    }

	@Bean(name = "gemfireClientCacheFactory")
    public ClientCacheFactory getGemfireClientCacheFactory(){

		Properties props = new Properties();
		props.setProperty("security-client-auth-init", "io.pivotal.ClientAuthInitialize.create");
		ClientCacheFactory ccf = new ClientCacheFactory(props);

		try {
			List<URI> locatorList = EnvParser.getInstance().getLocators();
			for (URI locator : locatorList) {
				ccf.addPoolLocator(locator.getHost(), locator.getPort());
			}
		} catch (IOException | URISyntaxException e) {
			throw new RuntimeException("Could not deploy Application", e);
		}
		return ccf;
	}


	@Bean(name = "customer")
	public Region<String, Customer> customerRegion(@Autowired ClientCache clientCache) {
		ClientRegionFactory<String, Customer> customerRegionFactory = clientCache.createClientRegionFactory(ClientRegionShortcut.PROXY);

		Region<String, Customer> customerRegion = customerRegionFactory.create("customer");

		return customerRegion;
	}
	
	@Bean(name="cacheManager")
	public GemfireCacheManager createGemfireCacheManager(@Autowired ClientCache gemfireCache) {

		GemfireCacheManager gemfireCacheManager = new GemfireCacheManager();
		gemfireCacheManager.setCache((Cache)gemfireCache);

		return gemfireCacheManager;
	}

}
