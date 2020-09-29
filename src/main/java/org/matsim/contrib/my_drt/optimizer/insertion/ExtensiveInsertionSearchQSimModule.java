/*
 * *********************************************************************** *
 * project: org.matsim.*
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2020 by the members listed in the COPYING,        *
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

package org.matsim.contrib.my_drt.optimizer.insertion;

import com.google.inject.Inject;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Named;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.dvrp.path.OneToManyPathSearch;
import org.matsim.contrib.dvrp.run.AbstractDvrpModeQSimModule;
import org.matsim.contrib.dvrp.run.ModalProviders;
import org.matsim.contrib.dvrp.trafficmonitoring.DvrpTravelTimeModule;
import org.matsim.contrib.my_drt.optimizer.QSimScopeForkJoinPoolHolder;
import org.matsim.contrib.my_drt.run.DrtConfigGroup;
import org.matsim.core.mobsim.framework.MobsimTimer;
import org.matsim.core.router.costcalculators.TravelDisutilityFactory;
import org.matsim.core.router.util.TravelDisutility;
import org.matsim.core.router.util.TravelTime;

/**
 * @author Michal Maciejewski (michalm)
 */
public class ExtensiveInsertionSearchQSimModule extends AbstractDvrpModeQSimModule {
	private final org.matsim.contrib.my_drt.run.DrtConfigGroup drtCfg;

	public ExtensiveInsertionSearchQSimModule(DrtConfigGroup drtCfg) {
		super(drtCfg.getMode());
		this.drtCfg = drtCfg;
	}

	@Override
	protected void configureQSim() {
		bindModal(new TypeLiteral<DrtInsertionSearch<OneToManyPathSearch.PathData>>() {
		}).toProvider(modalProvider(
				getter -> new ExtensiveInsertionSearch(getter.getModal(org.matsim.contrib.my_drt.optimizer.insertion.DetourPathCalculator.class), drtCfg,
						getter.get(MobsimTimer.class), getter.getModal(QSimScopeForkJoinPoolHolder.class).getPool(),
						getter.getModal(InsertionCostCalculator.PenaltyCalculator.class))));

		addModalComponent(org.matsim.contrib.my_drt.optimizer.insertion.MultiInsertionDetourPathCalculator.class, new ModalProviders.AbstractProvider<>(getMode()) {
			@Inject
			@Named(DvrpTravelTimeModule.DVRP_ESTIMATED)
			private TravelTime travelTime;

			@Override
			public org.matsim.contrib.my_drt.optimizer.insertion.MultiInsertionDetourPathCalculator get() {
				Network network = getModalInstance(Network.class);
				TravelDisutility travelDisutility = getModalInstance(
						TravelDisutilityFactory.class).createTravelDisutility(travelTime);
				return new org.matsim.contrib.my_drt.optimizer.insertion.MultiInsertionDetourPathCalculator(network, travelTime, travelDisutility, drtCfg);
			}
		});
		bindModal(DetourPathCalculator.class).to(modalKey(MultiInsertionDetourPathCalculator.class));
	}
}