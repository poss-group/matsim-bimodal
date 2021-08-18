package de.mpi.ds;

import de.mpi.ds.DrtTrajectoryAnalyzer.DrtTrajectoryAnalyzer;
import de.mpi.ds.custom_routing.CustomRoutingModule;
import de.mpi.ds.custom_transit_stop_handler.CustomTransitStopHandlerModule;
import de.mpi.ds.drt_plan_modification.DrtPlanModifier;
import de.mpi.ds.drt_plan_modification.DrtPlanModifierConfigGroup;
import de.mpi.ds.my_analysis.MyAnalysisModule;
import de.mpi.ds.osm_utils.ScenarioCreatorBuilderOsm;
import de.mpi.ds.osm_utils.ScenarioCreatorOsm;
import de.mpi.ds.parking_vehicles_tracker.ParkingVehicleTracker;
import de.mpi.ds.utils.ScenarioCreator;
import de.mpi.ds.utils.ScenarioCreatorBuilder;
import org.apache.log4j.Logger;
import org.matsim.contrib.drt.run.DrtConfigGroup;
import org.matsim.contrib.drt.run.DrtControlerCreator;
import org.matsim.contrib.drt.run.MultiModeDrtConfigGroup;
import org.matsim.contrib.dvrp.run.DvrpConfigGroup;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryLogging;
import org.matsim.vis.otfvis.OTFVisConfigGroup;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;

public class MatsimMain {

    private static final Logger LOG = Logger.getLogger(MatsimMain.class.getName());

