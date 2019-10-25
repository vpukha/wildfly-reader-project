package global.simpleway.logger;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.AbstractLifeCycle;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Core;
import org.apache.logging.log4j.core.ErrorHandler;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.SocketAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderFactory;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.core.net.AbstractSocketManager;
import org.apache.logging.log4j.core.net.Advertiser;
import org.apache.logging.log4j.core.net.Protocol;
import org.apache.logging.log4j.core.util.Assert;
import org.apache.logging.log4j.message.SimpleMessage;
import org.apache.logging.log4j.util.SortedArrayStringMap;
import org.apache.logging.log4j.util.StringMap;

@Plugin(name = "ChunkSocketAppender", category = Core.CATEGORY_NAME, elementType = Appender.ELEMENT_TYPE, printObject = true)
public class ChunkSocketAppender extends SocketAppender {

	public static final AtomicLong counterMessagesSent = new AtomicLong(0);
	public static final AtomicLong counterChunksSent = new AtomicLong(0);
	public static final AtomicLong counterErrorLayoutFallbacks = new AtomicLong(0);
	public static final AtomicLong counterErrorSentChunks = new AtomicLong(0);

	/**
	 * int - msg id
	 * short - number of messages, starts from 1
	 * short - total number of messages
	 */
	public static int HEADER_SIZE = 4 + 2 + 2;
	private final AtomicInteger msgIdCounter = new AtomicInteger(0);

	protected ChunkSocketAppender(String name, Layout<? extends Serializable> layout, Filter filter,
			AbstractSocketManager manager, boolean ignoreExceptions, boolean immediateFlush,
			Advertiser advertiser) {
		super(name, layout, filter, manager, ignoreExceptions, immediateFlush, advertiser);
	}

	/**
	 * Copied from {@link SocketAppender.Builder}
	 * <p>
	 * Builds a SocketAppender.
	 * <ul>
	 * <li>Removed deprecated "delayMillis", use "reconnectionDelayMillis".</li>
	 * <li>Removed deprecated "reconnectionDelay", use "reconnectionDelayMillis".</li>
	 * </ul>
	 */
	public static class Builder extends SocketAppender.Builder
			implements org.apache.logging.log4j.core.util.Builder<SocketAppender> {

		@SuppressWarnings("resource")
		@Override
		public SocketAppender build() {
			boolean immediateFlush = isImmediateFlush();
			final boolean bufferedIo = isBufferedIo();
			final Layout<? extends Serializable> layout = getLayout();
			if (layout == null) {
				AbstractLifeCycle.LOGGER.error("No layout provided for SocketAppender");
				return null;
			}

			final String name = getName();
			if (name == null) {
				AbstractLifeCycle.LOGGER.error("No name provided for SocketAppender");
				return null;
			}

			final Protocol protocol = getProtocol();
			final Protocol actualProtocol = protocol != null ? protocol : Protocol.TCP;
			if (actualProtocol == Protocol.UDP) {
				immediateFlush = true;
			}
			final AbstractSocketManager manager = SocketAppender.createSocketManager(name, actualProtocol, LogProperties.getLogServerHost(),
					LogProperties.getLogServerPort(), getConnectTimeoutMillis(), getSslConfiguration(), getReconnectDelayMillis(), getImmediateFail(), layout,
					getBufferSize(), getSocketOptions());

			return new ChunkSocketAppender(name, layout, getFilter(), manager, isIgnoreExceptions(),
					!bufferedIo || immediateFlush, getAdvertise() ? getConfiguration().getAdvertiser() : null);
		}
	}

	@PluginBuilderFactory
	public static Builder newBuilder() {
		return new ChunkSocketAppender.Builder();
	}

	@Override
	protected void writeByteArrayToManager(LogEvent event) {
		String formattedMessage = event.getMessage().getFormattedMessage();
		if (StringUtils.isBlank(formattedMessage)) {
			return;
		}
		counterMessagesSent.incrementAndGet();

		// In case of RingBufferLog4j event remove unnecessary fields,
		// new event instance is a Log4jLogEvent without problematic fields
		event = Log4jLogEvent.createMemento(event);
		if (!(event instanceof Log4jLogEvent)) {
			throw new IllegalStateException("Log4j2 Update safety check, to have proper instance which doesn't cause problems during a serialization");
		}

		//this is a static long counter for verification purposes
		byte[] originalMessage;
		try {
			originalMessage = getLayout().toByteArray(event);
			if (originalMessage.length == 0) {
				originalMessage = fallbackSimpleMessage(event, formattedMessage);
			}
		} catch (RuntimeException e) {
			ErrorHandler errorHandler = getHandler();
			if (errorHandler != null) {
				errorHandler.error("Unable to convert logger event to json message", e);
			}
			originalMessage = fallbackSimpleMessage(event, formattedMessage);
		}
		int partMaxSize = LogProperties.getLogServerChunkSize() - HEADER_SIZE;
		//optimized ceil function with int return value
		short totalParts = (short) Math.min((originalMessage.length + partMaxSize - 1) / partMaxSize, Short.MAX_VALUE);

		int msgId = msgIdCounter.incrementAndGet();
		AbstractSocketManager manager = getManager();
		RuntimeException atLeastOneException = null;

		for (int part = 0, offset = 0; originalMessage.length - offset > 0 && part < Short.MAX_VALUE; part++) {
			counterChunksSent.incrementAndGet();
			try {
				int partSize = Math.min(partMaxSize, originalMessage.length - offset);
				ByteBuffer chunk = ByteBuffer.allocate(partSize + HEADER_SIZE);
				chunk.clear();
				chunk.putInt(msgId)
						.putShort((short) part)
						.putShort(totalParts)
						.put(originalMessage, offset, partSize);
				offset += partSize;
				manager.drain(chunk);
				manager.flush();
			} catch (RuntimeException e) {
				counterErrorSentChunks.incrementAndGet();
				atLeastOneException = e;
			}
		}
		if (atLeastOneException != null) {
			throw atLeastOneException;
		}
	}

	private byte[] fallbackSimpleMessage(LogEvent event, String message) {
		counterErrorLayoutFallbacks.incrementAndGet();
		StringMap stringMap = new SortedArrayStringMap();
		stringMap.putValue("loggerProblem", true);
		Log4jLogEvent msg = Log4jLogEvent.newBuilder()
				.setLevel(event.getLevel() != null ? event.getLevel() : Level.WARN)
				.setLoggerName("global.simpleway.logger.fallback")
				.setThreadName("unknown")
				.setContextData(stringMap)
				.setMessage(new SimpleMessage(message))
				.build();
		return getLayout().toByteArray(msg);
	}
}
