package global.simpleway.wildfly;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.StreamSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.stereotype.Component;


@Component
public class Connector {

	private final static Logger logger = LoggerFactory.getLogger(Connector.class);

	@Autowired
	private AbstractEnvironment environment;

	@EventListener(ApplicationReadyEvent.class)
	public void doSomethingAfterStartup() throws Exception {
		Logger logger = LoggerFactory.getLogger(WildflyReaderApplication.class);


		while (true) {
			try {
				connectAndSubscribe();
			} catch (Exception e) {
				logger.warn("Uncaught exception occurred, trying again .... ", e);
				Thread.sleep(10_0000);
			}
		}
	}

	private static void connectAndSubscribe() throws Exception {

		WildflyReaderProperties properties = new WildflyReaderProperties().getInstance();

		List<String> sources = properties.wildFlyTopic;
		List<String> destinations = properties.activeMqQueue;
		if (sources.size() != destinations.size()) {
			throw new IllegalStateException("sources.size() != destinations.size()"); //TODO
		}

		for (int i = 0; i < sources.size(); i++) {
			String sourceTopic = sources.get(i);
			String destinationTopic = destinations.get(i);
			logger.info("Trying to make connection: from {} to {} ", sourceTopic, destinationTopic);
			MessageReceiver messageReceiver = new MessageReceiver();
			messageReceiver.wildFlySubscribe(sourceTopic, destinationTopic);
		}



		//we want to have high severity
		LoggerFactory.getLogger(WildflyReaderApplication.class).warn("Wildfly-reader server started");
	}

	@EventListener
	private void handleContextRefresh(ContextRefreshedEvent event) {
		final Environment env = event.getApplicationContext().getEnvironment();
		logger.info("====== Environment and configuration ======");
		logger.info("Active profiles: {}", Arrays.toString(env.getActiveProfiles()));
		final MutablePropertySources sources = ((AbstractEnvironment) env).getPropertySources();
		StreamSupport.stream(sources.spliterator(), false)
				.filter(ps -> ps instanceof EnumerablePropertySource)
				.map(ps -> ((EnumerablePropertySource) ps).getPropertyNames())
				.flatMap(Arrays::stream)
				.distinct()
				.filter(prop -> !(prop.contains("credentials") || prop.contains("password")))
				.forEach(prop -> logger.info("{}: {}", prop, env.getProperty(prop)));
		logger.info("===========================================");
	}

}
