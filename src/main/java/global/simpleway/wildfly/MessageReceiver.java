package global.simpleway.wildfly;

import java.util.Properties;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MessageReceiver implements MessageListener {
	private static final Logger log = LoggerFactory.getLogger(MessageReceiver.class.getName());

	// Set up all the default values
	private static final String DEFAULT_CONNECTION_FACTORY = "jms/RemoteConnectionFactory";
	private static final String INITIAL_CONTEXT_FACTORY = "org.jboss.naming.remote.client.InitialContextFactory";

	WildflyReaderProperties properties = new WildflyReaderProperties().getInstance();
	ActiveMQSender activeMQSender;



	public void wildFlySubscribe(String source, String dest) throws Exception {
		activeMQSender = new ActiveMQSender(dest);
		ConnectionFactory connectionFactory = null;
		Connection connection = null;
		Session session = null;
		MessageProducer producer = null;
		MessageConsumer consumer = null;
		Destination destination = null;
		TextMessage message = null;
		Context context = null;

		try {
			// Set up the context for the JNDI lookup
			final Properties env = new Properties();
			env.put(Context.INITIAL_CONTEXT_FACTORY, INITIAL_CONTEXT_FACTORY);
			env.put(Context.PROVIDER_URL, System.getProperty(Context.PROVIDER_URL, "http-remoting://" + properties.getWildFlyUrl()));
			env.put(Context.SECURITY_PRINCIPAL, System.getProperty("username", properties.getWildFlyUsername()));
			env.put(Context.SECURITY_CREDENTIALS, System.getProperty("password", properties.getWildFlyPassword()));
			env.put("jboss.naming.client.connect.timeout", "10000");
			log.info("Environment \"" + env.toString() + "\"");
			context = new InitialContext(env);

			// Perform the JNDI lookups
			String connectionFactoryString = System.getProperty("connection.factory", DEFAULT_CONNECTION_FACTORY);
			log.info("Attempting to acquire connection factory \"" + connectionFactoryString + "\"");
			connectionFactory = (ConnectionFactory) context.lookup(connectionFactoryString);
			log.info("Found connection factory \"" + connectionFactoryString + "\" in JNDI");

			String destinationString = System.getProperty("destination",  "jms/topic/" + source);
			log.info("Attempting to acquire destination \"" + destinationString + "\"");
			destination = (Destination) context.lookup(destinationString);
			log.info("Found destination \"" + destinationString + "\" in JNDI");

			// Create the JMS connection, session, producer, and consumer
			connection = connectionFactory.createConnection(System.getProperty("username", properties.getWildFlyUsername()), System.getProperty("password", properties.getWildFlyPassword()));
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			//  producer = session.createProducer(destination);
			consumer = session.createConsumer(destination);

			MessageReceiver messageReceiver = new MessageReceiver();

			consumer.setMessageListener(this);
			connection.start();

			//Close JNDI context
/*
			context.close();
			Thread.sleep(25000);
			if(consumer != null) consumer.close();
			if(session != null) session.close();
			if(connection != null) connection.close();
*/
		} catch (Exception e) {
			log.error(String.valueOf(e));
			throw e;
		}
		//activeMQSender.activeMqSubscribe(dest);
	}


	public void onMessage(Message msg) {
		try {
			if (msg instanceof TextMessage) {
				log.info("Message received: " + ((TextMessage) msg).getText());
				activeMQSender.sendMessage((TextMessage) msg);
			}
		} catch (JMSException jmse) {
			jmse.printStackTrace();
		}
	}

}