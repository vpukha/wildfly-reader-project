package global.simpleway.wildfly;

import static global.simpleway.wildfly.config.Settings.INTERNAL_LOGGER_PREFIX;

import org.slf4j.LoggerFactory;

import global.simpleway.wildfly.config.ConfigurationLogger;
import global.simpleway.wildfly.config.LoggerBootstrap;
import global.simpleway.wildfly.config.Settings;
import global.simpleway.wildfly.config.SettingsReader;

public class Main {
	static {
		LoggerBootstrap.start();
	}
	public static void main(String[] args) throws Exception {
		SettingsReader settingsReader = new SettingsReader();
		Settings settings = settingsReader.read();
		ConfigurationLogger.logConfigurationInfo(settingsReader);

		MessageReceiver messageReceiver = new MessageReceiver(settings);
		messageReceiver.wildFlySubscribe();
		//we want to have high severity
		LoggerFactory.getLogger(INTERNAL_LOGGER_PREFIX + Main.class).warn("Server started");
	}
}
