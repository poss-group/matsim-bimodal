package de.mpi.ds.utils;

import org.matsim.api.core.v01.*;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.api.core.v01.population.*;
import org.matsim.contrib.util.distance.DistanceUtils;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.scenario.ScenarioUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static de.mpi.ds.utils.CreateScenarioElements.compressGzipFile;
import static de.mpi.ds.utils.CreateScenarioElements.deleteFile;
import static de.mpi.ds.utils.InverseTransformSampler.normalDist;

public class PopulationUtil implements UtilComponent {
    private static Random rand = new Random();

    private PopulationUtil() {
    }

    public static void main(String... args) {
//        Boolean[] bools = {true, false, true, true, true, false, true, true, true, false};
//        System.out.println(Arrays.stream(bools).map(b -> b ? 1 : 0).mapToInt(Integer::intValue).sum());
//        for (Boolean bool : bools) {
//            System.out.println(bool);
//        }
        createPopulation("./output/population.xml", "./output/network.xml", N_REQUESTS, 0, 31357);
        compressGzipFile("./output/population.xml", "./output/population.xml.gz");
        deleteFile("./output/population.xml");
    }

    public static void createPopulation(String outputPopulationPath, String networkPath, int nRequests, double gamma,
                                        long seed) {
        rand.setSeed(seed);
        Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
        Population population = scenario.getPopulation();
//        Map<String, Coord> zoneGeometries = new HashMap<>();
        Network net = NetworkUtils.readNetwork(networkPath);
        double[] netDimsMinMax = getNetworkDimensionsMinMax(net);
        System.out.println("Network dimensions (min, max): " + Arrays.toString(netDimsMinMax));
//        InverseTransformSampler sampler = new InverseTransformSampler(a -> 1 / (netDimsMinMax[1] - netDimsMinMax[0]),
        InverseTransformSampler sampler = new InverseTransformSampler(x -> normalDist(x, 2000, 500),
                true,
                netDimsMinMax[0],
                netDimsMinMax[1],
                10000);
//        for (Node node : net.getNodes().values()) {
//            zoneGeometries.put(node.getId().toString(), node.getCoord());
//        }
        generatePopulation(population, net, nRequests, gamma, seed, sampler, netDimsMinMax[1]);

        PopulationWriter populationWriter = new PopulationWriter(scenario.getPopulation(), scenario.getNetwork());
        populationWriter.write(outputPopulationPath);
    }

    private static double[] getNetworkDimensionsMinMax(Network net) {
        List<Double> xCoords = net.getNodes().values().stream().map(n -> n.getCoord().getX())
                .collect(Collectors.toList());
        List<Double> yCoords = net.getNodes().values().stream().map(n -> n.getCoord().getY())
                .collect(Collectors.toList());
        double xmin = Collections.min(xCoords);
        double xmax = Collections.max(xCoords);
        double ymin = Collections.min(yCoords);
        double ymax = Collections.max(yCoords);

        return new double[]{Math.min(xmin, ymin), Math.max(xmax, ymax)};
    }


