package global.simpleway.wildfly;

import java.net.ConnectException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import global.simpleway.logger.LoggerBootstrap;
import global.simpleway.wildfly.config.ConfigurationLogger;
import global.simpleway.wildfly.config.Settings;
import global.simpleway.wildfly.config.SettingsReader;

public class Main {

	static {
		LoggerBootstrap.init();
	}

	public static void main(String[] args) throws InterruptedException {
		Logger logger = LoggerFactory.getLogger(Main.class);
		while (true) {
			try {
				SettingsReader settingsReader = new SettingsReader();
				Settings settings = settingsReader.read();
				ConfigurationLogger.logConfigurationInfo(settingsReader);

				connectAndSubscribe(settings);
			} catch (Exception e) {
				logger.warn("Uncaught exception occurred, trying again .... ", e);
				Thread.sleep(10_000);
			}
		}
	}

	private static void connectAndSubscribe(Settings settings) throws Exception {
		MessageReceiver messageReceiver = new MessageReceiver(settings);
		messageReceiver.wildFlySubscribe();
		//we want to have high severity
		LoggerFactory.getLogger(Main.class).warn("Wildfly-reader server started");
	}
}
