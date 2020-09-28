/*
 * *********************************************************************** *
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
 * *********************************************************************** *
 */

package org.matsim.contrib.my_drt.run;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Named;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.my_drt.optimizer.QSimScopeForkJoinPoolHolder;
import org.matsim.contrib.my_drt.optimizer.depot.DepotFinder;
import org.matsim.contrib.my_drt.optimizer.depot.NearestStartLinkAsDepot;
import org.matsim.contrib.my_drt.optimizer.insertion.DrtInsertionSearch;
import org.matsim.contrib.my_drt.optimizer.rebalancing.RebalancingStrategy;
import org.matsim.contrib.my_drt.passenger.DrtRequestCreator;
import org.matsim.contrib.my_drt.schedule.DrtStayTaskEndTimeCalculator;
import org.matsim.contrib.my_drt.schedule.DrtTaskFactory;
import org.matsim.contrib.my_drt.schedule.DrtTaskFactoryImpl;
import org.matsim.contrib.my_drt.scheduler.DrtScheduleInquiry;
import org.matsim.contrib.my_drt.scheduler.EmptyVehicleRelocator;
import org.matsim.contrib.my_drt.scheduler.RequestInsertionScheduler;
import org.matsim.contrib.my_drt.vrpagent.DrtActionCreator;
import org.matsim.contrib.dvrp.fleet.Fleet;
import org.matsim.contrib.dvrp.optimizer.VrpOptimizer;
import org.matsim.contrib.dvrp.passenger.*;
import org.matsim.contrib.dvrp.path.OneToManyPathSearch.PathData;
import org.matsim.contrib.dvrp.run.AbstractDvrpModeQSimModule;
import org.matsim.contrib.dvrp.run.DvrpConfigGroup;
import org.matsim.contrib.dvrp.run.ModalProviders;
import org.matsim.contrib.dvrp.schedule.ScheduleTimingUpdater;
import org.matsim.contrib.dvrp.trafficmonitoring.DvrpTravelTimeModule;
import org.matsim.contrib.dvrp.vrpagent.VrpAgentLogic;
import org.matsim.contrib.dvrp.vrpagent.VrpAgentSourceQSimModule;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.mobsim.framework.MobsimTimer;
import org.matsim.core.router.costcalculators.TravelDisutilityFactory;
import org.matsim.core.router.util.TravelDisutility;
import org.matsim.core.router.util.TravelTime;

/**
 * @author Michal Maciejewski (michalm)
 */
public class DrtModeQSimModule extends AbstractDvrpModeQSimModule {
	private final org.matsim.contrib.my_drt.run.DrtConfigGroup drtCfg;

	public DrtModeQSimModule(org.matsim.contrib.my_drt.run.DrtConfigGroup drtCfg) {
		super(drtCfg.getMode());
		this.drtCfg = drtCfg;
	}

