package edu.cmu.cs.prt;

import java.util.Optional;

/**
 * Represents an immutable location where vehicles pick up and drop off passengers at.
 * This class is thread-safe.
 */
public final class Stop {
    /**
     * The identifier of the stop.
     */
    private final String id;
    /**
     * The name of the stop.
     */
    private final String name;
    /**
     * The location of the stop.
     */
    private final Location location;

    static Stop createFrom(TrueTimeStop stopInternal) {
        String stopId = stopInternal.id();
        Optional<Stop> optStop = StopsCache.get(stopId);
        if (optStop.isPresent()) {
            // Cache contains key already, return cached route
            return optStop.get();
        }
        Stop newStop = new Stop(stopId, stopInternal.name(),
                new Location(stopInternal.latitude(), stopInternal.longitude()));
        StopsCache.put(newStop);
        return newStop;
    }

    static Stop createFrom(String stopId, String stopName, Location location) {
        Optional<Stop> optStop = StopsCache.get(stopId);
        // Cache contains key already, return cached route
        return optStop.orElseGet(() -> new Stop(stopId, stopName, location));
    }

    static Optional<Stop> of(String stopId) {
        return StopsCache.get(stopId);
    }

    private Stop(String id, String name, Location location) {
        this.id = id;
        this.name = name;
        this.location = location;
    }

    /**
     * Returns the unique identifier of this stop.
     * @return Stop's unique ID
     */
    String id() {
        return id;
    }

    /**
     * Returns the English name of this stop.
     * @return the name of this stop
     */
    public String name() {
        return name;
    }

    /**
     * Returns the location of this stop.
     * @return the location of this stop
     */
    public Location location() {
        return location;
    }

    /**
     * Compares the specified object with this stop for equality. Two stops are equal to each other if and only if they
     * have the same stop ID.
     * 
     * @param anObject Object to compare this stop to
     * @return True if the input is equal to this stop, false otherwise
     */
    @Override
    public boolean equals(Object anObject) {
        try {
            Stop otherRoute = (Stop) anObject;
            return id.equals(otherRoute.id());
        } catch (ClassCastException e) {
            return false;
        }
    }

    /**
     * Returns the hash code of this stop.
     * @return the hash code of this stop
     */
    @Override
    public int hashCode() {
        return id.hashCode();
    }

    /**
     * Returns the string representation of this stop.
     * @return the string representation of this stop
     */
    @Override
    public String toString() {
        return "Stop " + name + ", location: " + location;
    }
}
