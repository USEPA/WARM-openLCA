package gov.epa.warm.backend.app;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import com.google.common.base.Objects;

public class LoggerConfig {

	private static final String ROOT_PACKAGE = "gov.epa.warm";

	public static void setLevel(Level level) {
		Logger logger = Objects.equal(level, Level.ALL) ? Logger.getLogger(ROOT_PACKAGE) : Logger.getRootLogger();
		logger.setLevel(level);
		logger.info("Log-level=" + level);
	}

	public static void setUp() {
		Logger rootLogger = Logger.getRootLogger();
		rootLogger.setLevel(Level.ALL);
		setUpOlcaLogger();
	}

	private static void setUpOlcaLogger() {
		Logger logger = Logger.getLogger(ROOT_PACKAGE);
		HtmlLogFile.create(logger);
		addConsoleOutput(logger);
		setLogLevel(logger);
	}

	private static void addConsoleOutput(Logger logger) {
		BasicConfigurator.configure();
		ConsoleAppender appender = new ConsoleAppender(new PatternLayout());
		logger.addAppender(appender);
		appender.setTarget(ConsoleAppender.SYSTEM_OUT);
		appender.activateOptions();
	}

	private static void setLogLevel(Logger logger) {
		logger.setLevel(Level.ALL);
		logger.info("Log-level=" + logger.getLevel());
	}

}
