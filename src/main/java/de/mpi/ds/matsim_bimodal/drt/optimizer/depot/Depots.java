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

package de.mpi.ds.matsim_bimodal.drt.optimizer.depot;

import org.matsim.api.core.v01.network.Link;
import de.mpi.ds.matsim_bimodal.drt.schedule.DrtStayTask;
import org.matsim.contrib.dvrp.fleet.DvrpVehicle;
import org.matsim.contrib.dvrp.schedule.Schedule;
import org.matsim.contrib.dvrp.schedule.Schedule.ScheduleStatus;
import org.matsim.contrib.dvrp.schedule.Task;
import org.matsim.contrib.util.distance.DistanceUtils;

import java.util.Comparator;
import java.util.Set;

import static de.mpi.ds.matsim_bimodal.drt.schedule.DrtTaskBaseType.STAY;
import static de.mpi.ds.matsim_bimodal.drt.schedule.DrtTaskBaseType.STOP;

/**
 * @author michalm
 */
public class Depots {
	public static boolean isSwitchingFromStopToStay(DvrpVehicle vehicle) {
		Schedule schedule = vehicle.getSchedule();

		// only active vehicles
		if (schedule.getStatus() != ScheduleStatus.STARTED) {
			return false;
		}

		// current task is STAY
		Task currentTask = schedule.getCurrentTask();
		if (!STAY.isBaseTypeOf(currentTask)) {
			return false;
		}

		// previous task was STOP
		int previousTaskIdx = currentTask.getTaskIdx() - 1;
		return (previousTaskIdx >= 0 && STOP.isBaseTypeOf(schedule.getTasks().get(previousTaskIdx)));
	}

	public static Link findStraightLineNearestDepot(DvrpVehicle vehicle, Set<Link> links) {
		Link currentLink = ((DrtStayTask)vehicle.getSchedule().getCurrentTask()).getLink();
		return links.contains(currentLink) ?
				null /* already at a depot*/ :
				links.stream()
						.min(Comparator.comparing(
								l -> DistanceUtils.calculateSquaredDistance(currentLink.getCoord(), l.getCoord())))
						.get();
	}
}
