package delight.simplelog.graylog;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import biz.paluch.logging.gelf.intern.ErrorReporter;
import biz.paluch.logging.gelf.intern.GelfMessage;
import biz.paluch.logging.gelf.intern.GelfSender;
import biz.paluch.logging.gelf.intern.GelfSenderConfiguration;
import biz.paluch.logging.gelf.intern.GelfSenderFactory;
import delight.simplelog.FieldDefinition;
import delight.simplelog.Level;
import delight.simplelog.Log;
import delight.simplelog.LogListener;

public class GelfBridge implements LogListener {

	private GelfSender sender;
	private String thisHost;

	private static final int MAX_SHORT_MESSAGE_LENGTH = 250;

	private GelfMessage createMessage(Level level, String text) {
		GelfMessage message = new GelfMessage();

		message.setFullMessage(text);
		if (text.length() > MAX_SHORT_MESSAGE_LENGTH) {
			message.setShortMessage(text.substring(0, MAX_SHORT_MESSAGE_LENGTH - 2));
		} else {
			message.setShortMessage(text);
		}
		message.setJavaTimestamp(System.currentTimeMillis());
		message.setLevel(level.name());
		message.setHost(thisHost);
		message.setVersion("1.1");
		message.setMaximumMessageSize(8192);
		return message;
	}

	private void addFields(FieldDefinition[] fields, GelfMessage message) {
		for (FieldDefinition field : fields) {
			message.addField(field.key(), field.value());
		}
	}

	private void addStackTrace(Throwable exception, GelfMessage message) {
		final StringWriter sw = new StringWriter();
		exception.printStackTrace(new PrintWriter(sw));
		message.addField("StackTrace", sw.toString());
	}

	private void addContext(Object context, GelfMessage message) {
		message.addField("className", context.getClass().getName());
		message.addField("simpleClassName", context.getClass().getSimpleName());
		message.addField("instance", Log.getSimpleObjectName(context));
	}

	@Override
	public void onMessage(Level level, String text) {
		GelfMessage message = createMessage(level, text);

		sender.sendMessage(message);

	}

	@Override
	public void onMessage(Level level, String text, FieldDefinition[] fields) {
		GelfMessage message = createMessage(level, text);

		addFields(fields, message);

		sender.sendMessage(message);
	}

	@Override
	public void onMessage(Level level, String text, Throwable exception) {
		GelfMessage message = createMessage(level, text);

		addStackTrace(exception, message);

		sender.sendMessage(message);

	}

	@Override
	public void onMessage(Level level, String text, Throwable exception, FieldDefinition[] fields) {
		GelfMessage message = createMessage(level, text);

		addFields(fields, message);
		addStackTrace(exception, message);

		sender.sendMessage(message);

	}

	@Override
	public void onMessage(Level level, Object context, String text) {
		GelfMessage message = createMessage(level, text);

		addContext(context, message);

		sender.sendMessage(message);
	}

	@Override
	public void onMessage(Level level, Object context, String text, FieldDefinition[] fields) {
		GelfMessage message = createMessage(level, text);

		addContext(context, message);
		addFields(fields, message);

		sender.sendMessage(message);

	}

	@Override
	public void onMessage(Level level, Object context, String text, Throwable exception) {
		GelfMessage message = createMessage(level, text);

		addContext(context, message);
		addStackTrace(exception, message);

		sender.sendMessage(message);

	}

	@Override
	public void onMessage(Level level, Object context, String text, Throwable exception, FieldDefinition[] fields) {
		GelfMessage message = createMessage(level, text);

		addContext(context, message);
		addStackTrace(exception, message);
		addFields(fields, message);

		sender.sendMessage(message);

	}

	public GelfBridge(final String host, final int port) {
		super();
		this.sender = GelfSenderFactory.createSender(new GelfSenderConfiguration() {

			@Override
			public Map<String, Object> getSpecificConfigurations() {
				return new HashMap<String, Object>();
			}

			@Override
			public int getPort() {

				return port;
			}

			@Override
			public String getHost() {
				return host;
			}

			@Override
			public ErrorReporter getErrorReporter() {
				return new ErrorReporter() {

					@Override
					public void reportError(String message, Exception e) {
						System.out.println("Logging error: " + message);
						throw new RuntimeException(e);
					}
				};
			}
		});
		try {
			this.thisHost = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}

	}

}
