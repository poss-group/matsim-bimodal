/*
 * *********************************************************************** *
 * project: org.matsim.*
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2019 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** *
 */

package de.mpi.ds.matsim_bimodal.drt.routing;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Identifiable;
import org.matsim.api.core.v01.network.Link;
import de.mpi.ds.matsim_bimodal.drt.routing.DrtStopFacility;
import org.matsim.facilities.Facility;

import java.util.Map;

/**
 * @author Michal Maciejewski (michalm)
 */
public class DrtStopFacilityImpl implements DrtStopFacility {
	public static <F extends Identifiable<?> & Facility> DrtStopFacility createFromIdentifiableFacility(F facility) {
		return new de.mpi.ds.matsim_bimodal.drt.routing.DrtStopFacilityImpl(Id.create(facility.getId(), DrtStopFacility.class), facility.getLinkId(),
				facility.getCoord());
	}

	public static DrtStopFacility createFromLink(Link link) {
		return new de.mpi.ds.matsim_bimodal.drt.routing.DrtStopFacilityImpl(Id.create(link.getId(), DrtStopFacility.class), link.getId(), link.getCoord());
	}

	private final Id<DrtStopFacility> id;
	private final Id<Link> linkId;
	private final Coord coord;

	public DrtStopFacilityImpl(Id<DrtStopFacility> id, Id<Link> linkId, Coord coord) {
		this.id = id;
		this.linkId = linkId;
		this.coord = coord;
	}

	@Override
	public Id<DrtStopFacility> getId() {
		return id;
	}

	@Override
	public Id<Link> getLinkId() {
		return linkId;
	}

	@Override
	public Coord getCoord() {
		return coord;
	}

	@Override
	public Map<String, Object> getCustomAttributes() {
		throw new UnsupportedOperationException();
	}
}
