package global.simpleway.wildfly.config;

import java.util.Formatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigurationLogger {

	private static final Logger log = LoggerFactory.getLogger(ConfigurationLogger.class.getName());

	public static void logConfigurationInfo(SettingsReader reader) {
		Formatter formatter = new Formatter();
		String rowFormat = "  %1$-40s = %2$-80s%n";
		formatter.format("WildFly Reader Server is starting with following configuration:%n");

		reader.configMap().forEach((k,v) -> formatter.format(rowFormat, k,v));
		log.info(formatter.toString());
	}
}
