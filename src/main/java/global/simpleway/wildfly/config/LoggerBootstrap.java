package global.simpleway.wildfly.config;

//import global.simpleway.wildfly.config.log4j2.SafeDeleteAction;

public class LoggerBootstrap {

	public static void start() {
		// no library should use System out, except log4j2 internal logger
//		SystemOutInterceptor.intercept();
		//PluginManager.addPackages(List.of(SafeDeleteAction.class.getPackageName()));
	}
}
