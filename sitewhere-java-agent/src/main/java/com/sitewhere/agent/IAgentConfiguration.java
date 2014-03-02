/*
 * IAgentConfiguration.java 
 * --------------------------------------------------------------------------------------
 * Copyright (c) Reveal Technologies, LLC. All rights reserved. http://www.reveal-tech.com
 *
 * The software in this package is published under the terms of the MIT
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.agent;

/**
 * Constants for agent configuration properties.
 * 
 * @author Derek
 */
public interface IAgentConfiguration {

	/** Property for command processor classname */
	public static final String COMMAND_PROCESSOR_CLASSNAME = "command.processor.classname";

	/** Property for device unique hardware id */
	public static final String DEVICE_HARDWARE_ID = "device.hardware.id";

	/** Property for device specification token */
	public static final String DEVICE_SPECIFICATION_TOKEN = "device.specification.token";

	/** Property for MQTT hostname */
	public static final String MQTT_HOSTNAME = "mqtt.hostname";

	/** Property for MQTT port */
	public static final String MQTT_PORT = "mqtt.port";

	/** Property for outbound SiteWhere MQTT topic */
	public static final String MQTT_OUTBOUND_SITEWHERE_TOPIC = "mqtt.outbound.sitewhere.topic";

	/** Property for inbound SiteWhere MQTT topic */
	public static final String MQTT_INBOUND_SITEWHERE_TOPIC = "mqtt.inbound.sitewhere.topic";

	/** Property for inbound command MQTT topic */
	public static final String MQTT_INBOUND_COMMAND_TOPIC = "mqtt.inbound.command.topic";
}