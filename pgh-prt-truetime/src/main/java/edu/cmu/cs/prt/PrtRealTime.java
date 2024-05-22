package edu.cmu.cs.prt;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A static utility class for fetching information from Pittsburgh Regional Transit's real-time data pool. The methods
 * in this API may block indefinitely while obtaining information from the PRT backend.
 * <p>
 * <b>Example usage</b>
 * <p>Getting the predicted arrivals for a given stop and route using
 * {@link PrtRealTime#predictedArrivalsFor(Stop, Route)}.</p>
 * <pre>
 * Route route = ...
 * Stop stop = ...
 * {@literal List<}{@link PredictedArrival}{@literal >} arrivals = PrtRealTime.predictedArrivalsFor(stop, route);
 * </pre>
 * Getting the vehicles running on a given stop using {@link PrtRealTime#vehiclesFor(Route)}.
 * <pre>
 * Route route = ...
 * {@literal List<}{@link Vehicle}{@literal >} vehicles = PrtRealTime.vehiclesFor(route);
 * </pre>
 */
public final class PrtRealTime {
    private static final TrueTimeWebApiClient client = createClient("pgh-prt-truetime/key.secret");

    private PrtRealTime() { }

    /**
     * Method to create client for Web API.
     * @return web API client
     */
    private static TrueTimeWebApiClient createClient(String keyFilePath) {
        Path path = Path.of(keyFilePath);
        List<String> keyFileLinesList;
        try {
            keyFileLinesList = Files.readAllLines(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (keyFileLinesList.size() > 1) {
            throw new RuntimeException("Key file should have only one line");
        }
        String key = keyFileLinesList.get(0);
        return new TrueTimeWebApiClient("truetime.portauthority.org", key);
    }

    /**
     * Get the TrueTimeWebApiClient.
     * @return the TrueTimeWebApiClient
     */
    static TrueTimeWebApiClient getClient() {
        return client;
    }

    /**
     * Returns all vehicles currently operating on the given route.
     *
     * @param route route to get vehicles for
     * @return vehicles currently operating on the given route
     *
     * @throws IOException if the request to the PRT backend fails
     * @throws InterruptedException if the request to the PRT backend is interrupted
     */
    public static List<Vehicle> vehiclesFor(Route route) throws IOException, InterruptedException {
        return vehiclesFor(route.id());
    }

    /**
     * Returns all currently running vehicles for the given route.
     * <p>
     * If there are no currently running vehicles, an empty list will be returned.
     *
     * @param routeId transit route ID to get vehicles for
     * @return Running vehicles for the input route
     */
    static List<Vehicle> vehiclesFor(String routeId) throws IOException, InterruptedException {
        List<Vehicle> matchingVehicles;
        try {
            matchingVehicles = client.getVehiclesWithRouteDesignators(Set.of(routeId));
        } catch (NoDataFoundException e) {
            return Collections.emptyList();
        }
        return matchingVehicles;
    }

    /**
     * Returns the predicted arrivals for a given stop.
     * @param stop the stop to find predicted arrivals for
     * @param vehicleType the vehicle type to find predicted arrivals for
     * @return the predicted arrivals for the given stop
     *
     * @throws IOException if the request to the PRT backend fails
     * @throws InterruptedException if the request to the PRT backend is interrupted
     */
    public static List<PredictedArrival> predictedArrivalsFor(Stop stop, VehicleType vehicleType) throws IOException, InterruptedException {
        return predictedArrivalsFor(Set.of(stop), Collections.emptySet(), vehicleType);
    }

    /**
     * Returns the predicted arrivals for the given vehicle.
     * @param vehicle the vehicle to find predicted arrivals for
     * @return the predicted arrivals for the given vehicle
     *
     * @throws IOException if the request to the PRT backend fails
     * @throws InterruptedException if the request to the PRT backend is interrupted
     */
    public static List<PredictedArrival> predictedArrivalsFor(Vehicle vehicle) throws IOException, InterruptedException {
        return predictedArrivalsFor(Set.of(vehicle));
    }

    /**
     * Returns the predicted arrivals for the given vehicles.
     * @param vehicles the vehicles to find predicted arrivals for
     * @return the predicted arrivals for the given vehicles
     *
     * @throws IOException if the request to the PRT backend fails
     * @throws InterruptedException if the request to the PRT backend is interrupted
     */
    public static List<PredictedArrival> predictedArrivalsFor(Collection<Vehicle> vehicles)
            throws IOException, InterruptedException {
        Set<Vehicle> vehicleSet = Set.copyOf(vehicles);
        if (vehicles.isEmpty()) {
            return Collections.emptyList();
        }
        Set<String> vehicleIds = vehicleSet.stream().map(Vehicle::id).collect(Collectors.toSet());
        return client.getPredictionsForVehicles(vehicleIds, vehicles.iterator().next().type());
    }

    /**
     * Returns the predicted arrivals for the given stop and route.
     * @param stop the stop to find predicted arrivals for
     * @param route the route to find predicted arrivals for
     * @return the predicted arrivals for the given stop and route
     *
     * @throws IOException if the request to the PRT backend fails
     * @throws InterruptedException if the request to the PRT backend is interrupted
     */
    public static List<PredictedArrival> predictedArrivalsFor(Stop stop, Route route)
            throws IOException, InterruptedException {
        return predictedArrivalsFor(Set.of(stop), Set.of(route), route.vehicleType());
    }

    /**
     * Returns the predicted arrivals for the stops and routes specified.
     * @param stops the stops to get predicted arrivals for
     * @param routes the routes to get predicted arrivals for
     * @param vehicleType  the vehicle type to find predicted arrivals for
     * @return the predicted arrivals for the stops and routes specified
     *
     * @throws IOException if the request to the PRT backend fails
     * @throws InterruptedException if the request to the PRT backend is interrupted
     */
    public static List<PredictedArrival> predictedArrivalsFor(Collection<Stop> stops,
                                                              Collection<Route> routes,
                                                              VehicleType vehicleType)
            throws IOException, InterruptedException {
        Set<Stop> stopSet = Set.copyOf(stops);
        Set<Route> routeSet = Set.copyOf(routes);
        Set<String> stopIds = stopSet.stream().map(Stop::id).collect(Collectors.toSet());
        Set<String> routeIds = routeSet.stream().map(Route::id).collect(Collectors.toSet());
        return client.getPredictionsForRoutesAtStops(stopIds, routeIds, vehicleType);
    }
}

