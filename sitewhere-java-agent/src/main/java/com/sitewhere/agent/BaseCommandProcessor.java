/*
 * Copyright (c) SiteWhere LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the MIT
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.agent;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.StreamCorruptedException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sitewhere.device.communication.protobuf.proto.Sitewhere.Device;
import com.sitewhere.device.communication.protobuf.proto.Sitewhere.Device.Header;
import com.sitewhere.device.communication.protobuf.proto.Sitewhere.Device.RegistrationAck;
import com.sitewhere.device.communication.protobuf.proto.Sitewhere.SiteWhere.RegisterDevice;
import com.sitewhere.device.communication.protobuf.proto.Sitewhere.Model;
import com.sitewhere.device.communication.protobuf.proto.Sitewhere.SiteWhere;
import com.sitewhere.spi.device.event.IDeviceEventOriginator;

/**
 * Base class for command processing. Handles processing of inbound SiteWhere system
 * messages. Processing of specification commands is left up to subclasses.
 * 
 * @author Derek
 */
public class BaseCommandProcessor implements IAgentCommandProcessor {

	/** Static logger instance */
	private static final Logger LOGGER = Logger.getLogger(BaseCommandProcessor.class.getName());

	/** SiteWhere event dispatcher */
	private ISiteWhereEventDispatcher eventDispatcher;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sitewhere.agent.IAgentCommandProcessor#executeStartupLogic(java.lang.String,
	 * java.lang.String, com.sitewhere.agent.ISiteWhereEventDispatcher)
	 */
	@Override
	public void executeStartupLogic(String hardwareId, String specificationToken,
			ISiteWhereEventDispatcher dispatcher) throws SiteWhereAgentException {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.agent.IAgentCommandProcessor#processSiteWhereCommand(byte[],
	 * com.sitewhere.agent.ISiteWhereEventDispatcher)
	 */
	@Override
	public void processSiteWhereCommand(byte[] message, ISiteWhereEventDispatcher dispatcher)
			throws SiteWhereAgentException {
		ByteArrayInputStream stream = new ByteArrayInputStream(message);
		try {
			Header header = Device.Header.parseDelimitedFrom(stream);
			switch (header.getCommand()) {
			case ACK_REGISTRATION: {
				RegistrationAck ack = RegistrationAck.parseDelimitedFrom(stream);
				handleRegistrationAck(header, ack);
				break;
			}
			case ACK_DEVICE_STREAM: {
				// TODO: Add device stream support.
				break;
			}
			case RECEIVE_DEVICE_STREAM_DATA: {
				// TODO: Add device stream support.
				break;
			}
			}
		} catch (IOException e) {
			throw new SiteWhereAgentException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.agent.IAgentCommandProcessor#processSpecificationCommand(byte[],
	 * com.sitewhere.agent.ISiteWhereEventDispatcher)
	 */
	@Override
	public void processSpecificationCommand(byte[] message, ISiteWhereEventDispatcher dispatcher)
			throws SiteWhereAgentException {
		try {
			ByteArrayInputStream encoded = new ByteArrayInputStream(message);
			ObjectInputStream in = new ObjectInputStream(encoded);

			String commandName = (String) in.readObject();
			Object[] parameters = (Object[]) in.readObject();
			Object[] parametersWithOriginator = new Object[parameters.length + 1];
			Class<?>[] types = new Class[parameters.length];
			Class<?>[] typesWithOriginator = new Class[parameters.length + 1];
			int i = 0;
			for (Object parameter : parameters) {
				types[i] = parameter.getClass();
				typesWithOriginator[i] = types[i];
				parametersWithOriginator[i] = parameters[i];
				i++;
			}
			IDeviceEventOriginator originator = (IDeviceEventOriginator) in.readObject();
			typesWithOriginator[i] = IDeviceEventOriginator.class;
			parametersWithOriginator[i] = originator;

			Method method = null;
			try {
				method = getClass().getMethod(commandName, typesWithOriginator);
				method.invoke(this, parametersWithOriginator);
			} catch (NoSuchMethodException e) {
				LOGGER.log(Level.WARNING, "Unable to find method with originator parameter.", e);
				method = getClass().getMethod(commandName, types);
				method.invoke(this, parameters);
			}
		} catch (StreamCorruptedException e) {
			LOGGER.log(Level.WARNING, "Unable to decode command in hybrid mode.", e);
		} catch (IOException e) {
			LOGGER.log(Level.WARNING, "Unable to read command in hybrid mode.", e);
		} catch (ClassNotFoundException e) {
			LOGGER.log(Level.WARNING, "Unable to resolve parameter class.", e);
		} catch (NoSuchMethodException e) {
			LOGGER.log(Level.WARNING, "Unable to find method signature that matches command.", e);
		} catch (IllegalAccessException e) {
			LOGGER.log(Level.WARNING, "Not allowed to call method for command.", e);
		} catch (IllegalArgumentException e) {
			LOGGER.log(Level.WARNING, "Invalid argument for command.", e);
		} catch (InvocationTargetException e) {
			LOGGER.log(Level.WARNING, "Unable to call method for command.", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sitewhere.agent.IAgentCommandProcessor#setEventDispatcher(com.sitewhere.agent
	 * .ISiteWhereEventDispatcher)
	 */
	public void setEventDispatcher(ISiteWhereEventDispatcher eventDispatcher) {
		this.eventDispatcher = eventDispatcher;
	}

	public ISiteWhereEventDispatcher getEventDispatcher() {
		return eventDispatcher;
	}

	/**
	 * Handle the registration acknowledgement message.
	 * 
	 * @param ack
	 * @param originator
	 */
	public void handleRegistrationAck(Header header, RegistrationAck ack) {
	}

	/**
	 * Convenience method for sending device registration information to SiteWhere.
	 * 
	 * @param hardwareId
	 * @param specificationToken
	 * @throws SiteWhereAgentException
	 */
	public void sendRegistration(String hardwareId, String specificationToken) throws SiteWhereAgentException {
		RegisterDevice.Builder builder = RegisterDevice.newBuilder();
		RegisterDevice register =
				builder.setHardwareId(hardwareId).setSpecificationToken(specificationToken).build();
		getEventDispatcher().registerDevice(register, null);
	}

	/**
	 * Convenience method for sending an acknowledgement event to SiteWhere.
	 * 
	 * @param hardwareId
	 * @param message
	 * @param originator
	 * @throws SiteWhereAgentException
	 */
	public void sendAck(String hardwareId, String message, IDeviceEventOriginator originator)
			throws SiteWhereAgentException {
		SiteWhere.Acknowledge.Builder builder = SiteWhere.Acknowledge.newBuilder();
		SiteWhere.Acknowledge ack = builder.setHardwareId(hardwareId).setMessage(message).build();
		getEventDispatcher().acknowledge(ack, getOriginatorEventId(originator));
	}

	/**
	 * Convenience method for sending a measurement event to SiteWhere.
	 * 
	 * @param hardwareId
	 * @param name
	 * @param value
	 * @param originator
	 * @throws SiteWhereAgentException
	 */
	public void sendMeasurement(String hardwareId, String name, double value,
			IDeviceEventOriginator originator) throws SiteWhereAgentException {
		Model.DeviceMeasurements.Builder mb = Model.DeviceMeasurements.newBuilder();
		mb.setHardwareId(hardwareId).addMeasurement(
				Model.Measurement.newBuilder().setMeasurementId(name).setMeasurementValue(value).build());
		getEventDispatcher().sendMeasurement(mb.build(), getOriginatorEventId(originator));
	}

	/**
	 * Convenience method for sending a location event to SiteWhere.
	 * 
	 * @param hardwareId
	 * @param originator
	 * @param latitude
	 * @param longitude
	 * @param elevation
	 * @param originator
	 * @throws SiteWhereAgentException
	 */
	public void sendLocation(String hardwareId, double latitude, double longitude, double elevation,
			IDeviceEventOriginator originator) throws SiteWhereAgentException {
		Model.DeviceLocation.Builder lb = Model.DeviceLocation.newBuilder();
		lb.setHardwareId(hardwareId).setLatitude(latitude).setLongitude(longitude).setElevation(elevation);
		getEventDispatcher().sendLocation(lb.build(), getOriginatorEventId(originator));
	}

	/**
	 * Convenience method for sending an alert event to SiteWhere.
	 * 
	 * @param hardwareId
	 * @param alertType
	 * @param message
	 * @param originator
	 * @throws SiteWhereAgentException
	 */
	public void sendAlert(String hardwareId, String alertType, String message,
			IDeviceEventOriginator originator) throws SiteWhereAgentException {
		Model.DeviceAlert.Builder ab = Model.DeviceAlert.newBuilder();
		ab.setHardwareId(hardwareId).setAlertType(alertType).setAlertMessage(message);
		getEventDispatcher().sendAlert(ab.build(), getOriginatorEventId(originator));
	}

	/**
	 * Gets event id of the originating command if available.
	 * 
	 * @param originator
	 * @return
	 */
	protected String getOriginatorEventId(IDeviceEventOriginator originator) {
		if (originator == null) {
			return null;
		}
		return originator.getEventId();
	}
}