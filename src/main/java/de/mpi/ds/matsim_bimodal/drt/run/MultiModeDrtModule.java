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

package de.mpi.ds.matsim_bimodal.drt.run;

import com.google.inject.Inject;
import de.mpi.ds.matsim_bimodal.drt.analysis.DrtModeAnalysisModule;
import de.mpi.ds.matsim_bimodal.drt.routing.MultiModeDrtMainModeIdentifier;
import de.mpi.ds.matsim_bimodal.drt.run.DrtConfigGroup;
import de.mpi.ds.matsim_bimodal.drt.run.DrtModeModule;
import de.mpi.ds.matsim_bimodal.drt.run.DrtModeQSimModule;
import de.mpi.ds.matsim_bimodal.drt.run.MultiModeDrtConfigGroup;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.router.MainModeIdentifier;

/**
 * @author jbischoff
 * @author michalm (Michal Maciejewski)
 */
public final class MultiModeDrtModule extends AbstractModule {

	@Inject
	private MultiModeDrtConfigGroup multiModeDrtCfg;

	@Override
	public void install() {
		for (DrtConfigGroup drtCfg : multiModeDrtCfg.getModalElements()) {
			install(new DrtModeModule(drtCfg));
			installQSimModule(new DrtModeQSimModule(drtCfg));
			install(new DrtModeAnalysisModule(drtCfg));
		}

		bind(MainModeIdentifier.class).toInstance(new MultiModeDrtMainModeIdentifier(multiModeDrtCfg));
	}
}
