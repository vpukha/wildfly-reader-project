package global.simpleway.wildfly;

import java.util.Arrays;
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
		WildflyReaderProperties properties = WildflyReaderProperties.getInstance();

		List<String> sources = properties.wildFlyTopic;
		List<String> destinations = properties.activeMqQueue;
		if (sources.size() != destinations.size()) {
			throw new IllegalStateException("sources.size() != destinations.size()");
		}

		for (int i = 0; i < sources.size(); i++) {
			String sourceTopic = sources.get(i);
			String destinationTopic = destinations.get(i);
			new Thread(() -> connectAndSubscribeAndRetry(sourceTopic, destinationTopic), "bridge-" + sourceTopic + ":" + destinationTopic).start();
		}
		logger.warn("Wildfly-reader server started");
	}

	private void connectAndSubscribeAndRetry(String sourceTopic, String destinationTopic) {
		while (true) {
			try {
				logger.info("Trying to make connection: from {} to {} ", sourceTopic, destinationTopic);
				WildFlyReceiver wildFlyReceiver = new WildFlyReceiver(sourceTopic);
				ActiveMQSender activeMQSender = new ActiveMQSender(destinationTopic);
				Pipeline pipeline = new Pipeline(wildFlyReceiver, activeMQSender);
				pipeline.connectAndBlock();
			} catch (Exception e) {
				logger.warn("Uncaught exception occurred, trying again .... ", e);
				try {
					Thread.sleep(10_000);
				} catch (InterruptedException e1) {
					logger.info("Sleep interrupted ", e1);
				}
			}
		}
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
