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

package de.mpi.ds.matsim_bimodal.drt.optimizer;

import com.google.common.collect.ImmutableList;
import de.mpi.ds.matsim_bimodal.drt.optimizer.VehicleData;
import de.mpi.ds.matsim_bimodal.drt.optimizer.VehicleData.Entry;
import de.mpi.ds.matsim_bimodal.drt.optimizer.VehicleData.EntryFactory;
import de.mpi.ds.matsim_bimodal.drt.optimizer.VehicleData.Stop;
import de.mpi.ds.matsim_bimodal.drt.run.DrtConfigGroup;
import de.mpi.ds.matsim_bimodal.drt.schedule.DrtDriveTask;
import de.mpi.ds.matsim_bimodal.drt.schedule.DrtStayTask;
import de.mpi.ds.matsim_bimodal.drt.schedule.DrtStopTask;
import org.matsim.contrib.dvrp.fleet.DvrpVehicle;
import org.matsim.contrib.dvrp.schedule.Schedule;
import org.matsim.contrib.dvrp.schedule.Schedule.ScheduleStatus;
import org.matsim.contrib.dvrp.schedule.Task;
import org.matsim.contrib.dvrp.tracker.OnlineDriveTaskTracker;
import org.matsim.contrib.dvrp.util.LinkTimePair;

import java.util.ArrayList;
import java.util.List;

import static de.mpi.ds.matsim_bimodal.drt.schedule.DrtTaskBaseType.STOP;
import static de.mpi.ds.matsim_bimodal.drt.schedule.DrtTaskBaseType.getBaseType;

/**
 * @author michalm
 */
public class VehicleDataEntryFactoryImpl implements EntryFactory {
	private final double lookAhead;

	public VehicleDataEntryFactoryImpl(DrtConfigGroup drtCfg) {
		lookAhead = drtCfg.getMaxWaitTime() - drtCfg.getStopDuration();
		if (lookAhead < 0) {
			throw new IllegalArgumentException(
					DrtConfigGroup.MAX_WAIT_TIME + " must not be smaller than " + DrtConfigGroup.STOP_DURATION);
		}
	}

	public Entry create(DvrpVehicle vehicle, double currentTime) {
		if (!isEligibleForRequestInsertion(vehicle, currentTime)) {
			return null;
		}

		Schedule schedule = vehicle.getSchedule();
		final LinkTimePair start;
		final Task startTask;
		int nextTaskIdx;
		if (schedule.getStatus() == ScheduleStatus.STARTED) {
			startTask = schedule.getCurrentTask();
			switch (getBaseType(startTask)) {
				case DRIVE:
					DrtDriveTask driveTask = (DrtDriveTask)startTask;
					LinkTimePair diversionPoint = ((OnlineDriveTaskTracker)driveTask.getTaskTracker()).getDiversionPoint();
					start = diversionPoint != null ? diversionPoint : //diversion possible
							new LinkTimePair(driveTask.getPath().getToLink(),
									driveTask.getEndTime());// too late for diversion

					break;

				case STOP:
					DrtStopTask stopTask = (DrtStopTask)startTask;
					start = new LinkTimePair(stopTask.getLink(), stopTask.getEndTime());
					break;

				case STAY:
					DrtStayTask stayTask = (DrtStayTask)startTask;
					start = new LinkTimePair(stayTask.getLink(), currentTime);
					break;

				default:
					throw new RuntimeException();
			}

			nextTaskIdx = startTask.getTaskIdx() + 1;
		} else { // PLANNED
			start = new LinkTimePair(vehicle.getStartLink(), vehicle.getServiceBeginTime());
			startTask = null;
			nextTaskIdx = 0;
		}

		List<? extends Task> tasks = schedule.getTasks();
		List<DrtStopTask> stopTasks = new ArrayList<>();
		for (Task task : tasks.subList(nextTaskIdx, tasks.size())) {
			if (STOP.isBaseTypeOf(task)) {
				stopTasks.add((DrtStopTask)task);
			}
		}

		Stop[] stops = new Stop[stopTasks.size()];
		int outputOccupancy = 0;
		for (int i = stops.length - 1; i >= 0; i--) {
			Stop s = stops[i] = new Stop(stopTasks.get(i), outputOccupancy);
			outputOccupancy -= s.occupancyChange;
		}

		return new Entry(vehicle, new VehicleData.Start(startTask, start.link, start.time, outputOccupancy),
				ImmutableList.copyOf(stops));
	}

	public boolean isEligibleForRequestInsertion(DvrpVehicle vehicle, double currentTime) {
		return !(currentTime + lookAhead < vehicle.getServiceBeginTime() || currentTime >= vehicle.getServiceEndTime());
	}
}
