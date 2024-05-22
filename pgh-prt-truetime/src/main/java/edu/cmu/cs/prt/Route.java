package edu.cmu.cs.prt;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Represents an immutable public transit route.
 * A vehicle operates on a route in a given direction, as specified by {@link RouteDirection}.
 * <p>
 * This class is thread-safe.
 */
public final class Route {
    /**
     * The identifier of the route.
     */
    private final String id;
    /**
     * The name of the route.
     */
    private final String routeName;
    /**
     * The type of vehicles that operate on this route.
     */
    private final VehicleType type;

    /**
     * Information about the patterns for this route. A pattern is a sequence of geo-positional points that combine
     * to form the path that a vehicle travels on. A route has multiple patterns, typically one in each direction.
     * There can be different stops in each direction. This field is populated only when
     * {@link #waypoints(RouteDirection)} is invoked.
     */
    private List<TrueTimePattern> trueTimePatterns = Collections.emptyList();

    /**
     * Create a Route object from a TrueTimeRoute object.
     * @param routeInternal the TrueTimeRoute object to use
     * @return the Route object created
     */
    static Route createFrom(TrueTimeRoute routeInternal) {
        String routeId = routeInternal.routeId();
        Optional<Route> optRoute = RoutesCache.get(routeId);
        if (optRoute.isPresent()) {
            // Cache contains key already, return cached route
            return optRoute.get();
        }
        Route newRoute = new Route(routeId, routeInternal.routeName(), VehicleType.convert(routeInternal.rtpidatafeed()));
        RoutesCache.put(newRoute);
        return newRoute;
    }

    static Route createFrom(String routeId, String routeName, VehicleType type) {
        Optional<Route> optRoute = RoutesCache.get(routeId);
        // Cache contains key already, return cached route
        return optRoute.orElseGet(() -> new Route(routeId, routeName, type));
    }

    private Route(String id, String routeName, VehicleType type) {
        this.id = id;
        this.routeName = routeName;
        this.type = type;
    }

    /**
     * Returns the alphanumeric designator of this route.
     * @return the alphanumeric designator of this route
     */
    public String id() {
        return id;
    }

    /**
     * Returns the name of this route in English.
     * @return the name of this route in English
     */
    public String routeName() {
        return routeName;
    }

    /**
     * Returns the type of vehicles that operate on this route.
     * @return the type of vehicles that operate on this route
     */
    public VehicleType vehicleType() {
        return type;
    }

    /**
     * Returns the waypoints that constitute this route in the given direction. Joining these waypoints creates the path
     * that vehicles operating on this route will follow in the given direction.
     * @param direction the direction to get the waypoints for
     * @return the waypoints that constitute this route in the given direction
     * @throws IOException if the call to the web API fails
     * @throws InterruptedException if the call to the web API is interrupted
     */
    public List<Location> waypoints(RouteDirection direction)
            throws IOException, InterruptedException {
        if (trueTimePatterns.isEmpty()) {
            TrueTimeWebApiClient client = PrtRealTime.getClient();
            try {
                trueTimePatterns = client.getPatternsForRoute(id(), vehicleType());
            } catch (NoDataFoundException e) {
                trueTimePatterns = new ArrayList<TrueTimePattern>();
            }
            
        }
        List<Location> waypoints = new ArrayList<Location>();
        for (TrueTimePattern pattern : trueTimePatterns) {
            if (pattern.routeDirection().equals(direction)) {
                for (TrueTimeWaypoint waypoint : pattern.waypoints()) {
                    waypoints.add(new Location(waypoint.lat(), waypoint.lon()));
                }
                break;
            }
        }
        return waypoints;
    }

    /**
     * Returns the stops for this route in the specified direction. The stops returned are ordered by arrival, starting
     * with the first stop.
     * 
     * @param direction the direction to get stops for
     * @return the stops for this route in the specified direction
     * @throws IOException if the HTTP request to the web API fails
     * @throws InterruptedException if the HTTP request to the web API is interrupted
     */
    public List<Stop> stopsInDirection(RouteDirection direction)
            throws IOException, InterruptedException {
        if (trueTimePatterns.isEmpty()) {
            TrueTimeWebApiClient client = PrtRealTime.getClient();
            try {
                trueTimePatterns = client.getPatternsForRoute(id(), vehicleType());
            } catch (NoDataFoundException e) {
                trueTimePatterns = new ArrayList<TrueTimePattern>();
            }
        }
        List<Stop> stops = new ArrayList<>();
        for (TrueTimePattern pattern : trueTimePatterns) {
            if (pattern.routeDirection().equals(direction)) {
                for (TrueTimeWaypoint waypoint : pattern.waypoints()) {
                    // If this waypoint is a stop.
                    if (waypoint.typ().equals("S") && StopsCache.containsKey(waypoint.stpid())) {
                        Stop stop = StopsCache.get(waypoint.stpid()).get();
                        stops.add(stop);
                    }
                }
            }
        }
        return stops;
    }

    /**
     * Compares the specified object with this route for equality. Two routes are equal to each other if and only if
     * they have the same route ID.
     *
     * @param o object to be compared for equality with this route
     * @return true if the specified object is equal to this route
     */
    @Override
    public boolean equals(Object o) {
        try {
            TrueTimeRoute otherRoute = (TrueTimeRoute) o;
            return id.equals(otherRoute.routeId());
        } catch (ClassCastException e) {
            return false;
        }
    }

    /**
     * Returns the hash code of this route.
     * @return the hash code of this route
     */
    @Override
    public int hashCode() {
        return id.hashCode();
    }

    /**
     * Returns a string representation of this route.
     * @return a string representation of this route
     */
    @Override
    public String toString() {
        return id + " (" + routeName + ", " + type.getFeedName() + ")";
    }
}