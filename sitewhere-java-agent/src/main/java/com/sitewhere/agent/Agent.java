/*
 * Agent.java 
 * --------------------------------------------------------------------------------------
 * Copyright (c) Reveal Technologies, LLC. All rights reserved. http://www.reveal-tech.com
 *
 * The software in this package is published under the terms of the MIT
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.agent;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.fusesource.mqtt.client.BlockingConnection;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.Message;
import org.fusesource.mqtt.client.QoS;
import org.fusesource.mqtt.client.Topic;

import com.sitewhere.device.provisioning.protobuf.proto.Sitewhere.SiteWhere;
import com.sitewhere.device.provisioning.protobuf.proto.Sitewhere.SiteWhere.Acknowledge;
import com.sitewhere.device.provisioning.protobuf.proto.Sitewhere.SiteWhere.Command;
import com.sitewhere.device.provisioning.protobuf.proto.Sitewhere.SiteWhere.RegisterDevice;

/**
 * Agent that handles message processing.
 * 
 * @author Derek
 */
public class Agent {

	/** Static logger instance */
	private static final Logger LOGGER = Logger.getLogger(Agent.class.getName());

	/** Default outbound SiteWhere MQTT topic */
	private static final String DEFAULT_MQTT_OUTBOUND_SITEWHERE = "SiteWhere/input/protobuf";

	/** Default MQTT hostname */
	private static final String DEFAULT_MQTT_HOSTNAME = "localhost";

	/** Default MQTT port */
	private static final int DEFAULT_MQTT_PORT = 1883;

	/** Command processor Java classname */
	private String commandProcessorClassname;

	/** Hardware id */
	private String hardwareId;

	/** Specification token */
	private String specificationToken;

	/** MQTT server hostname */
	private String mqttHostname;

	/** MQTT server port */
	private int mqttPort;

	/** Outbound SiteWhere MQTT topic */
	private String outboundSiteWhereTopic;

	/** Inbound SiteWhere MQTT topic */
	private String inboundSiteWhereTopic;

	/** Inbound specification command MQTT topic */
	private String inboundCommandTopic;

	/** MQTT client */
	private MQTT mqtt;

	/** MQTT connection */
	private BlockingConnection connection;

	/** Outbound message processing */
	private MQTTOutbound outbound;

	/** Inbound message processing */
	private MQTTInbound inbound;

	/** Used to execute MQTT inbound in separate thread */
	private ExecutorService executor = Executors.newSingleThreadExecutor();

	/**
	 * Start the agent.
	 */
	public void start() throws SiteWhereAgentException {
		LOGGER.info("SiteWhere agent starting...");

		// Create an instance of the command processor.
		IAgentCommandProcessor processor = createProcessor();

		this.mqtt = new MQTT();
		try {
			mqtt.setHost(getMqttHostname(), getMqttPort());
		} catch (URISyntaxException e) {
			throw new SiteWhereAgentException("Invalid hostname for MQTT server.", e);
		}
		LOGGER.info("Connecting to MQTT broker at '" + getMqttHostname() + ":" + getMqttPort() + "'...");
		connection = mqtt.blockingConnection();
		try {
			connection.connect();
		} catch (Exception e) {
			throw new SiteWhereAgentException("Unable to establish MQTT connection.", e);
		}
		LOGGER.info("Connected to MQTT broker.");

		// Create outbound message processor.
		outbound = new MQTTOutbound(connection, getOutboundSiteWhereTopic());

		// Create inbound message processing thread.
		inbound =
				new MQTTInbound(connection, getInboundSiteWhereTopic(), getInboundCommandTopic(), processor,
						outbound);

		// Handle shutdown gracefully.
		Runtime.getRuntime().addShutdownHook(new ShutdownHandler());

		// Starts inbound processing loop in a separate thread.
		executor.execute(inbound);

		// Executes any custom startup logic.
		processor.executeStartupLogic(getHardwareId(), getSpecificationToken(), outbound);

		LOGGER.info("SiteWhere agent started.");
	}

