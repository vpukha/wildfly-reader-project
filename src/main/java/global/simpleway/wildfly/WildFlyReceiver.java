package global.simpleway.wildfly;

import java.util.Properties;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class WildFlyReceiver implements ExceptionListener {
	private static final Logger log = LoggerFactory.getLogger(WildFlyReceiver.class.getName());

	// Set up all the default values
	private static final String DEFAULT_CONNECTION_FACTORY = "jms/RemoteConnectionFactory";
	private static final String INITIAL_CONTEXT_FACTORY = "org.jboss.naming.remote.client.InitialContextFactory";


	private final String fullTopicName;
	private final ConnectionFactory connectionFactory;
	private final Context context;
	private volatile MessageListener messageListener;
	private volatile Connection connection;

	public WildFlyReceiver(String topicName) throws NamingException {
		WildflyReaderProperties properties = new WildflyReaderProperties().getInstance();
		final Properties env = new Properties();
		// Set up the context for the JNDI lookup
		env.put(Context.INITIAL_CONTEXT_FACTORY, INITIAL_CONTEXT_FACTORY);
		env.put(Context.PROVIDER_URL, properties.getWildFlyUrl());
		env.put(Context.SECURITY_PRINCIPAL, properties.getWildFlyUsername());
		env.put(Context.SECURITY_CREDENTIALS, properties.getWildFlyPassword());
		env.put("jboss.naming.client.connect.timeout", "10000");
		log.info("Environment \"" + env.toString() + "\"");
		// Perform the JNDI lookups
		String connectionFactoryString = System.getProperty("connection.factory", DEFAULT_CONNECTION_FACTORY);
		context = new InitialContext(env);
		log.info("Attempting to acquire connection factory \"" + connectionFactoryString + "\"");

		connectionFactory = (ConnectionFactory) context.lookup(connectionFactoryString);
		log.info("Found connection factory \"" + connectionFactoryString + "\" in JNDI");
		this.fullTopicName = "jms/topic/" + topicName;
	}

	/**
	 * must be called before {@link #connect()}
	 */
	public void setMessageListener(MessageListener messageListener) {
		this.messageListener = messageListener;
	}

	//	public MessageConsumer getConsumer() {
//		return consumer;
//	}

//	MessageConsumer consumer = null;

//	public void wildFlySubscribe(String source) throws Exception {
//		 connection = null;
//		 session = null;
//	//	MessageConsumer consumer = null;
//		 destination = null;
//		TextMessage message = null;
//		Context context = null;
//
//
//		//activeMQSender.activeMqSubscribe(dest);
//	}




	public void connect() throws JMSException, NamingException {

//		try {
			log.info("Attempting to acquire destination \"" + fullTopicName + "\"");
			Destination destination = (Destination) context.lookup(fullTopicName);
//			log.info("Found destination \"" + destinationString + "\" in JNDI");
			// Create the JMS connection, session, producer, and consumer
			WildflyReaderProperties properties = new WildflyReaderProperties().getInstance();
			if (connection != null) {
				throw new IllegalStateException("Connection already exists, cannot connect");
			}
			connection = connectionFactory.createConnection(properties.getWildFlyUsername(), properties.getWildFlyPassword());
			Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			//  producer = session.createProducer(destination);
			MessageConsumer consumer = session.createConsumer(destination);

			//	WildFlyReceiver wildFlyReceiver = new WildFlyReceiver();

//			consumer.setMessageListener(this);
			if (messageListener != null) {
				consumer.setMessageListener(messageListener);
			}
//			connection
			connection.setExceptionListener(this);
			connection.start();

			//Close JNDI context
/*
			context.close();
			Thread.sleep(25000);
			if(consumer != null) consumer.close();
			if(session != null) session.close();
			if(connection != null) connection.close();
*/
//		} catch (Exception e) {
//			log.error(String.valueOf(e));
//			throw e;
//		}

	}

	public void pauseReceiving() throws JMSException {
		connection.stop();
	}

	public void continueReceiving() throws JMSException {
		connection.start();
	}

	@Override
	public void onException(JMSException exception) {
		try {
			//TODO test it thread sleep
			Thread.sleep(1_000);
		} catch (InterruptedException e) {
			log.warn("Interrupted from sleeping in exception handler");
		}
		try {
			connection.start();
		} catch (JMSException | RuntimeException e) {
			log.warn("Cannot start connection in exception handler ", e);
		}
	}

	public void close() throws JMSException {
		connection.close();
	}

	//	@Override
//	public void onMessage(Message message) {
//
//	}

/*
	public void onMessage(Message msg) {
		try {
			if (msg instanceof TextMessage) {
				log.info("Message received: " + ((TextMessage) msg).getText());
				//activeMQSender.sendMessage((TextMessage) msg);
			}
		} catch (JMSException jmse) {
			jmse.printStackTrace();
		}
	}
*/
}