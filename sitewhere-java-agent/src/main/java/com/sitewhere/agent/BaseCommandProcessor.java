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
}