	/**
	 * Create an instance of the command processor.
	 * 
	 * @return
	 * @throws SiteWhereAgentException
	 */
	protected IAgentCommandProcessor createProcessor() throws SiteWhereAgentException {
		try {
			Class<?> clazz = Class.forName(getCommandProcessorClassname());
			return (IAgentCommandProcessor) clazz.newInstance();
		} catch (ClassNotFoundException e) {
			throw new SiteWhereAgentException(e);
		} catch (InstantiationException e) {
			throw new SiteWhereAgentException(e);
		} catch (IllegalAccessException e) {
			throw new SiteWhereAgentException(e);
		}
	}

	/**
	 * Internal class for sending MQTT outbound messages.
	 * 
	 * @author Derek
	 */
	public static class MQTTOutbound implements ISiteWhereEventDispatcher {

		/** MQTT outbound topic */
		private String topic;

		/** MQTT connection */
		private BlockingConnection connection;

		public MQTTOutbound(BlockingConnection connection, String topic) {
			this.connection = connection;
			this.topic = topic;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * com.sitewhere.agent.ISiteWhereEventDispatcher#registerDevice(com.sitewhere.
		 * device.provisioning.protobuf.proto.Sitewhere.SiteWhere.RegisterDevice,
		 * java.lang.String)
		 */
		@Override
		public void registerDevice(RegisterDevice register, String originator) throws SiteWhereAgentException {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			try {
				SiteWhere.Header.Builder builder = SiteWhere.Header.newBuilder();
				builder.setCommand(Command.REGISTER);
				if (originator != null) {
					builder.setOriginator(originator);
				}
				builder.build().writeDelimitedTo(out);
				register.writeDelimitedTo(out);
				connection.publish(getTopic(), out.toByteArray(), QoS.AT_LEAST_ONCE, false);
			} catch (IOException e) {
				throw new SiteWhereAgentException("Problem encoding registration message.", e);
			} catch (Exception e) {
				throw new SiteWhereAgentException(e);
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * com.sitewhere.agent.ISiteWhereEventDispatcher#acknowledge(com.sitewhere.device
		 * .provisioning.protobuf.proto.Sitewhere.SiteWhere.Acknowledge, java.lang.String)
		 */
		@Override
		public void acknowledge(Acknowledge ack, String originator) throws SiteWhereAgentException {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			try {
				SiteWhere.Header.Builder builder = SiteWhere.Header.newBuilder();
				builder.setCommand(Command.ACKNOWLEDGE);
				if (originator != null) {
					builder.setOriginator(originator);
				}
				builder.build().writeDelimitedTo(out);
				ack.writeDelimitedTo(out);
				connection.publish(getTopic(), out.toByteArray(), QoS.AT_LEAST_ONCE, false);
			} catch (IOException e) {
				throw new SiteWhereAgentException("Problem encoding ack message.", e);
			} catch (Exception e) {
				throw new SiteWhereAgentException(e);
			}
		}

		public BlockingConnection getConnection() {
			return connection;
		}

		public void setConnection(BlockingConnection connection) {
			this.connection = connection;
		}

		public String getTopic() {
			return topic;
		}

		public void setTopic(String topic) {
			this.topic = topic;
		}
	}

	/**
	 * Handles inbound commands. Monitors two topics for messages. One contains SiteWhere
	 * system messages and the other contains messages defined in the device
	 * specification.
	 * 
	 * @author Derek
	 */
	public static class MQTTInbound implements Runnable {

		/** MQTT connection */
		private BlockingConnection connection;

		/** SiteWhere inbound MQTT topic */
		private String sitewhereTopic;

		/** Command inbound MQTT topic */
		private String commandTopic;

		/** Command processor */
		private IAgentCommandProcessor processor;

		/** Event dispatcher */
		private ISiteWhereEventDispatcher dispatcher;

		public MQTTInbound(BlockingConnection connection, String sitewhereTopic, String commandTopic,
				IAgentCommandProcessor processor, ISiteWhereEventDispatcher dispatcher) {
			this.connection = connection;
			this.sitewhereTopic = sitewhereTopic;
			this.commandTopic = commandTopic;
			this.processor = processor;
			this.dispatcher = dispatcher;
		}

