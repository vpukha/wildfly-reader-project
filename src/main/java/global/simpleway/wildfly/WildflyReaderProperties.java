package global.simpleway.wildfly;

import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("wildfly.reader")
public class WildflyReaderProperties {

	private static WildflyReaderProperties instance;

	public static WildflyReaderProperties getInstance() {
		return instance;
	}

	@PostConstruct
	public void storeInstance() {
		instance = this;
	}

	public String activeMqUrl = "localhost:61616";

	public String activeMqUsername = null;

	public String activeMqPassword = null;

	public List<String> activeMqQueue = Collections.singletonList("ATS");

	public List<String> activeMqTopic = Collections.singletonList("topic");

	public String wildFlyUrl = "http-remoting://localhost:8080";

	public List<String> wildFlyTopic = Collections.singletonList("MyTopic");

	public List<String> wildFlyQueue = Collections.singletonList("queue");

	public String wildFlyUsername = "admin";

	public String wildFlyPassword = "admin";

	public String getActiveMqUrl() {
		return activeMqUrl;
	}

	public List<String> getWildFlyQueue() {
		return wildFlyQueue;
	}

	public void setWildFlyQueue(List<String> wildFlyQueue) {
		this.wildFlyQueue = wildFlyQueue;
	}

	public void setActiveMqUrl(String activeMqUrl) {
		this.activeMqUrl = activeMqUrl;
	}

	public String getActiveMqUsername() {
		return activeMqUsername;
	}

	public void setActiveMqUsername(String activeMqUsername) {
		this.activeMqUsername = activeMqUsername;
	}

	public String getActiveMqPassword() {
		return activeMqPassword;
	}

	public void setActiveMqPassword(String activeMqPassword) {
		this.activeMqPassword = activeMqPassword;
	}

	public String getWildFlyUrl() {
		return wildFlyUrl;
	}

	public void setWildFlyUrl(String wildFlyUrl) {
		this.wildFlyUrl = wildFlyUrl;
	}

	public String getWildFlyUsername() {
		return wildFlyUsername;
	}

	public void setWildFlyUsername(String wildFlyUsername) {
		this.wildFlyUsername = wildFlyUsername;
	}

	public String getWildFlyPassword() {
		return wildFlyPassword;
	}

	public void setWildFlyPassword(String wildFlyPassword) {
		this.wildFlyPassword = wildFlyPassword;
	}

	public List<String> getWildFlyTopic() {
		return wildFlyTopic;
	}

	public void setWildFlyTopic(List<String> wildFlyTopic) {
		this.wildFlyTopic = wildFlyTopic;
	}

	public List<String> getActiveMqQueue() {
		return activeMqQueue;
	}

	public void setActiveMqQueue(List<String> activeMqQueue) {
		this.activeMqQueue = activeMqQueue;
	}

	public List<String> getActiveMqTopic() {
		return activeMqTopic;
	}

	public void setActiveMqTopic(List<String> activeMqTopic) {
		this.activeMqTopic = activeMqTopic;
	}

}