    public static void main(String[] args) {
        LOG.info("Reading config");
        Config config = ConfigUtils.loadConfig(args[0], new MultiModeDrtConfigGroup(), new DvrpConfigGroup(),
                new DrtPlanModifierConfigGroup(), new OTFVisConfigGroup());
//      Config config = ConfigUtils.loadConfig(args[0], new OTFVisConfigGroup());
//        config.global().setNumberOfThreads(1);

        LOG.info("Starting matsim simulation...");
        try {
//            CoordinateTransformation coordinateTransformation = TransformationFactory
//                    .getCoordinateTransformation(TransformationFactory.WGS84, "EPSG:25832");
//            Network network = new SupersonicOsmNetworkReader.Builder()
//                    .setCoordinateTransformation(coordinateTransformation)
//                    .build().read("/home/helge/Applications/osm-data/out_osmosis.osm.pbf");
//            new NetworkWriter(network).write("test_network.xml");

//            runMultipleNDrt(config, args[1], args[2], args[3], false);
//            runMultipleConvCrit(config, args[1], args[2], args[3], args[4], false);
            runMultipleNetworks(config, args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9],
                    args[10], args[11], args[12], args[13], args[14], args[15]);
//            manuallyStartMultipleNeworks(args[0]);
//            runMulitpleDeltaMax(config, args[1], args[2]);
//            manuallyStartMultipleDeltaMax(args[0]);
//            runRealWorldScenario(config, args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8],
//                    args[9], args[10]);
//            run(config, false, false);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        LOG.info("Simulation finished");
    }

//    private static void manuallyStartMultipleDeltaMax(String configPath) throws Exception {
//        Config config = ConfigUtils.loadConfig(configPath, new MultiModeDrtConfigGroup(), new DvrpConfigGroup(),
//                new DrtPlanModifierConfigGroup(), new OTFVisConfigGroup());
//        runMulitpleDeltaMax(config, "1.0", "create-input");
//        for (double deltaMax = 1.1; deltaMax < 2.1; deltaMax += 1.1) {
//            config = ConfigUtils.loadConfig(configPath, new MultiModeDrtConfigGroup(), new DvrpConfigGroup(),
//                    new DrtPlanModifierConfigGroup(), new OTFVisConfigGroup());
//            runMulitpleDeltaMax(config, String.valueOf(Math.round(deltaMax * 10) / 10.), "unimodal");
//        }
//    }

//    private static void runMulitpleDeltaMax(Config config, String deltaMaxString, String mode) throws Exception {
//        System.out.println("DeltaMax: " + deltaMaxString);
//        String basicOutPath = config.controler().getOutputDirectory();
//        String varyParameter = "DeltaMax_";
//        double deltaMax = Double.parseDouble(deltaMaxString);
//        String iterationSpecificPath = Paths.get(basicOutPath, varyParameter + deltaMaxString).toString();
//        String inputPath = Paths.get(basicOutPath, "input").toString();
//        String networkPath = Paths.get(inputPath, "network_input.xml.gz").toString();
//        String populationPath = Paths.get(inputPath, "population_input.xml.gz").toString();
//        String transitSchedulePath = Paths.get(inputPath, "transitSchedule_input.xml.gz").toString();
//        String transitVehiclesPath = Paths.get(inputPath, "transitVehicles_input.xml.gz").toString();
//        String drtFleetPath = Paths.get(inputPath, "drtvehicles_input.xml.gz").toString();
//
//        config.network().setInputFile(networkPath);
//        config.plans().setInputFile(populationPath);
//        config.transit().setTransitScheduleFile(transitSchedulePath);
//        config.transit().setVehiclesFile(transitVehiclesPath);
//        MultiModeDrtConfigGroup.get(config).getModalElements().stream().findFirst().orElseThrow()
//                .setVehiclesFile(drtFleetPath);
//
//        String outPath = iterationSpecificPath;
//        double endTime = 3 * 24 * 3600;
//        if (mode.equals("create-input")) {
//            ScenarioCreator scenarioCreator = new ScenarioCreatorBuilder().setCarGridSpacing(100).setSystemSize(10000)
//                    .setSmallLinksCloseToNodes(true).setdrtFleetSize(70).setDrtCapacity(10000)
//                    .setNRequests((int) (3 * 1e5))
//                    .setRequestEndTime((int) endTime).setSmallLinksCloseToNodes(true)
//                    .setDrtOperationEndTime(endTime).setCreateTrainLines(false)
//                    .setTravelDistanceDistribution("InverseGamma").setTravelDistanceMeanOverL(1. / 8).build();
//            LOG.info("Creating network");
//            scenarioCreator.createNetwork(networkPath);
//            LOG.info("Finished creating network\nCreating population for network");
//            scenarioCreator.createPopulation(populationPath, networkPath);
//            LOG.info("Finished creating population\nCreating transit Schedule");
////            scenarioCreator.createTransitSchedule(networkPath, transitSchedulePath, transitVehiclesPath);
//            LOG.info("Finished creating transit schedule\nCreating drt fleet");
//            scenarioCreator.createDrtFleet(networkPath, drtFleetPath);
//            LOG.info("Finished creating drt fleet");
//            return;
//        } else if (mode.equals("unimodal")) {
//            MultiModeDrtConfigGroup.get(config).getModalElements().stream().findFirst().orElseThrow()
//                    .setMaxDetour(deltaMax);
//            config.transit().setUseTransit(false);
//            config.transit().setTransitScheduleFile(null);
//            config.transit().setVehiclesFile(null);
//
//            config.qsim().setEndTime(endTime);
//        }
//        DrtPlanModifierConfigGroup.get(config).setMode(mode);
//
//        config.controler().setOutputDirectory(outPath);
//        LOG.info("Running simulation");
//        run(config, false, false);
//        LOG.info("Finished simulation with " + varyParameter + " = " + deltaMaxString);
//    }

    private static void manuallyStartMultipleNeworks(String configPath) throws Exception {
//        String[] modes = new String[]{"create-input", "bimodal", "unimodal", "car"};
        String[] modes = new String[]{"create-input", "unimodal"};
        String carGridSpacingString = "100";
        int simTime = 12 * 3600;
//        for (int N_drt = 9; N_drt < 10; N_drt += 5) {
//            for (int railInterval = 8; railInterval < 9; railInterval += 1) {
//                for (double freq = 0.01; freq < 0.02; freq += 0.01) {
//                    for (String mode : modes) {
//                        Config config = ConfigUtils
//                                .loadConfig(configPath, new MultiModeDrtConfigGroup(), new DvrpConfigGroup(),
//                                        new DrtPlanModifierConfigGroup(), new OTFVisConfigGroup());
//                        runMultipleNetworks(config, mode, "2000", "0.5", "100", "20", "20", "1000",
//                                "1.5", "234", "36000", "true", "1", "multi");
//                    }
//                }
//            }
//        }

        for (double dcut = 200; dcut < 5000; dcut += 200) {
            for (int railInt = 2; railInt < 50; railInt += 2) {
                for (String mode : new String[]{"create-input"}) {
                    Config config = ConfigUtils
                            .loadConfig(configPath, new MultiModeDrtConfigGroup(), new DvrpConfigGroup(),
                                    new DrtPlanModifierConfigGroup(), new OTFVisConfigGroup());
//                runMultipleNetworks(config, mode, "2000", "0.5", "100", "20", "20", String.valueOf(reqs),
//                        "234", "36000", "true", "false", "1", "multi");
                    runMultipleNetworks(config, mode, "1000", String.valueOf(dcut), "100", String.valueOf(railInt),
                            "1000",
                            "20000000", "42", "36000", "true", "true", "1", "0", "InverseGamma", "CIConstDrtDemand");
                }
            }
        }
    }

