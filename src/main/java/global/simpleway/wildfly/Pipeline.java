package global.simpleway.wildfly;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Pipeline {

	private static final Logger log = LoggerFactory.getLogger(Pipeline.class.getName());

	WildFlyReceiver wildFlyReceiver;
	ActiveMQSender activeMQSender;
	MessageConsumer consumer;

	public Pipeline(WildFlyReceiver wildFlyReceiver, ActiveMQSender activeMQSender) throws Exception {
		this.wildFlyReceiver = wildFlyReceiver;
		this.activeMQSender = activeMQSender;
		//this.activeMQSender.activeMqSubscribe(dest);
//		this.wildFlyReceiver.setMessageListener();
		this.wildFlyReceiver.setMessageListener(msg -> {
			if (msg instanceof TextMessage) {
				try {
					log.info("Message received: " + ((TextMessage) msg).getText());
				} catch (JMSException e) {
					log.warn("Cannot read text from incoming text message", e);
				}
				try {
					activeMQSender.send((TextMessage) msg);
				} catch (JMSException e) {

					//TODO hadle reconnect ...
					//probalby disconnect wildFlyReceiver
					try {
						wildFlyReceiver.pauseReceiving();
						//TODO do a activeMQSender#send retry in different thread
					} catch (JMSException e1) {
						log.warn("Cannot pause wildFly subscription", e);
					}
				}
			}
		});
//		this.setListener();
	}

	public void connectAndBlock() throws NamingException, JMSException {
		this.activeMQSender.connect();
		this.wildFlyReceiver.connect();
		while (true) {
			try {
				log.debug("Still running");
				Thread.sleep(60_000);
			} catch (InterruptedException e) {
				log.debug("Waked up from sleeping");
			}
		}
	}

	//
//	public void setListener() throws JMSException {
//		this.consumer = wildFlyReceiver.getConsumer();
//		consumer.setMessageListener((MessageListener) wildFlyReceiver);
//	}

//	public void onMessage(Message msg) {
//		try {
//
//		} catch (JMSException jmse) {
//			jmse.printStackTrace();
//		}
//	}

}
