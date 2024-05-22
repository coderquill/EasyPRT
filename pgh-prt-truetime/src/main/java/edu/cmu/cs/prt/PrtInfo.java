package edu.cmu.cs.prt;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * A static utility class for fetching information about stops and routes in the Pittsburgh Regional Transit system.
 * <p>
 * <b>Example usage</b>
 * <p>Getting the routes that go through a given stop using
 * {@link PrtInfo#closestStop(Location)} and {@link PrtInfo#routesThru(Stop)}.</p>
 * <pre>
 * {@link Location} location = ...
 * {@link Stop} stop = PrtInfo.closestStop(location);
 * {@literal List<}{@link Route}{@literal >} routes = PrtInfo.routesThru(stop);
 * </pre>
 */
public final class PrtInfo {
    private PrtInfo() { }

    /**
     * Returns all the stops for the specified route in the given direction.
     * A route's stops are defined as the stops on the route that a
     * vehicle picks up and/or drops passengers at.
     * <p>
     * <b>Note:</b> this method queries the PRT backend for the requested information and may block indefinitely.
     * @param route route to get stops for
     * @param direction the direction of the route to get stops for
     * @return stops for the specified route
     *
     * @throws IOException if the request to the PRT backend fails
     * @throws InterruptedException if the request to the PRT backend is interrupted
     */
    public static List<Stop> stopsFor(Route route, RouteDirection direction)
            throws IOException, InterruptedException {
        return stopsFor(route.id(), direction, route.vehicleType());
    }

    /**
     * Returns all the stops for the specified route. A route's stops are defined as the stops on the route that a
     * vehicle picks up and/or drops passengers at.
     * @param routeId identifier of the route to get stops for
     * @param direction the direction of the route to get stops for
     * @param vehicleType the type of the vehicle to get stops for
     * @return stops for the specified route
     *
     * @throws IOException if the request to the PRT backend fails
     * @throws InterruptedException if the request to the PRT backend is interrupted
     */
    static List<Stop> stopsFor(String routeId,
                               RouteDirection direction,
                               VehicleType vehicleType) throws IOException, InterruptedException {
        TrueTimeWebApiClient client = PrtRealTime.getClient();
        try {
            return client.getStopsForRoute(routeId, direction, vehicleType);
        } catch (NoDataFoundException e) {
            return Collections.emptyList();
        }
    }

    /**
     * Returns the closest stop to the given location.
     * @param location the location to find the closest stop to
     * @return the closest stop to the given location
     */
    public static Stop closestStop(Location location) {
        Stop closestStop = null;
        for (Stop stop : StopsCache.allStops()) {
            if (closestStop == null ||
                    location.metersFrom(stop.location()) < location.metersFrom(closestStop.location())) {
                closestStop = stop;
            }
        }
        return closestStop;
    }

    /**
     * Returns all the stops in the Pittsburgh Regional Transit system.
     * @return all the stops in the Pittsburgh Regional Transit system
     */
    public static List<Stop> allStops() {
        return StopsCache.allStops();
    }

    /**
     * Returns all the routes in the Pittsburgh Regional Transit system.
     * @return all the routes in the Pittsburgh Regional Transit system
     */
    public static List<Route> allRoutes() {
        return RoutesCache.allRoutes();
    }

    /**
     * Returns all the stops that are within the specified distance of the given location.
     * @param location the location to find stops near
     * @param distanceFeet the distance in feet around this location to find stops in
     * @return the stops that are within the specified distance of the given location
     */
    public static List<Stop> stopsNear(Location location, double distanceFeet) {
        List<Stop> stops = new ArrayList<>();
        for (Stop stop : StopsCache.allStops()) {
            if (location.feetFrom(stop.location()) < distanceFeet) {
                stops.add(stop);
            }
        }
        return stops;
    }
    
    /**
     * Returns the routes that go through the given stop.
     * @param stop the stop to use for retrieving routes
     * @return the routes that go through the given stop
     */
    public static List<Route> routesThru(Stop stop) {
        return PrtSchedule.routesThru(stop);
    }

    /**
     * Returns the route corresponding to the specified route identifier.
     * @param routeId the identifier of the route to get
     * @return the route corresponding to the specified route identifier
     */
    public static Optional<Route> routeOf(String routeId) {
        return RoutesCache.get(routeId);
    }
}