    private static void runRealWorldScenario(Config config, String mode, String meanDistString, String dCutString,
                                             String ptSpacingString, String nDrtString, String nReqsString,
                                             String seedString, String endTimeString, String trainDepartureInterval,
                                             String outFolder) throws Exception {
        String basicOutPath = config.controler().getOutputDirectory();
        if (!outFolder.equals("")) {
            basicOutPath = basicOutPath.concat("/" + outFolder);
        }
        double ptSpacing = Double.parseDouble(ptSpacingString);
        int nReqs = Integer.parseInt(nReqsString);
        double meanDist = Double.parseDouble(meanDistString);
        double trainFreq = Double.parseDouble(trainDepartureInterval);
        double dCut = Double.parseDouble(dCutString);
        int nDrt = Integer.parseInt(nDrtString);
        String nReqsOutPath = Paths.get(basicOutPath, nReqsString.concat("reqs")).toString();
        String meanDistOutPath = Paths.get(nReqsOutPath, meanDistString.concat("dist")).toString();
        String nDrtOutPath = Paths.get(meanDistOutPath, nDrtString.concat("drt")).toString();
        String dCutOutPath = Paths.get(nDrtOutPath, dCutString.concat("dcut")).toString();
        String lOutPaht = Paths.get(dCutOutPath, ptSpacingString.concat("l")).toString();

        String inputPath = Paths.get(lOutPaht, "input").toString();
        String networkPathIn = "network_clean.xml";
        String networkPathOut = Paths.get(inputPath, "network_input.xml.gz").toString();
        String populationPath = Paths.get(inputPath, "population_input.xml.gz").toString();
        String transitSchedulePath = Paths.get(inputPath, "transitSchedule_input.xml.gz").toString();
        String transitVehiclesPath = Paths.get(inputPath, "transitVehicles_input.xml.gz").toString();
        String drtFleetPath = Paths.get(inputPath, "drtvehicles_input.xml.gz").toString();
        String drtFleetPath2 = Paths.get(inputPath, "acc_egr_drtvehicles_input.xml.gz").toString();

        config.network().setInputFile(networkPathOut);
        config.plans().setInputFile(populationPath);
        config.transit().setTransitScheduleFile(transitSchedulePath);
        config.transit().setVehiclesFile(transitVehiclesPath);

        List<DrtConfigGroup> drtConfigGroups = new ArrayList<>(MultiModeDrtConfigGroup.get(config).getModalElements());
        drtConfigGroups.get(0).setVehiclesFile(drtFleetPath);
        if (drtConfigGroups.size() == 2) {
            drtConfigGroups.get(1).setVehiclesFile(drtFleetPath2);
        }

        String outPath = null;
        if (mode.equals("create-input")) {

            OutputDirectoryLogging.initLoggingWithOutputDirectory(inputPath);

            double endTime = Double.parseDouble(endTimeString);

            ScenarioCreatorOsm scenarioCreatorOsm = new ScenarioCreatorBuilderOsm()
                    .setNRequests(nReqs).setTravelDistanceDistribution("InverseGamma")
                    .setSeed(Long.parseLong(seedString)).setMeanTravelDist(meanDist)
                    .setRequestEndTime((int) (endTime - 3600)).setTransitEndTime(endTime)
                    .setDrtOperationEndTime(endTime).setPtSpacing(ptSpacing)
                    .setCutoffDistance(dCut)
                    .setdrtFleetSize(nDrt).setDepartureIntervalTime(trainFreq).build();
            double mu = 1. / scenarioCreatorOsm.getDepartureIntervalTime();
            double nu = 1. / scenarioCreatorOsm.getRequestEndTime();
            LOG.info("Q: " + mu / (nu * scenarioCreatorOsm.getnRequests() * scenarioCreatorOsm.getTravelDistanceMean() *
                    scenarioCreatorOsm.getTravelDistanceMean()));
            LOG.info("Creating network");
            scenarioCreatorOsm
                    .addTramsToNetwork(networkPathIn, networkPathOut, transitSchedulePath, transitVehiclesPath);
            LOG.info("Finished creating network\nCreating population for network");
            scenarioCreatorOsm.generatePopulation(populationPath);
            LOG.info("Finished creating population\nCreating transit Schedule");
            if (drtConfigGroups.size() == 1) {
                scenarioCreatorOsm.generateDrtVehicles(drtFleetPath);
            } else if (drtConfigGroups.size() == 2) {
                scenarioCreatorOsm.generateDrtVehicles(drtFleetPath, drtFleetPath2, dCut);
            }
            LOG.info("Finished creating drt fleet");
            return;
        } else if (mode.equals("bimodal")) {
            ConfigUtils.addOrGetModule(config, DrtPlanModifierConfigGroup.class)
                    .setdCut(dCut);
//            MultiModeDrtConfigGroup.get(config).getModalElements().stream().findFirst().orElseThrow()
//                    .setMaxDetour(deltaMax);
            outPath = Paths.get(lOutPaht, mode).toString();
        } else if (mode.equals("unimodal")) {
//            MultiModeDrtConfigGroup.get(config).getModalElements().stream().findFirst().orElseThrow()
//                    .setMaxDetour(deltaMax);
            outPath = Paths.get(nDrtOutPath, mode).toString();
            config.transit().setUseTransit(false);
        } else if (mode.equals("car")) {
            outPath = Paths.get(meanDistOutPath, mode).toString();
            config.transit().setUseTransit(false);
        }
        DrtPlanModifierConfigGroup.get(config).setMode(mode);

        config.controler().setOutputDirectory(outPath);
        LOG.info("Running simulation");
        run(config, false, true);
        LOG.info("Finished simulation");
    }

