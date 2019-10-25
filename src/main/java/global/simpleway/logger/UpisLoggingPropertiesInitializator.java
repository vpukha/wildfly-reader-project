package global.simpleway.logger;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.core.async.AsyncLoggerContextSelector;
import org.apache.logging.log4j.core.config.plugins.util.PluginManager;

public class UpisLoggingPropertiesInitializator {
	private static final String LOG4J2_ASYNC_QUEUE_FULL_POLICY = "log4j2.asyncQueueFullPolicy"; // Discard
	private static final String LOG4J2_ASYNC_QUEUE_FULL_DISCARD_THRESHOLD = "log4j2.discardThreshold"; // FATAL - discard everything

	private static final String LOG4J2_ASYNC_LOGGER_WAIT_STRATEGY = "log4j2.asyncLoggerWaitStrategy"; // Timeout
	private static final String LOG4J2_ASYNC_LOGGER_RING_BUFFER_SIZE = "log4j2.asyncLoggerRingBufferSize"; // 256*1024
	private static final String LOG4J2_CONTEXT_SELECTOR = "log4j2.contextSelector"; // org.apache.logging.log4j.core.async.AsyncLoggerContextSelector
	private static final String MAIL_SMTP_STARTTLS_ENADLE = "mail.smtp.starttls.enable";

	public static void init() {
		System.setProperty(LOG4J2_ASYNC_QUEUE_FULL_POLICY, PropertyUtils.getSystemOrEnvString(LOG4J2_ASYNC_QUEUE_FULL_POLICY, "Discard"));
		System.setProperty(LOG4J2_ASYNC_QUEUE_FULL_DISCARD_THRESHOLD, PropertyUtils.getSystemOrEnvString(LOG4J2_ASYNC_QUEUE_FULL_DISCARD_THRESHOLD, "FATAL"));
		System.setProperty(LOG4J2_ASYNC_LOGGER_WAIT_STRATEGY, PropertyUtils.getSystemOrEnvString(LOG4J2_ASYNC_LOGGER_WAIT_STRATEGY, "Timeout"));
		System.setProperty(LOG4J2_ASYNC_LOGGER_RING_BUFFER_SIZE, PropertyUtils.getSystemOrEnvString(LOG4J2_ASYNC_LOGGER_RING_BUFFER_SIZE, Integer.toString(16 * 1024)));
		System.setProperty(LOG4J2_CONTEXT_SELECTOR, PropertyUtils.getSystemOrEnvString(LOG4J2_CONTEXT_SELECTOR, AsyncLoggerContextSelector.class.getName()));

		PluginManager.addPackages(Arrays.asList("global.simpleway.logger"));
	}

	public static Map<String, String> getPropertiesAsMap() {
		Map<String, String> properties = new HashMap<>();

		properties.put(LOG4J2_ASYNC_QUEUE_FULL_POLICY, "" + getAsyncQueueFullPolicy());
		properties.put(LOG4J2_ASYNC_QUEUE_FULL_DISCARD_THRESHOLD, "" + getAsyncQueueFullDiscardThreshold());
		properties.put(LOG4J2_ASYNC_LOGGER_WAIT_STRATEGY, "" + getAsyncLoggerWaitStrategy());
		properties.put(LOG4J2_ASYNC_LOGGER_RING_BUFFER_SIZE, "" + getAsyncLoggerRingBufferSize());
		properties.put(LOG4J2_CONTEXT_SELECTOR, "" + getContextSelector());
		properties.put(MAIL_SMTP_STARTTLS_ENADLE, "" + isMailSmtpStartTlsEnabled());

		return properties;
	}

	public static String getAsyncQueueFullPolicy() {
		return PropertyUtils.getSystemOrEnvString(LOG4J2_ASYNC_QUEUE_FULL_POLICY);
	}

	public static String getAsyncQueueFullDiscardThreshold() {
		return PropertyUtils.getSystemOrEnvString(LOG4J2_ASYNC_QUEUE_FULL_DISCARD_THRESHOLD);
	}

	public static String getAsyncLoggerWaitStrategy() {
		return PropertyUtils.getSystemOrEnvString(LOG4J2_ASYNC_LOGGER_WAIT_STRATEGY);
	}

	public static String getAsyncLoggerRingBufferSize() {
		return PropertyUtils.getSystemOrEnvString(LOG4J2_ASYNC_LOGGER_RING_BUFFER_SIZE);
	}

	public static String getContextSelector() {
		return PropertyUtils.getSystemOrEnvString(LOG4J2_CONTEXT_SELECTOR);
	}

	public static boolean isMailSmtpStartTlsEnabled() {
		return PropertyUtils.getSystemOrEnvBoolean(MAIL_SMTP_STARTTLS_ENADLE);
	}
}
