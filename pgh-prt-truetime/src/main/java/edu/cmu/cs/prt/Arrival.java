package edu.cmu.cs.prt;

import java.time.LocalTime;

/**
 * Represents the event of a vehicle arriving at a stop.
 */
public interface Arrival {
    /**
     * Returns the time of the arrival.
     * @return the time of the arrival
     */
    public LocalTime time();

    /**
     * Returns the stop for this arrival.
     * @return the stop for this arrival
     */
    public Stop stop();

    /**
     * Returns the route that this arrival is for.
     * @return the route that this arrival is for
     */
    public Route route();

    /**
     * Returns the direction of the route for this arrival.
     * @return the direction of the route for this arrival
     */
    public RouteDirection routeDirection();
}
