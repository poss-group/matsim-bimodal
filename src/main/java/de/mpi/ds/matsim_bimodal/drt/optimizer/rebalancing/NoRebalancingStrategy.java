/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2017 by the members listed in the COPYING,        *
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

package de.mpi.ds.matsim_bimodal.drt.optimizer.rebalancing;

import de.mpi.ds.matsim_bimodal.drt.optimizer.rebalancing.RebalancingStrategy;
import org.matsim.contrib.dvrp.fleet.DvrpVehicle;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author michalm
 */
public class NoRebalancingStrategy implements RebalancingStrategy {
	@Override
	public List<Relocation> calcRelocations(Stream<? extends DvrpVehicle> rebalancableVehicles, double time) {
		return Collections.emptyList();
	}
}
