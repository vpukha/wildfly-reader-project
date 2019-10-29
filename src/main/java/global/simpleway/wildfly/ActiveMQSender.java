package global.simpleway.wildfly;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ActiveMQSender {
	private static final Logger log = LoggerFactory.getLogger(ActiveMQSender.class.getName());

	MessageProducer producer;
	private final String fullDestinationTopic;
	private volatile Connection connection;

	public ActiveMQSender(String destinationTopic){
		WildflyReaderProperties properties = new WildflyReaderProperties().getInstance();
		this.fullDestinationTopic = destinationTopic;

	}

	public void connect() throws JMSException {
		WildflyReaderProperties properties = new WildflyReaderProperties().getInstance();
		// Getting JMS connection from the server and starting it
		ConnectionFactory connectionFactory;
		log.info("Establishing ActiveMQ connection to {} ", properties.getActiveMqUrl());

		if (properties.getActiveMqUsername() != null){
			connectionFactory = new ActiveMQConnectionFactory(properties.getActiveMqUsername(), properties.getActiveMqPassword(), "tcp://" + properties.getActiveMqUrl());
		}
		else{
			connectionFactory = new ActiveMQConnectionFactory("tcp://" + properties.getActiveMqUrl());
		}
		if (connection != null) {
			throw new IllegalStateException("Connection already exists, cannot connect");
		}
		connection = connectionFactory.createConnection();
		log.info("Found ActiveMQ connection factory");

		connection.start();
		log.info("ActiveMQ connection started");
		//Creating a non transactional session to send/receive JMS message.
		Session session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
		log.info("ActiveMQ session created");

		Destination destination = session.createQueue(fullDestinationTopic);

		log.info("ActiveMQ connected to queue: {} ", fullDestinationTopic);

		// MessageProducer is used for sending messages to the queue.
		producer = session.createProducer(destination);
		log.info("ActiveMQ producer created");
	}

	public void send(TextMessage msg) throws JMSException {
		producer.send(msg);
		log.info("Message is sent to ActiveMQ: {} ", msg);
	}

	public void close() throws JMSException {
		connection.close();
	}
}