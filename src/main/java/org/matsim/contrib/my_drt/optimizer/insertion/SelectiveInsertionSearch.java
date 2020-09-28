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

package org.matsim.contrib.my_drt.optimizer.insertion;

import org.matsim.contrib.my_drt.optimizer.insertion.InsertionGenerator.Insertion;
import org.matsim.contrib.dvrp.path.OneToManyPathSearch.PathData;
import org.matsim.contrib.my_drt.optimizer.VehicleData;
import org.matsim.contrib.my_drt.passenger.DrtRequest;
import org.matsim.contrib.my_drt.run.DrtConfigGroup;
import org.matsim.core.mobsim.framework.MobsimTimer;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ForkJoinPool;

/**
 * @author michalm
 */
public class SelectiveInsertionSearch implements DrtInsertionSearch<PathData> {

	// step 1: initial filtering out feasible insertions
	private final org.matsim.contrib.my_drt.optimizer.insertion.DetourTimesProvider restrictiveDetourTimesProvider;
	private final BestInsertionFinder<Double> initialInsertionFinder;

	// step 2: finding best insertion
	private final ForkJoinPool forkJoinPool;
	private final org.matsim.contrib.my_drt.optimizer.insertion.DetourPathCalculator detourPathCalculator;
	private final BestInsertionFinder<PathData> bestInsertionFinder;

	public SelectiveInsertionSearch(DetourPathCalculator detourPathCalculator, DrtConfigGroup drtCfg, MobsimTimer timer,
                                    ForkJoinPool forkJoinPool, InsertionCostCalculator.PenaltyCalculator penaltyCalculator) {
		this.detourPathCalculator = detourPathCalculator;
		this.forkJoinPool = forkJoinPool;

		// TODO use more sophisticated DetourTimeEstimator
		double restrictiveBeelineSpeed = ((SelectiveInsertionSearchParams)drtCfg.getDrtInsertionSearchParams()).getRestrictiveBeelineSpeedFactor()
				* drtCfg.getEstimatedDrtSpeed() / drtCfg.getEstimatedBeelineDistanceFactor();

		restrictiveDetourTimesProvider = new DetourTimesProvider(
				DetourTimeEstimator.createBeelineTimeEstimator(restrictiveBeelineSpeed));

		initialInsertionFinder = new BestInsertionFinder<>(
				new InsertionCostCalculator<>(drtCfg, timer, penaltyCalculator, Double::doubleValue));

		bestInsertionFinder = new BestInsertionFinder<>(
				new InsertionCostCalculator<>(drtCfg, timer, penaltyCalculator, PathData::getTravelTime));
	}

	@Override
	public Optional<org.matsim.contrib.my_drt.optimizer.insertion.InsertionWithDetourData<PathData>> findBestInsertion(DrtRequest drtRequest,
                                                                                                                       Collection<VehicleData.Entry> vEntries) {
		org.matsim.contrib.my_drt.optimizer.insertion.InsertionGenerator insertionGenerator = new InsertionGenerator();
		org.matsim.contrib.my_drt.optimizer.insertion.DetourData<Double> restrictiveTimeData = restrictiveDetourTimesProvider.getDetourData(drtRequest);

		// Parallel outer stream over vehicle entries. The inner stream (flatmap) is sequential.
		Optional<Insertion> bestInsertion = forkJoinPool.submit(
				// find best insertion given a stream of insertion with time data
				() -> initialInsertionFinder.findBestInsertion(drtRequest,
						//for each vehicle entry
						vEntries.parallelStream()
								//generate feasible insertions (wrt occupancy limits)
								.flatMap(e -> insertionGenerator.generateInsertions(drtRequest, e).stream())
								//map them to insertions with admissible detour times
								.map(restrictiveTimeData::createInsertionWithDetourData))
						.map(InsertionWithDetourData::getInsertion)).join();

		if (bestInsertion.isEmpty()) {
			return Optional.empty();
		}

		//compute actual path
		DetourData<PathData> pathData = detourPathCalculator.calculatePaths(drtRequest, List.of(bestInsertion.get()));
		return bestInsertionFinder.findBestInsertion(drtRequest, bestInsertion.
				stream().map(pathData::createInsertionWithDetourData));
	}
}
