package de.mpi.ds.my_analysis;

import org.apache.log4j.Logger;
import org.matsim.core.controler.AbstractModule;

public class MyAnalysisModule extends AbstractModule {
    private final static Logger LOG = Logger.getLogger(MyAnalysisModule.class.getName());

    @Override
    public void install() {
        LOG.info("Initiating");
        this.addControlerListenerBinding().to(ServabilityListener.class);

        PtVehicleDistanceTracker ptVehicleDistanceTracker = new PtVehicleDistanceTracker();
        this.addEventHandlerBinding().toInstance(ptVehicleDistanceTracker);
        this.addControlerListenerBinding().toInstance(ptVehicleDistanceTracker);
        LOG.info("Finalizing");
    }
}