	@Override
	protected void configureQSim() {
		install(new VrpAgentSourceQSimModule(getMode()));
		install(new PassengerEngineQSimModule(getMode()));

		addModalComponent(org.matsim.contrib.my_drt.optimizer.DrtOptimizer.class, modalProvider(
				getter -> new org.matsim.contrib.my_drt.optimizer.DefaultDrtOptimizer(drtCfg, getter.getModal(Fleet.class), getter.get(MobsimTimer.class),
						getter.getModal(DepotFinder.class), getter.getModal(RebalancingStrategy.class),
						getter.getModal(DrtScheduleInquiry.class), getter.getModal(ScheduleTimingUpdater.class),
						getter.getModal(EmptyVehicleRelocator.class),
						getter.getModal(org.matsim.contrib.my_drt.optimizer.insertion.UnplannedRequestInserter.class))));

		bindModal(DepotFinder.class).toProvider(
				modalProvider(getter -> new NearestStartLinkAsDepot(getter.getModal(Fleet.class))));

		bindModal(PassengerRequestValidator.class).to(DefaultPassengerRequestValidator.class).asEagerSingleton();

		addModalComponent(org.matsim.contrib.my_drt.optimizer.QSimScopeForkJoinPoolHolder.class,
				() -> new org.matsim.contrib.my_drt.optimizer.QSimScopeForkJoinPoolHolder(drtCfg.getNumberOfThreads()));

		bindModal(org.matsim.contrib.my_drt.optimizer.insertion.UnplannedRequestInserter.class).toProvider(modalProvider(
				getter -> new org.matsim.contrib.my_drt.optimizer.insertion.DefaultUnplannedRequestInserter(drtCfg, getter.getModal(Fleet.class),
						getter.get(MobsimTimer.class), getter.get(EventsManager.class),
						getter.getModal(RequestInsertionScheduler.class),
						getter.getModal(org.matsim.contrib.my_drt.optimizer.VehicleData.EntryFactory.class),
						getter.getModal(new TypeLiteral<DrtInsertionSearch<PathData>>() {
						}), getter.getModal(QSimScopeForkJoinPoolHolder.class).getPool()))).asEagerSingleton();

		install(getInsertionSearchQSimModule(drtCfg));

		bindModal(org.matsim.contrib.my_drt.optimizer.VehicleData.EntryFactory.class).toInstance(new org.matsim.contrib.my_drt.optimizer.VehicleDataEntryFactoryImpl(drtCfg));

		bindModal(org.matsim.contrib.my_drt.optimizer.insertion.InsertionCostCalculator.PenaltyCalculator.class).to(
				drtCfg.isRejectRequestIfMaxWaitOrTravelTimeViolated() ?
						org.matsim.contrib.my_drt.optimizer.insertion.InsertionCostCalculator.RejectSoftConstraintViolations.class :
						org.matsim.contrib.my_drt.optimizer.insertion.InsertionCostCalculator.DiscourageSoftConstraintViolations.class).asEagerSingleton();

		bindModal(DrtTaskFactory.class).toInstance(new DrtTaskFactoryImpl());

		bindModal(EmptyVehicleRelocator.class).toProvider(new ModalProviders.AbstractProvider<>(drtCfg.getMode()) {
			@Inject
			@Named(DvrpTravelTimeModule.DVRP_ESTIMATED)
			private TravelTime travelTime;

			@Inject
			private MobsimTimer timer;

			@Override
			public EmptyVehicleRelocator get() {
				Network network = getModalInstance(Network.class);
				DrtTaskFactory taskFactory = getModalInstance(DrtTaskFactory.class);
				TravelDisutility travelDisutility = getModalInstance(
						TravelDisutilityFactory.class).createTravelDisutility(travelTime);
				return new EmptyVehicleRelocator(network, travelTime, travelDisutility, timer, taskFactory);
			}
		}).asEagerSingleton();

		bindModal(DrtScheduleInquiry.class).to(DrtScheduleInquiry.class).asEagerSingleton();

		bindModal(RequestInsertionScheduler.class).toProvider(modalProvider(
				getter -> new RequestInsertionScheduler(drtCfg, getter.getModal(Fleet.class),
						getter.get(MobsimTimer.class),
						getter.getNamed(TravelTime.class, DvrpTravelTimeModule.DVRP_ESTIMATED),
						getter.getModal(ScheduleTimingUpdater.class), getter.getModal(DrtTaskFactory.class))))
				.asEagerSingleton();

		bindModal(ScheduleTimingUpdater.class).toProvider(modalProvider(
				getter -> new ScheduleTimingUpdater(getter.get(MobsimTimer.class),
						new DrtStayTaskEndTimeCalculator(drtCfg)))).asEagerSingleton();

		bindModal(VrpAgentLogic.DynActionCreator.class).
				toProvider(modalProvider(getter -> new DrtActionCreator(getter.getModal(PassengerHandler.class),
						getter.get(MobsimTimer.class), getter.get(DvrpConfigGroup.class)))).
				asEagerSingleton();

		bindModal(PassengerRequestCreator.class).toProvider(new Provider<DrtRequestCreator>() {
			@Inject
			private EventsManager events;
			@Inject
			private MobsimTimer timer;

			@Override
			public DrtRequestCreator get() {
				return new DrtRequestCreator(getMode(), events, timer);
			}
		}).asEagerSingleton();

		bindModal(VrpOptimizer.class).to(modalKey(org.matsim.contrib.my_drt.optimizer.DrtOptimizer.class));
	}

	public static AbstractDvrpModeQSimModule getInsertionSearchQSimModule(DrtConfigGroup drtCfg) {
		switch (drtCfg.getDrtInsertionSearchParams().getName()) {
			case org.matsim.contrib.my_drt.optimizer.insertion.ExtensiveInsertionSearchParams.SET_NAME:
				return new org.matsim.contrib.my_drt.optimizer.insertion.ExtensiveInsertionSearchQSimModule(drtCfg);

			case org.matsim.contrib.my_drt.optimizer.insertion.SelectiveInsertionSearchParams.SET_NAME:
				return new org.matsim.contrib.my_drt.optimizer.insertion.SelectiveInsertionSearchQSimModule(drtCfg);

			default:
				throw new RuntimeException(
						"Unsupported DRT insertion search type: " + drtCfg.getDrtInsertionSearchParams().getName());
		}
	}
}
