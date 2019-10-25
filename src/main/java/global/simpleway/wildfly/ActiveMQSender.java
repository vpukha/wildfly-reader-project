package global.simpleway.wildfly;

import java.util.logging.Logger;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;

import global.simpleway.wildfly.config.Settings;

public class ActiveMQSender {
	private static final Logger log = Logger.getLogger(ActiveMQSender.class.getName());

	MessageProducer producer;

	private final String queue;
	private final String username;
	private final String password;
	private final String providerUrl;


	public ActiveMQSender(Settings settings) {
		this.queue = settings.activeMqQueue;
		this.username = settings.activeMqUsername;
		this.password = settings.activeMqPassword;
		this.providerUrl = "tcp://" + settings.activeMqUrl;
	}


	public void activeMqSubscribe() throws JMSException {
		// Getting JMS connection from the server and starting it
		ConnectionFactory connectionFactory;
		log.info("Establishing ActiveMQ connection to " + providerUrl);

		if (username != null){
			connectionFactory = new ActiveMQConnectionFactory(username, password, providerUrl);
		}
		else{
			connectionFactory = new ActiveMQConnectionFactory(providerUrl);
		}
		Connection connection = connectionFactory.createConnection();
		log.info("Found ActiveMQ connection factory");

		connection.start();
		log.info("ActiveMQ connection started");
		//Creating a non transactional session to send/receive JMS message.
		Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		log.info("ActiveMQ session created");

		Destination destination = session.createQueue(queue);
		log.info("ActiveMQ connected to queue:  " +queue);

		// MessageProducer is used for sending messages to the queue.
		producer = session.createProducer(destination);
		log.info("ActiveMQ producer created");
	}

	public void sendMessage(TextMessage msg) throws JMSException {
		producer.send(msg);
		log.info("Message is sent to ActiveMQ:  " +msg);

	}
}