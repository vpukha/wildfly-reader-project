package global.simpleway.wildfly;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.jms.JMSException;
import javax.jms.TextMessage;
import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Pipeline {

	private static final Logger logger = LoggerFactory.getLogger(Pipeline.class);

	private WildFlyReceiver wildFlyReceiver;
	private ActiveMQSender activeMQSender;
	private final AtomicBoolean block = new AtomicBoolean(true);

	public Pipeline(WildFlyReceiver wildFlyReceiver, ActiveMQSender activeMQSender) throws Exception {
		this.wildFlyReceiver = wildFlyReceiver;
		this.activeMQSender = activeMQSender;
		this.wildFlyReceiver.setCloseListener(() -> {
			closeSender();
			block.set(false);
		});
		this.wildFlyReceiver.setMessageListener(msg -> {
			if (msg instanceof TextMessage) {
				TextMessage textMessage = (TextMessage) msg;
				String messageText = null;
				try {
					messageText = textMessage.getText();
					logger.info("Message received: " + messageText);
				} catch (JMSException e) {
					logger.warn("Cannot read text from incoming text message", e);
				}
				if (messageText != null) {
					try {
						activeMQSender.send(textMessage);
						msg.acknowledge();
					} catch (JMSException e) {
						logger.warn("Cannot resend message to activeMQ. Message: {}", messageText, e);
						closeReceiver();
						closeSender();
						block.set(false);
					}
				}
			}
		});
	}

	private void closeReceiver() {
		try {
			wildFlyReceiver.close();
		} catch (JMSException e) {
			logger.warn("Cannot close wildFly connection", e);
		}
	}

	private void closeSender() {
		try {
			activeMQSender.close();
		} catch (JMSException e) {
			logger.warn("Cannot close activeMQ connection", e);
		}
	}

	public void connectAndBlock() throws NamingException, JMSException {
		this.activeMQSender.connect();
		this.wildFlyReceiver.connect();
		while (block.get()) {
			try {
				Thread.sleep(5_000);
			} catch (InterruptedException e) {
				logger.debug("Waked up from sleeping");
			}
		}
		logger.info("Pipeline has ended");
	}

}
