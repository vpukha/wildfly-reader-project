package global.simpleway.logger;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class LoggerBootstrap {

	public static void init() {
		System.setProperty("application.id", "wildfly-reader");
		System.setProperty("logger.system.started", ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd___HH-mm-ss")));
		UpisLoggingPropertiesInitializator.init();
		SystemOutInterceptor.intercept();
	}
}
