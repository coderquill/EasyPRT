package edu.cmu.cs.prt;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Represents an immutable instance of a vehicle arrival event at a stop.
 * Each {@code HistoryEntry} instance encapsulates data related to a single vehicle arrival,
 * including trip identifier, route information, stop details, direction of the route,
 * scheduled arrival time, and the actual arrival time.
 *
 * <p>This class is designed to provide a comprehensive view of each arrival event,
 * making it suitable for analysis and reporting purposes within a transportation system.
 * Being an immutable class, it ensures that the data integrity is maintained throughout
 * the lifecycle of each instance.</p>
 *
 * @see PrtHistoryTable
 */
public final class HistoryEntry {
    private final String tripId;
    private final Route route;
    private final Stop stop;
    private final RouteDirection routeDirection;
    private final LocalDateTime scheduledTime;
    private final LocalDateTime actualArrivalTime;

    /**
     * @param tripId           Identifier for the bus trip, unique for each trip.
     * @param route            The {@link Route} object representing the bus route.
     * @param stop             The {@link Stop} object representing the bus stop.
     * @param routeDirection   The direction of the bus route, represented by {@link RouteDirection}.
     * @param scheduledTime    The scheduled arrival time of the bus.
     * @param actualArrivalTime The actual arrival time of the bus.
     * 
     * @throws IllegalArgumentException If any of the inputs is null, tripId is empty, or scheduled and actual times are too far apart
     */
    HistoryEntry(String tripId, Route route, Stop stop, RouteDirection routeDirection,
                         LocalDateTime scheduledTime, LocalDateTime actualArrivalTime) {
        if (tripId == null || tripId.isBlank()) {
            throw new IllegalArgumentException("Trip ID must not be null or blank.");
        }
        if (route == null || stop == null || scheduledTime == null || actualArrivalTime == null) {
            throw new IllegalArgumentException("Invalid inputs");
        }
        this.tripId = tripId;
        this.route = route;
        this.stop = stop;
        this.routeDirection = routeDirection;
        this.scheduledTime = scheduledTime;
        this.actualArrivalTime = actualArrivalTime;
    }

    /**
     * Returns the unique identifier of the trip stored in this entry.
     * @return Trip ID associated with this entry
     */
    String tripId() {
        return tripId;
    }

    /**
     * Returns the route associated with this entry.
     * @return the route associated with this entry
     */
    public Route route() {
        return route;
    }

    /**
     * Returns the stop associated with this entry.
     * @return the stop associated with this entry
     */
    public Stop stop() {
        return stop;
    }

    /**
     * Returns the route direction associated with this entry.
     * @return the route direction associated with this entry
     */
    public RouteDirection routeDirection() {
        return routeDirection;
    }

    /**
     * Returns the scheduled arrival time of the vehicle at the stop this entry is for.
     * @return the scheduled arrival time of the vehicle at the stop this entry is for
     */
    public LocalDateTime scheduledTime() {
        return scheduledTime;
    }

    /**
     * Returns the actual arrival time of the vehicle at the stop this entry is for.
     * @return the actual arrival time of the vehicle at the stop this entry is for
     */
    public LocalDateTime actualArrivalTime() {
        return actualArrivalTime;
    }

    /**
     * Returns a string representation of this history entry.
     * @return a string representation of this history entry
     */
    @Override
    public String toString() {
        return  "route: " + route + "; " +
                "routeDirection: " + routeDirection + "; " +
                "stop: " + stop + "; " +
                "actualArrivalTime: " + (actualArrivalTime != null ? actualArrivalTime : "") + "; " +
                "scheduledArrivalTime: " + (scheduledTime != null ? scheduledTime : "") + ";";
    }


    /**
     * Calculates the deviation of the arrival this entry is for.
     * The deviation of an arrival is defined as the difference between the actual arrival time and the scheduled
     * arrival time.
     * The deviation is precise to about one second.
     *
     * @return the deviation of the arrival this entry is for
     */
    public Duration arrivalDeviation() {
        long deviationInSeconds = ChronoUnit.SECONDS.between(scheduledTime, actualArrivalTime);
        return Duration.ofSeconds(deviationInSeconds);
    }

    /**
     * Calculates the delay of the arrival this entry is for.
     * "The delay of an arrival is the duration it is late by, or Duration.ZERO if it is on time or early.
     * The delay is precise to about one second.
     *
     * @return the delay of the arrival this entry is for
     */
    public Duration arrivalDelay() {
        long delayInSeconds = ChronoUnit.SECONDS.between(scheduledTime, actualArrivalTime);
        if(delayInSeconds < 0) delayInSeconds = 0;
        return Duration.ofSeconds(delayInSeconds);
    }
}