		@Override
		public void run() {

			// Subscribe to chosen topic.
			Topic[] topics =
					{
							new Topic(getSitewhereTopic(), QoS.AT_LEAST_ONCE),
							new Topic(getCommandTopic(), QoS.AT_LEAST_ONCE) };
			try {
				connection.subscribe(topics);
				LOGGER.info("Started MQTT inbound processing thread.");
				while (true) {
					try {
						Message message = connection.receive();
						message.ack();
						if (getSitewhereTopic().equals(message.getTopic())) {
							getProcessor().processSiteWhereCommand(message.getPayload(), getDispatcher());
						} else if (getCommandTopic().equals(message.getTopic())) {
							getProcessor().processSpecificationCommand(message.getPayload(), getDispatcher());
						} else {
							LOGGER.warning("Message for unknown topic received: " + message.getTopic());
						}
					} catch (InterruptedException e) {
						LOGGER.warning("Device event processor interrupted.");
						return;
					} catch (Throwable e) {
						LOGGER.log(Level.SEVERE, "Exception processing inbound message", e);
					}
				}
			} catch (Exception e) {
				LOGGER.log(Level.SEVERE, "Exception while attempting to subscribe to inbound topics.", e);
			}
		}

		public BlockingConnection getConnection() {
			return connection;
		}

		public void setConnection(BlockingConnection connection) {
			this.connection = connection;
		}

		public String getSitewhereTopic() {
			return sitewhereTopic;
		}

		public void setSitewhereTopic(String sitewhereTopic) {
			this.sitewhereTopic = sitewhereTopic;
		}

		public String getCommandTopic() {
			return commandTopic;
		}

		public void setCommandTopic(String commandTopic) {
			this.commandTopic = commandTopic;
		}

		public IAgentCommandProcessor getProcessor() {
			return processor;
		}

		public void setProcessor(IAgentCommandProcessor processor) {
			this.processor = processor;
		}

		public ISiteWhereEventDispatcher getDispatcher() {
			return dispatcher;
		}

		public void setDispatcher(ISiteWhereEventDispatcher dispatcher) {
			this.dispatcher = dispatcher;
		}
	}

	/**
	 * Handles graceful shutdown of agent.
	 * 
	 * @author Derek
	 */
	public class ShutdownHandler extends Thread {
		@Override
		public void run() {
			if (connection != null) {
				try {
					connection.disconnect();
					LOGGER.info("Disconnected from MQTT broker.");
				} catch (Exception e) {
					LOGGER.log(Level.WARNING, "Exception disconnecting from MQTT broker.", e);
				}
			}
		}
	}

