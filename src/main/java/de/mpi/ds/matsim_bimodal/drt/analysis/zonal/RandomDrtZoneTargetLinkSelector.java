/* *********************************************************************** *
 * project: org.matsim.*
 * Controler.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2007 by the members listed in the COPYING,        *
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
 * *********************************************************************** */

package de.mpi.ds.matsim_bimodal.drt.analysis.zonal;

import org.matsim.api.core.v01.network.Link;
import de.mpi.ds.matsim_bimodal.drt.analysis.zonal.DrtZone;
import de.mpi.ds.matsim_bimodal.drt.analysis.zonal.DrtZoneTargetLinkSelector;
import org.matsim.core.gbl.MatsimRandom;

import java.util.Random;

/**
 * @author tschlenther
 */
public class RandomDrtZoneTargetLinkSelector implements DrtZoneTargetLinkSelector{

	private final Random random = MatsimRandom.getLocalInstance();

	@Override
	public Link selectTargetLink(DrtZone zone) {
		return zone.getLinks().get(random.nextInt(zone.getLinks().size()));
	}

}