    private static void runMultipleNetworks(Config config, String mode, String travelDistMeanString,
                                            String dCutString,
                                            String carGridSpacingString, String railIntervalString, String N_drt,
                                            String nReqsString, String seedString,
                                            String endTimeString, String diagConnections, String constDrtDemandString,
                                            String meanAndSpeedScaleFactorString, String fracWithCommonOrigDestString,
                                            String travelDistanceDistributionString, String outFolder) throws
            Exception {
        // 20000000 requests for varying dcut from 200 to 10000; resulting requests: 11922 to 130679
        String basicOutPath = config.controler().getOutputDirectory();
        if (!outFolder.equals("")) {
            basicOutPath = basicOutPath.concat("/" + outFolder);
        }
        int railInterval = Integer.parseInt(railIntervalString);
        int nReqs = Integer.parseInt(nReqsString);
        double carGridSpacing = Double.parseDouble(carGridSpacingString);
//        double deltaMax = Double.parseDouble(deltaMaxString);
        double dCut = Double.parseDouble(dCutString);
        double travelDistMean = Double.parseDouble(travelDistMeanString);
        double meanAndSpeedScaleFactor = Double.parseDouble(meanAndSpeedScaleFactorString);
        boolean constDrtDemand = Boolean.parseBoolean(constDrtDemandString);
        double fracWithCommonOrigDest = Double.parseDouble(fracWithCommonOrigDestString);
        //if system size is not modified
//        String iterationSpecificPath = Paths.get(basicOutPath,
//                nReqsString.concat("reqs"),
//                N_drt.concat("drt"),
//                dCutString.concat("dcut"),
//                travelDistMeanString.concat("dist"),
//                (int) (railInterval * carGridSpacing) + "l")
//                .toString();
        String nReqsOutPath = Paths.get(basicOutPath, nReqsString.concat("reqs")).toString();
        String meanDistOutPath = Paths.get(nReqsOutPath, travelDistMeanString.concat("dist")).toString();
        String fracComOrigDestPath = Paths
                .get(meanDistOutPath, fracWithCommonOrigDestString.concat("frac_comm_orig_dest")).toString();
        String nDrtOutPath = Paths.get(fracComOrigDestPath, N_drt.concat("drt")).toString();
        String dCutOutPath = Paths.get(nDrtOutPath, dCutString.concat("dcut")).toString();
        String lOutPath = Paths.get(dCutOutPath, (int) (railInterval * carGridSpacing) + "l").toString();

        String inputPath = Paths.get(lOutPath, "input").toString();
        String networkPath = Paths.get(inputPath, "network_input.xml.gz").toString();
        String populationPath = Paths.get(inputPath, "population_input.xml.gz").toString();
        String transitSchedulePath = Paths.get(inputPath, "transitSchedule_input.xml.gz").toString();
        String transitVehiclesPath = Paths.get(inputPath, "transitVehicles_input.xml.gz").toString();
        String drtFleetPath = Paths.get(inputPath, "drtvehicles_input.xml.gz").toString();

        config.network().setInputFile(networkPath);
        config.plans().setInputFile(populationPath);
        config.transit().setTransitScheduleFile(transitSchedulePath);
        config.transit().setVehiclesFile(transitVehiclesPath);
        MultiModeDrtConfigGroup.get(config).getModalElements().stream().findFirst().orElseThrow()
                .setVehiclesFile(drtFleetPath);

        String outPath = null;
        if (mode.equals("create-input")) {

            OutputDirectoryLogging.initLoggingWithOutputDirectory(inputPath);

            double endTime = Double.parseDouble(endTimeString);

            ScenarioCreator scenarioCreator = new ScenarioCreatorBuilder().setCarGridSpacing(carGridSpacing)
                    .setRailInterval(railInterval).setNRequests(nReqs)
                    .setTravelDistanceDistribution(travelDistanceDistributionString)
                    .setSeed(Long.parseLong(seedString)).setTravelDistanceMean(travelDistMean)
                    .setRequestEndTime((int) (endTime - 3600)).setTransitEndTime(endTime)
                    .setDrtOperationEndTime(endTime)
                    .setFracWithCommonOrigDest(fracWithCommonOrigDest)
                    .setDiagonalConnetions(Boolean.parseBoolean(diagConnections))
                    .setMeanAndSpeedScaleFactor(meanAndSpeedScaleFactor)
                    .setConstDrtDemand(constDrtDemand)
                    .setCutoffDistance(dCut)
                    .setSmallLinksCloseToNodes(false).setdrtFleetSize(Integer.parseInt(N_drt)).build();
//            double mu = 1. / scenarioCreator.getDepartureIntervalTime();
//            double nu = 1. / scenarioCreator.getRequestEndTime();
//            double E = scenarioCreator.getnRequests() /
//                    (scenarioCreator.getSystemSize() * scenarioCreator.getSystemSize());
//            double avDist = scenarioCreator.getSystemSize() * scenarioCreator.getTravelDistanceMeanOverL();
//            LOG.info("Q: " + mu / (nu * scenarioCreator.getnRequests() * scenarioCreator.getTravelDistanceMean() *
//                    scenarioCreator.getTravelDistanceMean()));
            LOG.info("Creating network / Transit");
            scenarioCreator.createNetwork(networkPath, transitSchedulePath, transitVehiclesPath);
            LOG.info("Finished creating network\nCreating population for network");
            scenarioCreator.createPopulation(populationPath, networkPath);
            LOG.info("Finished creating population");
            LOG.info("Finished creating population\nCreating drt fleet");
            scenarioCreator.createDrtFleet(networkPath, drtFleetPath);
            LOG.info("Finished creating drt fleet");
            OutputDirectoryLogging.closeOutputDirLogging();
            return;
        } else if (mode.equals("bimodal")) {
//            MultiModeDrtConfigGroup.get(config).getModalElements().stream().findFirst().orElseThrow()
//                    .setMaxDetour(deltaMax);
            ConfigUtils.addOrGetModule(config, DrtPlanModifierConfigGroup.class)
                    .setdCut(dCut);
            outPath = Paths.get(lOutPath, mode).toString();
        } else if (mode.equals("unimodal")) {
//            MultiModeDrtConfigGroup.get(config).getModalElements().stream().findFirst().orElseThrow()
//                    .setMaxDetour(deltaMax);
            outPath = Paths.get(nDrtOutPath, mode).toString();
            config.transit().setUseTransit(false);
        } else if (mode.equals("car")) {
            outPath = Paths.get(meanDistOutPath, mode).toString();
        }
        DrtPlanModifierConfigGroup.get(config).setMode(mode);
//        if (mode.equals("car")) {
//            config.removeModule(MultiModeDrtConfigGroup.GROUP_NAME);
//        }

        config.controler().setOutputDirectory(outPath);
        LOG.info("Running simulation");
        run(config, false, true);
        LOG.info("Finished simulation!");
//        LOG.info("Deleting input directory");
    }


