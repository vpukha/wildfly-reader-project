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
	private static final Logger logger = LoggerFactory.getLogger(WildFlyReceiver.class);

	// Set up all the default values
	private static final String DEFAULT_CONNECTION_FACTORY = "jms/RemoteConnectionFactory";
	private static final String INITIAL_CONTEXT_FACTORY = "org.jboss.naming.remote.client.InitialContextFactory";

	private final String fullTopicName;
	private final ConnectionFactory connectionFactory;
	private final Context context;
	private volatile MessageListener messageListener;
	private volatile Connection connection;
	private Runnable closeListener;

	public WildFlyReceiver(String topicName) throws NamingException {
		WildflyReaderProperties properties = WildflyReaderProperties.getInstance();
		final Properties env = new Properties();
		// Set up the context for the JNDI lookup
		env.put(Context.INITIAL_CONTEXT_FACTORY, INITIAL_CONTEXT_FACTORY);
		env.put(Context.PROVIDER_URL, properties.getWildFlyUrl());
		env.put(Context.SECURITY_PRINCIPAL, properties.getWildFlyUsername());
		env.put(Context.SECURITY_CREDENTIALS, properties.getWildFlyPassword());
		env.put("jboss.naming.client.connect.timeout", "10000");
		logger.info("Environment {}", env.toString());
		// Perform the JNDI lookups
		String connectionFactoryString = System.getProperty("connection.factory", DEFAULT_CONNECTION_FACTORY);
		context = new InitialContext(env);
		logger.info("Attempting to acquire connection factory {}", connectionFactoryString);

		connectionFactory = (ConnectionFactory) context.lookup(connectionFactoryString);
		logger.info("Found connection factory {}", connectionFactoryString, " in JNDI");
		this.fullTopicName = "jms/topic/" + topicName;
	}

	/**
	 * must be called before {@link #connect()}
	 */
	public void setMessageListener(MessageListener messageListener) {
		this.messageListener = messageListener;
	}

	public void setCloseListener(Runnable closeListener) {
		this.closeListener = closeListener;
	}

	public void connect() throws JMSException, NamingException {

		logger.info("Attempting to acquire destination {}", fullTopicName);
		Destination destination = (Destination) context.lookup(fullTopicName);
		// Create the JMS connection, session, producer, and consumer
		WildflyReaderProperties properties = WildflyReaderProperties.getInstance();
		if (connection != null) {
			throw new IllegalStateException("Connection already exists, cannot connect");
		}
		connection = connectionFactory.createConnection(properties.getWildFlyUsername(), properties.getWildFlyPassword());
		Session session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
		MessageConsumer consumer = session.createConsumer(destination);

		if (messageListener != null) {
			consumer.setMessageListener(messageListener);
		}
		connection.setExceptionListener(this);
		connection.start();
	}

	@Override
	public void onException(JMSException exception) {
		try {
			Thread.sleep(1_000);
		} catch (InterruptedException e) {
			logger.warn("Interrupted from sleeping in exception handler");
		}
		try {
			connection.close();
			if (closeListener != null) {
				closeListener.run();
			}
		} catch (JMSException | RuntimeException e) {
			logger.warn("Cannot start connection in exception handler, trying again ", e);

		}
	}

	public void close() throws JMSException {
		connection.close();
	}
}