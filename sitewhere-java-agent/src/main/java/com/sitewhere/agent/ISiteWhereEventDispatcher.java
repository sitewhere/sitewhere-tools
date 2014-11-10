/*
 * ISiteWhereEventDispatcher.java 
 * --------------------------------------------------------------------------------------
 * Copyright (c) Reveal Technologies, LLC. All rights reserved. http://www.reveal-tech.com
 *
 * The software in this package is published under the terms of the MIT
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.agent;

import com.sitewhere.device.provisioning.protobuf.proto.Sitewhere.SiteWhere;

/**
 * Interface for events that can be dispatched to SiteWhere server.
 * 
 * @author Derek
 */
public interface ISiteWhereEventDispatcher {

	/**
	 * Register a device.
	 * 
	 * @param register
	 * @param originator
	 * @throws SiteWhereAgentException
	 */
	public void registerDevice(SiteWhere.RegisterDevice register, String originator)
			throws SiteWhereAgentException;

	/**
	 * Send an acknowledgement message.
	 * 
	 * @param ack
	 * @param originator
	 * @throws SiteWhereAgentException
	 */
	public void acknowledge(SiteWhere.Acknowledge ack, String originator) throws SiteWhereAgentException;

	/**
	 * Send a measurement event.
	 * 
	 * @param measurement
	 * @param originator
	 * @throws SiteWhereAgentException
	 */
	public void sendMeasurement(SiteWhere.DeviceMeasurements measurement, String originator)
			throws SiteWhereAgentException;

	/**
	 * Send a location event.
	 * 
	 * @param location
	 * @param originator
	 * @throws SiteWhereAgentException
	 */
	public void sendLocation(SiteWhere.DeviceLocation location, String originator)
			throws SiteWhereAgentException;

	/**
	 * Send an alert event.
	 * 
	 * @param alert
	 * @param originator
	 * @throws SiteWhereAgentException
	 */
	public void sendAlert(SiteWhere.DeviceAlert alert, String originator) throws SiteWhereAgentException;
}