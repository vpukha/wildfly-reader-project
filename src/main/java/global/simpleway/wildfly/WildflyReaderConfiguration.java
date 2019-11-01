package global.simpleway.wildfly;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({ WildflyReaderProperties.class })
public class WildflyReaderConfiguration {

	private static final Logger logger = LoggerFactory.getLogger(WildflyReaderConfiguration.class);

	@Autowired
	private WildflyReaderProperties properties; //we need to initialize properties for static usage

}
