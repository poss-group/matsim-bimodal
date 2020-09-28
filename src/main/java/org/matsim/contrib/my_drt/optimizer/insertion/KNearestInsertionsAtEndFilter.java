/* *********************************************************************** *
 * project: org.matsim.*
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2018 by the members listed in the COPYING,        *
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

package org.matsim.contrib.my_drt.optimizer.insertion;

import org.matsim.contrib.my_drt.optimizer.insertion.InsertionGenerator.Insertion;
import org.matsim.contrib.my_drt.optimizer.VehicleData;
import org.matsim.contrib.util.PartialSort;

import java.util.List;

/**
 * "Insertion at end" means appending both pickup and dropoff at the end of the schedule, which means the ride
 * is not shared (like a normal taxi). In this case, the best insertion-at-end is the one that is closest in time,
 * so we just select the nearest (in straight-line) for the MultiNodeDijkstra (OneToManyPathSearch)
 *
 * @author michalm
 */
class KNearestInsertionsAtEndFilter {
	// synchronised addition via addInsertionAtEndCandidate(Insertion insertionAtEnd, double timeDistance)
	private final PartialSort<Insertion> nearestInsertionsAtEnd;

	private final double admissibleBeelineSpeedFactor;

	public KNearestInsertionsAtEndFilter(int k, double admissibleBeelineSpeedFactor) {
		nearestInsertionsAtEnd = new PartialSort<>(k);
		this.admissibleBeelineSpeedFactor = admissibleBeelineSpeedFactor;
	}

	/**
	 * Designed to be used with parallel streams
	 */
	boolean filter(InsertionWithDetourData<Double> insertion) {
		VehicleData.Entry vEntry = insertion.getVehicleEntry();
		int i = insertion.getPickup().index;

		if (i < vEntry.stops.size()) {//not an insertion at the schedule end
			return true;
		}

		//i == j == stops.size()
		double departureTime = vEntry.getWaypoint(i).getDepartureTime();

		// x ADMISSIBLE_BEELINE_SPEED_FACTOR to remove bias towards near but still busy vehicles
		// (timeToPickup is underestimated by this factor)
		double timeDistance = departureTime + admissibleBeelineSpeedFactor * insertion.getDetourToPickup();
		addInsertionAtEndCandidate(insertion.getInsertion(), timeDistance);
		return false;//skip now; the selected (i.e. K nearest) insertions will be added later
	}

	public List<Insertion> getNearestInsertionsAtEnd() {
		return nearestInsertionsAtEnd.kSmallestElements();
	}

	//synchronized -- allows filtering of parallel streams
	private synchronized void addInsertionAtEndCandidate(Insertion insertionAtEnd, double timeDistance) {
		nearestInsertionsAtEnd.add(insertionAtEnd, timeDistance);
	}
}