    private static void generatePopulation(Population population, Network net, int nRequests, double gamma, long seed,
                                           InverseTransformSampler sampler, double L) {
        rand.setSeed(seed);
//        Id<Node> orig_id;
//        Id<Node> dest_id;
//        List<Id<Node>> nodeIdList = net.getNodes().values().stream()
//                .filter(n -> n.getCoord().getX() % delta_xy == 0)
//                .filter(n -> n.getCoord().getY() % delta_xy == 0)
//                .map(Identifiable::getId)
//                .collect(Collectors.toList());
        Coord orig_coord;
        Coord dest_coord;
        List<Node> nodeList = net.getNodes().values().stream()
                .filter(n -> (n.getCoord().getX() % delta_x == 0) && (n.getCoord().getY() % delta_y == 0))
                .collect(Collectors.toList());
        for (int j = 0; j < nRequests; j++) {
            do {
//                orig_coord = getRandomNodeOfCollection(net.getNodes().values()).getCoord();
                orig_coord = nodeList.get(rand.nextInt(nodeList.size())).getCoord();
                if (sampler != null) {
                    double dist = 0;
                    try {
                        dist = sampler.getSample();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    double angle = rand.nextDouble() * 2 * Math.PI;

                    double newX = ((orig_coord.getX() + dist * Math.cos(angle)) % L + L) % L;
                    double newY = ((orig_coord.getY() + dist * Math.sin(angle)) % L + L) % L;
                    Coord pre_target = new Coord(newX, newY);
                    dest_coord = getClosestNode(pre_target, nodeList, L).getCoord();
                } else {
//                    dest_coord = getRandomNodeOfCollection(net.getNodes().values()).getCoord();
                    dest_coord = nodeList.get(rand.nextInt(nodeList.size())).getCoord();
                }
            } while (orig_coord.equals(dest_coord));
            generateTrip(orig_coord, dest_coord, j, population, gamma);
        }
    }

    private static double getDirectionPeriodicBC(double v, double L) {
        return v % L;
    }

    private static Node getRandomNodeOfCollection(Collection<? extends Node> collection) {
        return collection.stream().skip(rand.nextInt(collection.size())).findFirst().orElseThrow();
    }

    private static void generateTrip(Coord source, Coord sink, int passenger_id, Population population, double gamma) {
        Person person = population.getFactory()
                .createPerson(Id.createPersonId(String.valueOf(passenger_id)));
        // person.getCustomAttributes().put("hasLicense", "false");
        person.getAttributes().putAttribute("hasLicense", "false");
        Plan plan = population.getFactory().createPlan();
        Coord sourceLocation = shoot(source);
        Coord sinkLocation = shoot(sink);
//			Coord sourceTransferLocation = searchTransferLoc(sourceLocation, sinkLocation);
//			Coord sinkTransferLocation = searchTransferLoc(sinkLocation, sourceLocation);
        plan.addActivity(createFirst(sourceLocation, population));
//			if (!sourceLocation.equals(sourceTransferLocation)) {
//				plan.addLeg(createDriveLeg(population, TransportMode.drt));
//				plan.addActivity(createDrtActivity(sourceTransferLocation, population));
//			}
//        plan.addLeg(createDriveLeg(population, TransportMode.drt));
//        if (DistanceUtils.calculateDistance(sourceLocation, sinkLocation)/(pt_interval*delta_xy) > gamma &&
//                DistanceUtils.calculateDistance(sourceLocation, sinkLocation)/(pt_interval*delta_xy) < 1 ) {
//            System.out.println("bla");
//        }
        if (DistanceUtils.calculateDistance(sourceLocation, sinkLocation) > gamma * pt_interval * delta_xy) {
            plan.addLeg(createDriveLeg(population, TransportMode.pt));
        } else {
            plan.addLeg(createDriveLeg(population, TransportMode.drt));
        }
//			if (!sinkLocation.equals(sinkTransferLocation)) {
//				plan.addActivity(createDrtActivity(sinkTransferLocation, population));
//				plan.addLeg(createDriveLeg(population, TransportMode.drt));
//			}
        plan.addActivity(createSecond(sinkLocation, population));
        person.addPlan(plan);
        population.addPerson(person);
    }

    private static Leg createDriveLeg(Population population, String mode) {
        Leg leg = population.getFactory().createLeg(mode);
        return leg;
    }

    private static Coord shoot(Coord source) {
        // Insert code here to blur the input coordinate.
        // For example, add a random number to the x and y coordinates.
        return source;
    }

    private static Activity createSecond(Coord workLocation, Population population) {
        Activity activity = population.getFactory().createActivityFromCoord("dummy", workLocation);
//        activity.setEndTime(24 * 60 * 60); // [s]
        return activity;
    }

    private static Activity createFirst(Coord homeLocation, Population population) {
        Activity activity = population.getFactory().createActivityFromCoord("dummy", homeLocation);
        activity.setEndTime(rand.nextInt(MAX_END_TIME)); // [s]
        return activity;
    }

    private static Coord searchTransferLoc(Coord startLoc, Coord targetLoc) {
        double source_x = startLoc.getX();
        double source_y = startLoc.getY();
        double sink_x = targetLoc.getX();
        double sink_y = targetLoc.getY();
        double new_x = source_x;
        double new_y = source_y;
        if (source_x / 1000 % 2 == 0 && source_y / 1000 % 2 == 0) {
            if (sink_x - source_x < sink_y - source_y && sink_x - source_x != 0) {
                new_x = source_x + Math.signum(sink_x - source_x) * 1000;
            } else {
                new_y = source_y + Math.signum(sink_y - source_y) * 1000;
            }
        }
        return new Coord(new_x, new_y);
    }

    private static Activity createDrtActivity(Coord location, Population population) {
        Activity activity = population.getFactory().createActivityFromCoord("dummy", location);
        activity.setMaximumDuration(0);
        return activity;
    }

    private static Id<Person> createId(String source, String sink, int i, String transportMode) {
        return Id.create(source + "_" + sink + "_" + i, Person.class);
    }


    private static Node getClosestNode(Coord coord, List<Node> nodes, double L) {
        return nodes.stream()
                .min(Comparator
                        .comparingDouble(node -> calculateDistancePeriodicBC(node.getCoord(), coord, L)))
                .orElseThrow();
    }

    private static double calculateDistancePeriodicBC(Coord from, Coord to, double L) {
        double deltaX = Math.abs(to.getX() - from.getX());
        double deltaXperiodic = deltaX < L / 2 ? deltaX : -deltaX + L;
        double deltaY = Math.abs(to.getY() - from.getY());
        double deltaYperiodic = deltaY < L / 2 ? deltaY : -deltaY + L;
        return Math.sqrt(deltaXperiodic * deltaXperiodic + deltaYperiodic * deltaYperiodic);
    }

    public static double taxiDistDistributionNotNormalized(double x, double mean, double k) {
        double z = x / mean;
        return Math.exp(-1. / z) * Math.pow(z, -k);
    }
}