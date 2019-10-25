package global.simpleway.wildfly;

import java.util.Properties;
import java.util.logging.Logger;

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

import global.simpleway.wildfly.config.Settings;
import global.simpleway.wildfly.config.SettingsReader;

public class MessageReceiver implements MessageListener {
	private static final Logger log = Logger.getLogger(MessageReceiver.class.getName());

	// Set up all the default values
	private static final String DEFAULT_CONNECTION_FACTORY = "jms/RemoteConnectionFactory";
	private static final String INITIAL_CONTEXT_FACTORY = "org.jboss.naming.remote.client.InitialContextFactory";

	private final String topic;
	private final String username;
	private final String password;
	private final String providerUrl;

	SettingsReader settingsReader = new SettingsReader();
	Settings settings = settingsReader.read();
	ActiveMQSender activeMQSender = new ActiveMQSender(settings);

	public MessageReceiver(Settings settings) throws JMSException {
		this.topic = "jms/topic/" + settings.wildFlyTopic;
		this.username = settings.wildFlyUsername;
		this.password = settings.wildFlyPassword;
		this.providerUrl = "http-remoting://" + settings.wildFlyUrl;
		activeMQSender.activeMqSubscribe();
	}


	public void wildFlySubscribe() throws Exception {

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
			env.put(Context.PROVIDER_URL, System.getProperty(Context.PROVIDER_URL, providerUrl));
			env.put(Context.SECURITY_PRINCIPAL, System.getProperty("username", username));
			env.put(Context.SECURITY_CREDENTIALS, System.getProperty("password", password));
			env.put("jboss.naming.client.connect.timeout", "10000");

			context = new InitialContext(env);

			// Perform the JNDI lookups
			String connectionFactoryString = System.getProperty("connection.factory", DEFAULT_CONNECTION_FACTORY);
			log.info("Attempting to acquire connection factory \"" + connectionFactoryString + "\"");
			connectionFactory = (ConnectionFactory) context.lookup(connectionFactoryString);
			log.info("Found connection factory \"" + connectionFactoryString + "\" in JNDI");

			String destinationString = System.getProperty("destination", this.topic);
			log.info("Attempting to acquire destination \"" + destinationString + "\"");
			destination = (Destination) context.lookup(destinationString);
			log.info("Found destination \"" + destinationString + "\" in JNDI");

			// Create the JMS connection, session, producer, and consumer
			connection = connectionFactory.createConnection(System.getProperty("username", this.username), System.getProperty("password", password));
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			//  producer = session.createProducer(destination);
			consumer = session.createConsumer(destination);


			SettingsReader settingsReader = new SettingsReader();
			Settings settings = settingsReader.read();
			MessageReceiver messageReceiver = new MessageReceiver(settings);

			consumer.setMessageListener(messageReceiver);
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
			log.severe(e.getMessage());
			throw e;
		}
	}


	public void onMessage(Message msg) {
		try {
			String msgText;
			if (msg instanceof TextMessage) {
					activeMQSender.sendMessage((TextMessage) msg);
				msgText = ((TextMessage) msg).getText();
			} else {
				msgText = msg.toString();
			}
			log.info("Message received: " + msgText);
		} catch (JMSException jmse) {
			jmse.printStackTrace();
		}
	}

}