/*------------------------------------------------------------------------
 * 
 * copyright : (C) 2008 by Benjamin Mueller 
 * email     : news@fork.ch
 * website   : http://sourceforge.net/projects/adhocrailway
 * version   : $Id: SRCPRoute.java,v 1.1 2008-04-24 07:29:51 fork_ch Exp $
 * 
 *----------------------------------------------------------------------*/

/*------------------------------------------------------------------------
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 *----------------------------------------------------------------------*/

package de.dermoba.srcp.model.routes;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SRCPRoute {

	private String id =UUID.randomUUID().toString();

	private SRCPRouteState routeState = SRCPRouteState.UNDEF;

	private List<SRCPRouteItem> routeItems;

	public SRCPRoute() {
		routeItems = new ArrayList<SRCPRouteItem>();
	}

	public SRCPRouteState getRouteState() {
		return routeState;
	}

	public void setRouteState(final SRCPRouteState routeState) {
		this.routeState = routeState;
	}

	public List<SRCPRouteItem> getRouteItems() {
		return routeItems;
	}

	public void setRouteItems(final List<SRCPRouteItem> routeItems) {
		this.routeItems = routeItems;
	}

	public void addRouteItem(final SRCPRouteItem item) {
		this.routeItems.add(item);
	}

	@Override
	public int hashCode() {
		HashCodeBuilder hashCodeBuilder = new HashCodeBuilder();
		hashCodeBuilder.append(id);
		return hashCodeBuilder.hashCode();

	}

	@Override
	public boolean equals(final Object obj) {
		if(!(obj instanceof  SRCPRoute)) {
			return false;
		}
		SRCPRoute rhs = (SRCPRoute) obj;
		EqualsBuilder equalsBuilder = new EqualsBuilder();
		equalsBuilder.append(id, rhs.id);
		return equalsBuilder.build();
	}
}
