package global.simpleway.logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Intercept {@link System#out} and {@link System#err} calls and direct then to the logger.
 * Must be called first to avoid creating 2x folders with different timestamps
 * @author mzachar
 * 
 */
/* package */final class SystemOutInterceptor {
	private SystemOutInterceptor() {
		//no code
	}

	public static void intercept() {
		// once logback is logging directly we can safely intercept
		System.setOut(new LoggingPrintStream("system.out", Level.INFO));
		System.setErr(new LoggingPrintStream("system.err",  Level.WARN));
	}
}

class LoggingPrintStream extends PrintStream {

	public LoggingPrintStream(String loggerName, Level level) {
		super(new LoggingOutputStream(loggerName, level), true);
	}
}

class LoggingOutputStream extends OutputStream {

	private final Logger logger; // NOSONAR

	private final ByteArrayOutputStream out = new ByteArrayOutputStream(1024);

	private final String systemLineSeparator = System.getProperty("line.separator");

	private final Level level;

	public LoggingOutputStream(String loggerName, Level level) {
		logger = LogManager.getLogger(loggerName);
		this.level = level;
	}

	@Override
	public void write(int b) throws IOException {
		out.write(b);
	}

	@Override
	public void flush() throws IOException {
		String msg = out.toString();

		if (msg.endsWith(systemLineSeparator)) {
			msg = msg.substring(0, msg.length() - systemLineSeparator.length());
		}

		if (!msg.isEmpty()) {
			logger.log(level, msg);
		}
		out.reset();
	}
}