    public static void run(Config config, boolean otfvis, boolean isGridAndPt) throws Exception {
//        String vehiclesFile = getVehiclesFile(config);
//        LOG.info(
//                "STARTING with\npopulation file: " + config.plans().getInputFile() +
//                        " and\nvehicles file: " + vehiclesFile + "\n---------------------------");

        // For dvrp/drt
        Controler controler = DrtControlerCreator.createControler(config, otfvis);
        Collection<DrtConfigGroup> modalElements = MultiModeDrtConfigGroup.get(config).getModalElements();
        System.out.println(modalElements.size());

        // For only pt
//		Scenario scenario = ScenarioUtils.loadScenario(config);
//		Controler controler = new Controler(scenario);

        // Set up SBB Transit/Raptor
//        controler.addOverridingModule(new SwissRailRaptorModule());

        //Custom Modules
//        controler.addOverridingModule(new BimodalAssignmentModule());
//        controler.addOverridingModule(new GridPrePlanner());
        controler.addOverridingModule(new MyAnalysisModule());
        controler.addOverridingQSimModule(new CustomTransitStopHandlerModule());

//        controler.addOverridingModule(new SBBTransitModule());
////        controler.addOverridingModule(new SwissRailRaptorModule());
//        controler.configureQSimComponents(components -> {SBBTransitEngineQSimModule.configure(components);});

        controler.addOverridingModule(new DrtTrajectoryAnalyzer());
        DrtPlanModifierConfigGroup drtGroup = ConfigUtils.addOrGetModule(config, DrtPlanModifierConfigGroup.class);
        controler.addOverridingModule(new DrtPlanModifier(drtGroup));
        controler.addOverridingModule(new CustomRoutingModule());
        controler.addOverridingModule(new ParkingVehicleTracker());

//        double[] netDims = getNetworkDimensionsMinMax(controler.getScenario().getNetwork(), isGridAndPt);
//        assert (doubleCloseToZero(netDims[0]) && doubleCloseToZero(netDims[1] - 10000)) :
//                "You have to change L (" + netDims[0] + "," + netDims[1] + ") in: " +
//                        "org/matsim/core/router/util/LandmarkerPieSlices.java; " +
//                        "org/matsim/core/utils/geometry/CoordUtils.java; " +
//                        "org/matsim/core/utils/collections/QuadTree.java" +
//                        "org/matsim/contrib/util/distance/DistanceUtils.java";

        controler.run();
    }


