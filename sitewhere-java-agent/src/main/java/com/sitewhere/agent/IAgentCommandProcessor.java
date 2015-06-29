/*
 * Copyright (c) SiteWhere LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the MIT
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.agent;

/**
 * Interface for classes that process commands for an agent.
 * 
 * @author Derek
 */
public interface IAgentCommandProcessor {

	/**
	 * Executes logic that happens before the standard processing loop.
	 * 
	 * @param hardwareId
	 * @param specificationToken
	 * @param dispatcher
	 * @throws SiteWhereAgentException
	 */
	public void executeStartupLogic(String hardwareId, String specificationToken,
			ISiteWhereEventDispatcher dispatcher) throws SiteWhereAgentException;

	/**
	 * Process a SiteWhere system command.
	 * 
	 * @param message
	 * @param dispatcher
	 * @throws SiteWhereAgentException
	 */
	public void processSiteWhereCommand(byte[] message, ISiteWhereEventDispatcher dispatcher)
			throws SiteWhereAgentException;

	/**
	 * Process a specification command.
	 * 
	 * @param message
	 * @param dispatcher
	 * @throws SiteWhereAgentException
	 */
	public void processSpecificationCommand(byte[] message, ISiteWhereEventDispatcher dispatcher)
			throws SiteWhereAgentException;

	/**
	 * Set based on hardware id configured in agent.
	 * 
	 * @param hardwareId
	 * @throws SiteWhereAgentException
	 */
	public void setHardwareId(String hardwareId) throws SiteWhereAgentException;

	/**
	 * Set based on specification token configured in agent.
	 * 
	 * @param specificationToken
	 * @throws SiteWhereAgentException
	 */
	public void setSpecificationToken(String specificationToken) throws SiteWhereAgentException;

	/**
	 * Set the event dispatcher that allows data to be sent back to SiteWhere.
	 * 
	 * @param dispatcher
	 */
	public void setEventDispatcher(ISiteWhereEventDispatcher dispatcher);
}