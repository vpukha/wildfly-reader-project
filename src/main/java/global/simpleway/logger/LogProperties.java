package global.simpleway.logger;

import java.util.Map;

public class LogProperties {

	private static final String LOG_SERVER_PORT = "logger.server.port";
	private static final String LOG_SERVER_HOST = "logger.server.host";
	private static final String LOG_SERVER_CHUNK_SIZE = "logger.server.chunk.size";
	private static final String APPLICATION_ID = "application.id";

	public static String getLogServerHost() {
		return PropertyUtils.getSystemOrEnvString(LOG_SERVER_HOST, "localhost");
	}

	public static int getLogServerPort() {
		return PropertyUtils.getSystemOrEnvInteger(LOG_SERVER_PORT, 1111);
	}

	public static int getLogServerChunkSize() {
		return PropertyUtils.getSystemOrEnvInteger(LOG_SERVER_CHUNK_SIZE, 4096);
	}

	public static String getApplicationId() {
		return PropertyUtils.getSystemOrEnvString(APPLICATION_ID, getDefaultApplicationId());
	}

	private static String getComputerName() {
		Map<String, String> env = System.getenv();
		if (env.containsKey("COMPUTERNAME"))
			return env.get("COMPUTERNAME");
		else if (env.containsKey("HOSTNAME"))
			return env.get("HOSTNAME");
		else
			return "DEFAULT-HOST-NAME";
	}

	public static String getDefaultApplicationId() {
		try {
			StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
			String className = stackTrace[stackTrace.length - 1].getClassName();
			String app = className.substring(className.lastIndexOf(".") + 1);
			String computerName = getComputerName();
			return computerName + "-" + app;
		} catch (Exception e) {
			return "DEFAULT-APP-ID";
		}
	}
}