    private static void runMultipleConvCrit(Config config, String zetas, String popDir, String drtDir,
                                            String appendOutDir, boolean otfvis) throws Exception {

        String[] zetaList = zetas.split(",");
        System.out.println(zetaList);

        String populationPath = Paths.get(popDir, "population_zeta0_0.xml.gz").toString();
        String drtPath = Paths.get(drtDir, "drtvehicles.xml.gz").toString();

        for (String zeta : zetaList) {

            config.plans().setInputFile(populationPath);
            Collection<DrtConfigGroup> modalElements = MultiModeDrtConfigGroup.get(config).getModalElements();
            assert modalElements.size() == 1 : "Only one drt modal element expected in config file";
            modalElements.stream().findFirst().get().setVehiclesFile(drtPath);

            ConfigUtils.addOrGetModule(config, DrtPlanModifierConfigGroup.class).setdCut(Double.parseDouble(zeta));
            config.controler()
                    .setOutputDirectory(Paths.get("./output".concat(appendOutDir), "zeta".concat(zeta)).toString());
            System.out.println(populationPath);
            System.out.println(drtPath);
            System.out.println("Output: " + config.controler().getOutputDirectory());

            run(config, otfvis, true);
        }

    }


    private static String getVehiclesFile(Config config) {
        Collection<DrtConfigGroup> modalElements = MultiModeDrtConfigGroup.get(config).getModalElements();
        assert modalElements.size() == 1 : "Only one drt modal element expected in config file";
        return modalElements.stream().findFirst().get().getVehiclesFile();
    }

    private static String[] getFiles(Pattern pattern, String Dir) {
        File dir = new File(Dir);
        FilenameFilter filter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return pattern.matcher(name).matches();
            }
        };
        String[] files = dir.list(filter);
        assert files != null;
        Arrays.sort(files);

        return files;
    }
}