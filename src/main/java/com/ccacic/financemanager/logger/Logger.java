package com.ccacic.financemanager.logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.ccacic.financemanager.fileio.FileHandler;

/**
 * General thread-safe singleton logging class. Messages to be logged are added to a queue with their text and
 * severity level (info, debug, warning, error), which is displayed in brackets in front of the message
 * when it is logged along with a timestamp. Messages added without a severity level are treated as debug
 * level messages. Where the messages are logged is controlled by the PrintStream within the instance. Messages
 * below a certain severity level can be ignored by the logger by changing its verbosity level, default logs
 * debug and higher severity
 * 
 * @author Cameron Cacic
 *
 */
public final class Logger {
	
	private static final Logger loggerInstance = new Logger();
	
	private static LoggingThread loggingThread;
	private static final boolean[] flag = new boolean[] {true};
	
	/**
	 * A Thread specifically for logging from a queue
	 * @author Cameron Cacic
	 *
	 */
	private static class LoggingThread extends Thread {
		
		@Override
		public void run() {
			
			while (flag[0]) {
				
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				while (!loggerInstance.queue.isEmpty()) {
					
					Message message = loggerInstance.queue.poll();
					if (loggerInstance.minSeverity.compareTo(message.severity) <= 0) {
						
						String log = "[" + message.threadName + "]"
								+ "[" + message.time + "]"
								+ "[" + message.severity.name() + "]"
								+ " " + message.text;
						loggerInstance.printStream.println(log);
						if (loggerInstance.printStream != System.out) {
							System.out.println(log);
						}
						
					}
					
				}
				
			}
			
		}
		
	}
	
	/**
	 * Returns the Logger instance
	 * @return the Logger instance
	 */
	public static Logger getInstance() {
		return loggerInstance;
	}
	
	/**
	 * Returns the logging Thread
	 * @return the logging Thread
	 */
	public static Thread getLoggingThread() {
		return loggingThread;
	}
	
	/**
	 * Sets the stream the Logger instance with write out to
	 * @param stream the stream to write to
	 */
	public static synchronized void setInstanceStream(PrintStream stream) {
		loggerInstance.printStream = stream;
	}
	
	/**
	 * Sets the minimum severity level that will get logged
	 * @param minSeverity the minimum logged severity
	 */
	public static synchronized void setVerbosityLevel(Severity minSeverity) {
		loggerInstance.minSeverity = minSeverity;
	}
	
	/**
	 * Begins logging
	 */
	public static synchronized void startLogging() {
		loggingThread = new LoggingThread();
		loggingThread.start();
	}
	
	/**
	 * Halts logging
	 */
	public static synchronized void stopLogging() {
		flag[0] = false;
		try {
			loggingThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * The severity levels of Messages
	 * @author Cameron Cacic
	 *
	 */
	public enum Severity {
		INFO,
		DEBUG,
		WARN,
		ERROR
	}
	
	/**
	 * A class that represents a message delivered to Logger to log
	 * @author Cameron Cacic
	 *
	 */
	private class Message {
		
		private final Severity severity;
		private final String text;
		private final LocalDateTime time;
		private final String threadName;
		
		/**
		 * Creates a new Message
		 * @param severity the severity of the Message
		 * @param text the text of the Message
		 * @param time the timestamp of the Message
		 * @param threadName the name of the Thread the Message is recieved from
		 */
		Message(Severity severity, String text, LocalDateTime time, String threadName) {
			this.severity = severity;
			this.text = text;
			this.time = time;
			this.threadName = threadName;
		}
		
	}
	
	
	private PrintStream printStream;
	private final ConcurrentLinkedQueue<Message> queue;
	private Severity minSeverity;
	
	/**
	 * Creates the singleton instance of Logger
	 */
	private Logger() {
		File logFile = new File(FileHandler.getInstance().getDataDir(), "log.txt");
		try {
			printStream = new PrintStream(logFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			printStream = System.out;
		}
		queue = new ConcurrentLinkedQueue<>();
		minSeverity = Severity.DEBUG;
		
		logDebug("Logger Initialized");
	}
	
	/**
	 * Logs the passed message with the passed Severity
	 * @param severity the Severity of the message
	 * @param message the message
	 */
	private void log(Severity severity, String message) {
		
		LocalDateTime time = LocalDateTime.now();
		String threadName = Thread.currentThread().getName();
		
		queue.add(new Message(severity, message, time, threadName));
		
	}
	
	/**
	 * Logs an information level message
	 * @param message the information message
	 */
	public void logInfo(String message) {
		log(Severity.INFO, message);
	}
	
	/**
	 * Logs a debug level message
	 * @param message the debug message
	 */
	public void logDebug(String message) {
		log(Severity.DEBUG, message);
	}
	
	/**
	 * Logs a warning message
	 * @param message the warning message
	 */
	public void logWarning(String message) {
		log(Severity.WARN, message);
	}
	
	/**
	 * Logs an error message
	 * @param message the error message
	 */
	public void logError(String message) {
		log(Severity.ERROR, message);
	}
	
	/**
	 * Logs an Exception
	 * @param e the Exception to log
	 */
	public void logException(Exception e) {
		log(Severity.ERROR, e.getMessage());
		e.printStackTrace(printStream);
	}
	
}
