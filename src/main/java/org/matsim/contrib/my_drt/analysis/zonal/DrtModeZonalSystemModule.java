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

package org.matsim.contrib.my_drt.analysis.zonal;

import one.util.streamex.EntryStream;
import org.locationtech.jts.geom.prep.PreparedGeometry;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.dvrp.run.AbstractDvrpModeModule;
import org.matsim.contrib.my_drt.analysis.DrtRequestAnalyzer;
import org.matsim.contrib.my_drt.run.DrtConfigGroup;
import org.matsim.core.controler.MatsimServices;

import java.util.List;

import static org.matsim.contrib.my_drt.analysis.zonal.DrtGridUtils.createGridFromNetwork;
import static org.matsim.contrib.my_drt.analysis.zonal.DrtGridUtils.createGridFromNetworkWithinServiceArea;
import static org.matsim.utils.gis.shp2matsim.ShpGeometryUtils.loadPreparedGeometries;

/**
 * @author Michal Maciejewski (michalm)
 */
public class DrtModeZonalSystemModule extends AbstractDvrpModeModule {

	private final org.matsim.contrib.my_drt.run.DrtConfigGroup drtCfg;

	public DrtModeZonalSystemModule(org.matsim.contrib.my_drt.run.DrtConfigGroup drtCfg) {
		super(drtCfg.getMode());
		this.drtCfg = drtCfg;
	}

	@Override
	public void install() {
		DrtZonalSystemParams params = drtCfg.getZonalSystemParams().orElseThrow();

		bindModal(DrtZonalSystem.class).toProvider(modalProvider(getter -> {
			Network network = getter.getModal(Network.class);
			switch (params.getZonesGeneration()) {
				case ShapeFile:
					final List<PreparedGeometry> preparedGeometries = loadPreparedGeometries(
							params.getZonesShapeFileURL(getConfig().getContext()));
					return DrtZonalSystem.createFromPreparedGeometries(network,
							EntryStream.of(preparedGeometries).mapKeys(i -> (i + 1) + "").toMap());

				case GridFromNetwork:
					var gridZones = drtCfg.getOperationalScheme() == DrtConfigGroup.OperationalScheme.serviceAreaBased ?
							createGridFromNetworkWithinServiceArea(network, params.getCellSize(),
									loadPreparedGeometries(
											drtCfg.getDrtServiceAreaShapeFileURL(getConfig().getContext()))) :
							createGridFromNetwork(network, params.getCellSize());
					return DrtZonalSystem.createFromPreparedGeometries(network, gridZones);

				default:
					throw new RuntimeException("Unsupported zone generation");
			}
		})).asEagerSingleton();

		bindModal(DrtZoneTargetLinkSelector.class).toProvider(modalProvider(getter -> {
				switch (params.getTargetLinkSelection()) {
					case mostCentral:
						return new MostCentralDrtZoneTargetLinkSelector(getter.getModal(DrtZonalSystem.class));
					case random:
						return new RandomDrtZoneTargetLinkSelector();
					default:
						throw new RuntimeException("Unsupported target link selection = " + params.getTargetLinkSelection());
				}
		})).asEagerSingleton();

		//zonal analysis
		bindModal(ZonalIdleVehicleXYVisualiser.class).
				toProvider(modalProvider(
						getter -> new ZonalIdleVehicleXYVisualiser(getter.get(MatsimServices.class), drtCfg.getMode(),
								getter.getModal(DrtZonalSystem.class)))).asEagerSingleton();
		addControlerListenerBinding().to(modalKey(ZonalIdleVehicleXYVisualiser.class));
		addEventHandlerBinding().to(modalKey(ZonalIdleVehicleXYVisualiser.class));

		bindModal(DrtZonalWaitTimesAnalyzer.class).toProvider(modalProvider(
				getter -> new DrtZonalWaitTimesAnalyzer(drtCfg, getter.getModal(DrtRequestAnalyzer.class),
						getter.getModal(DrtZonalSystem.class)))).asEagerSingleton();
		addControlerListenerBinding().to(modalKey(DrtZonalWaitTimesAnalyzer.class));
		addEventHandlerBinding().to(modalKey(DrtZonalWaitTimesAnalyzer.class));
	}
}
