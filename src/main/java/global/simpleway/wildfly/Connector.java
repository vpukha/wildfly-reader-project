package global.simpleway.wildfly;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.AbstractEnvironment;
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


}
