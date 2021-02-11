package de.mpi.ds.utils;

import org.apache.log4j.Logger;
import org.jfree.util.Log;

public class ScenarioCreator {
    private final static Logger LOG = Logger.getLogger(ScenarioCreator.class.getName());

    private NetworkCreator networkCreator;
    private PopulationCreator populationCreator;
    private TransitScheduleCreator transitScheduleCreator;
    private DrtFleetVehiclesCreator drtFleetVehiclesCreator;

    public ScenarioCreator(double systemSize, int systemSizeOverGridPtSize, int systemSizeOverGridSize,
                           long linkCapacity,
                           double freeSpeedCar, double freeSpeedTrain, double freeSpeedTrainForSchedule,
                           double numberOfLanes, int requestEndTime, int nRequests, double transitEndTime,
                           double departureIntervalTime, double transitStopLength, int nDrtVehicles, int drtCapacity,
                           double drtOperationStartTime, double drtOperationEndTime, long seed, String transportMode,
                           boolean isGridNetwork, boolean diagonalConnections) {

        assert systemSizeOverGridSize > systemSizeOverGridPtSize :
                "Pt grid spacing must be bigger than drt grid spacing";
//        assert systemSizeOverGridSize % systemSizeOverGridPtSize == 0 :
//                "Pt grid spacing mus be integer multiple of drt grid spacing";

        this.networkCreator = new NetworkCreator(systemSize, systemSizeOverGridSize, systemSizeOverGridPtSize,
                linkCapacity,
                freeSpeedTrainForSchedule, numberOfLanes, freeSpeedCar, diagonalConnections);
        this.populationCreator = new PopulationCreator(nRequests, requestEndTime, seed, transportMode, isGridNetwork);
        this.transitScheduleCreator = new TransitScheduleCreator(systemSize, systemSizeOverGridPtSize, freeSpeedTrain,
                transitEndTime, transitStopLength, freeSpeedTrainForSchedule, departureIntervalTime);
        this.drtFleetVehiclesCreator = new DrtFleetVehiclesCreator(drtCapacity, drtOperationStartTime,
                drtOperationEndTime, nDrtVehicles);
    }

    public static void main(String... args) {
        ScenarioCreator scenarioCreator = new ScenarioCreatorBuilder().build();

        String netPath = "./output/network_diag.xml.gz";
        String popPath = "./output/population.xml.gz";
        String drtFleetPath = "output/drtvehicles.xml";
        String transitSchedulePath = "output/transitSchedule_15min.xml.gz";
        String transitVehiclesPath = "output/transitVehicles_15min.xml.gz";
        scenarioCreator.createNetwork(netPath, true);
        scenarioCreator.createPopulation(popPath, netPath);
        scenarioCreator.createDrtFleet(netPath, drtFleetPath);
        scenarioCreator.createTransitSchedule(netPath, transitSchedulePath, transitVehiclesPath);
    }

    public void createNetwork(String outputPath, boolean createTrainLines) {
        networkCreator.createGridNetwork(outputPath, createTrainLines);
    }

    public void createPopulation(String outputPopulationPath, String networkPath) {
        populationCreator.createPopulation(outputPopulationPath, networkPath);
    }

    public void createTransitSchedule(String networkPath, String outputSchedulePath, String outputVehiclePath) {
        transitScheduleCreator.runTransitScheduleUtil(networkPath, outputSchedulePath, outputVehiclePath);
    }

    public void createDrtFleet(String networkPath, String outputPath) {
        drtFleetVehiclesCreator.run(networkPath, outputPath);
    }
}