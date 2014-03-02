/*
 * SiteWhereAgentException.java 
 * --------------------------------------------------------------------------------------
 * Copyright (c) Reveal Technologies, LLC. All rights reserved. http://www.reveal-tech.com
 *
 * The software in this package is published under the terms of the MIT
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.agent;

public class SiteWhereAgentException extends Exception {

	private static final long serialVersionUID = 3351303154000958250L;

	public SiteWhereAgentException() {
	}

	public SiteWhereAgentException(String message) {
		super(message);
	}

	public SiteWhereAgentException(Throwable error) {
		super(error);
	}

	public SiteWhereAgentException(String message, Throwable error) {
		super(message, error);
	}
}