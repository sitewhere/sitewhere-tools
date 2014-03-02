/*
 * BaseCommandProcessor.java 
 * --------------------------------------------------------------------------------------
 * Copyright (c) Reveal Technologies, LLC. All rights reserved. http://www.reveal-tech.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.agent;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import com.sitewhere.device.provisioning.protobuf.proto.Sitewhere.Device;
import com.sitewhere.device.provisioning.protobuf.proto.Sitewhere.Device.Header;
import com.sitewhere.device.provisioning.protobuf.proto.Sitewhere.Device.RegistrationAck;
import com.sitewhere.device.provisioning.protobuf.proto.Sitewhere.SiteWhere;

/**
 * Base class for command processing. Handles processing of inbound SiteWhere system
 * messages. Processing of specification commands is left up to subclasses.
 * 
 * @author Derek
 */
public class BaseCommandProcessor implements IAgentCommandProcessor {

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
			case REGISTER_ACK: {
				RegistrationAck ack = RegistrationAck.parseDelimitedFrom(stream);
				handleRegisterAck(header, ack);
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
	}

	/**
	 * Handle the registration acknowledgement message.
	 * 
	 * @param ack
	 * @param originator
	 */
	public void handleRegisterAck(Header header, RegistrationAck ack) {
	}

	/**
	 * Convenience method for sending an acknowledgement event to SiteWhere.
	 * 
	 * @param dispatcher
	 * @param hardwareId
	 * @param originator
	 * @param message
	 * @throws SiteWhereAgentException
	 */
	public void sendAck(ISiteWhereEventDispatcher dispatcher, String hardwareId, String originator,
			String message) throws SiteWhereAgentException {
		SiteWhere.Acknowledge.Builder builder = SiteWhere.Acknowledge.newBuilder();
		SiteWhere.Acknowledge ack = builder.setHardwareId(hardwareId).setMessage(message).build();
		dispatcher.acknowledge(ack, originator);
	}

	/**
	 * Convenience method for sending a measurement event to SiteWhere.
	 * 
	 * @param dispatcher
	 * @param hardwareId
	 * @param originator
	 * @param name
	 * @param value
	 * @throws SiteWhereAgentException
	 */
	public void sendMeasurement(ISiteWhereEventDispatcher dispatcher, String hardwareId, String originator,
			String name, double value) throws SiteWhereAgentException {
		SiteWhere.DeviceMeasurement.Builder mb = SiteWhere.DeviceMeasurement.newBuilder();
		mb.setHardwareId(hardwareId).setMeasurementId(name).setMeasurementValue(value);
		dispatcher.sendMeasurement(mb.build(), originator);
	}

	/**
	 * Convenience method for sending a location event to SiteWhere.
	 * 
	 * @param dispatcher
	 * @param hardwareId
	 * @param originator
	 * @param latitude
	 * @param longitude
	 * @param elevation
	 * @throws SiteWhereAgentException
	 */
	public void sendLocation(ISiteWhereEventDispatcher dispatcher, String hardwareId, String originator,
			double latitude, double longitude, double elevation) throws SiteWhereAgentException {
		SiteWhere.DeviceLocation.Builder lb = SiteWhere.DeviceLocation.newBuilder();
		lb.setHardwareId(hardwareId).setLatitude(latitude).setLongitude(longitude).setElevation(elevation);
		dispatcher.sendLocation(lb.build(), originator);
	}

	/**
	 * Convenience method for sending an alert event to SiteWhere.
	 * 
	 * @param dispatcher
	 * @param hardwareId
	 * @param originator
	 * @param alertType
	 * @param message
	 * @throws SiteWhereAgentException
	 */
	public void sendAlert(ISiteWhereEventDispatcher dispatcher, String hardwareId, String originator,
			String alertType, String message) throws SiteWhereAgentException {
		SiteWhere.DeviceAlert.Builder ab = SiteWhere.DeviceAlert.newBuilder();
		ab.setHardwareId(hardwareId).setAlertType(alertType).setAlertMessage(message);
		dispatcher.sendAlert(ab.build(), originator);
	}
}