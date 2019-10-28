package global.simpleway.wildfly;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import global.simpleway.logger.LoggerBootstrap;

@SpringBootApplication
public class WildflyReaderApplication {

	static {
		LoggerBootstrap.init();
	}

	public static void main(String[] args) throws InterruptedException {
		SpringApplication.run(WildflyReaderApplication.class, args);
	}


}