	/**
	 * Validates the agent configuration.
	 * 
	 * @return
	 */
	public boolean load(Properties properties) {
		LOGGER.info("Validating configuration...");

		// Load command processor class name.
		setCommandProcessorClassname(properties.getProperty(IAgentConfiguration.COMMAND_PROCESSOR_CLASSNAME));
		if (getCommandProcessorClassname() == null) {
			LOGGER.severe("Command processor class name not specified.");
			return false;
		}

		// Validate hardware id.
		setHardwareId(properties.getProperty(IAgentConfiguration.DEVICE_HARDWARE_ID));
		if (getHardwareId() == null) {
			LOGGER.severe("Device hardware id not specified in configuration.");
			return false;
		}
		LOGGER.info("Using configured device hardware id: " + getHardwareId());

		// Validate specification token.
		setSpecificationToken(properties.getProperty(IAgentConfiguration.DEVICE_SPECIFICATION_TOKEN));
		if (getSpecificationToken() == null) {
			LOGGER.severe("Device specification token not specified in configuration.");
			return false;
		}
		LOGGER.info("Using configured device specification token: " + getSpecificationToken());

		// Validate MQTT hostname.
		setMqttHostname(properties.getProperty(IAgentConfiguration.MQTT_HOSTNAME));
		if (getMqttHostname() == null) {
			LOGGER.warning("Using default MQTT hostname: " + DEFAULT_MQTT_HOSTNAME);
			setMqttHostname(DEFAULT_MQTT_HOSTNAME);
		}

		// Validate MQTT port.
		String strPort = properties.getProperty(IAgentConfiguration.MQTT_PORT);
		if (strPort != null) {
			try {
				setMqttPort(Integer.parseInt(strPort));
			} catch (NumberFormatException e) {
				LOGGER.warning("Non-numeric MQTT port specified, using: " + DEFAULT_MQTT_PORT);
				setMqttPort(DEFAULT_MQTT_PORT);
			}
		} else {
			LOGGER.warning("No MQTT port specified, using: " + DEFAULT_MQTT_PORT);
			setMqttPort(DEFAULT_MQTT_PORT);
		}

		// Validate outbound SiteWhere topic.
		setOutboundSiteWhereTopic(properties.getProperty(IAgentConfiguration.MQTT_OUTBOUND_SITEWHERE_TOPIC));
		if (getOutboundSiteWhereTopic() == null) {
			LOGGER.warning("Using default outbound SiteWhere MQTT topic: " + DEFAULT_MQTT_OUTBOUND_SITEWHERE);
			setOutboundSiteWhereTopic(DEFAULT_MQTT_OUTBOUND_SITEWHERE);
		}

		// Validate inbound SiteWhere topic.
		setInboundSiteWhereTopic(properties.getProperty(IAgentConfiguration.MQTT_INBOUND_SITEWHERE_TOPIC));
		if (getInboundSiteWhereTopic() == null) {
			String in = calculateInboundSiteWhereTopic();
			LOGGER.warning("Using default inbound SiteWhere MQTT topic: " + in);
			setInboundSiteWhereTopic(in);
		}

		// Validate inbound command topic.
		setInboundCommandTopic(properties.getProperty(IAgentConfiguration.MQTT_INBOUND_COMMAND_TOPIC));
		if (getInboundCommandTopic() == null) {
			String in = calculateInboundCommandTopic();
			LOGGER.warning("Using default inbound command MQTT topic: " + in);
			setInboundCommandTopic(in);
		}
		return true;
	}

	protected String calculateInboundSiteWhereTopic() {
		return "SiteWhere/system/" + getHardwareId();
	}

	protected String calculateInboundCommandTopic() {
		return "SiteWhere/commands/" + getHardwareId();
	}

	public String getCommandProcessorClassname() {
		return commandProcessorClassname;
	}

	public void setCommandProcessorClassname(String commandProcessorClassname) {
		this.commandProcessorClassname = commandProcessorClassname;
	}

	public String getHardwareId() {
		return hardwareId;
	}

	public void setHardwareId(String hardwareId) {
		this.hardwareId = hardwareId;
	}

	public String getSpecificationToken() {
		return specificationToken;
	}

	public void setSpecificationToken(String specificationToken) {
		this.specificationToken = specificationToken;
	}

	public String getMqttHostname() {
		return mqttHostname;
	}

	public void setMqttHostname(String mqttHostname) {
		this.mqttHostname = mqttHostname;
	}

	public int getMqttPort() {
		return mqttPort;
	}

	public void setMqttPort(int mqttPort) {
		this.mqttPort = mqttPort;
	}

	public String getOutboundSiteWhereTopic() {
		return outboundSiteWhereTopic;
	}

	public void setOutboundSiteWhereTopic(String outboundSiteWhereTopic) {
		this.outboundSiteWhereTopic = outboundSiteWhereTopic;
	}

	public String getInboundSiteWhereTopic() {
		return inboundSiteWhereTopic;
	}

	public void setInboundSiteWhereTopic(String inboundSiteWhereTopic) {
		this.inboundSiteWhereTopic = inboundSiteWhereTopic;
	}

	public String getInboundCommandTopic() {
		return inboundCommandTopic;
	}

	public void setInboundCommandTopic(String inboundCommandTopic) {
		this.inboundCommandTopic = inboundCommandTopic;
	}
}