package edu.cmu.cs.prt;

import java.time.LocalTime;
import java.util.Comparator;
import java.time.DateTimeException;
import java.util.Optional;

/**
 * Represents the scheduled arrival of a vehicle at a stop.
 */
public final class ScheduledArrival implements Arrival {
    /**
     * The stop that this scheduled arrival is for.
     */
    private final Stop stop;

    /**
     * The scheduled arrival time.
     */
    private final LocalTime time;

    /**
     * The route that this scheduled arrival is for.
     */
    private final Route route;

    private final RouteDirection routeDirection;

    static ScheduledArrival createFrom(Stop stop, String arrivalTime, String routeId, RouteDirection routeDirection) {
        Optional<Route> route = RoutesCache.get(routeId);
        if (route.isEmpty()) {
            throw new IllegalArgumentException("Route with id " + routeId + " not found");
        }
        return new ScheduledArrival(stop, convertToLocalTime(arrivalTime), route.get(), routeDirection);
    }

    private static LocalTime convertToLocalTime(String str) {
        try {
            return LocalTime.parse(str);
        } catch (DateTimeException e1) {
            try {
                // Schedule may have times that go past midnight (e.g. 24:00:01)
                String[] strArr = str.split(":");
                return LocalTime.of(Integer.parseInt(strArr[0]) - 24, Integer.parseInt(strArr[1]), Integer.parseInt(strArr[2]));
            } catch (IndexOutOfBoundsException | NumberFormatException | DateTimeException e2) {
                System.out.printf("Parse error for %s%n", str);
                return LocalTime.MIN;
            }
        }
    }

    private ScheduledArrival(Stop stop, LocalTime arrivalTime, Route route, RouteDirection routeDirection) {
        this.stop = stop;
        this.time = arrivalTime;
        this.route = route;
        this.routeDirection = routeDirection;
    };

    /**
     * Returns the transit's scheduled arrival time at this stop.
     * <p>
     * Time is represented in 24 hours (00:00 to 23:59) and precise to the 
     * seconds.
     * <p>
     * It's possible that this time is earlier than start time of the
     * entire trip. This is because the trip spans a period of time that crosses
     * midnight, so time is reset from 23:59 to 00:00.
     * 
     * @return Scheduled arrival time at stop
     */
    public LocalTime time() {
        return time;
    }

    /**
     * Returns the stop associated with this scheduled arrival.
     * @return the stop associated with this scheduled arrival
     */
    public Stop stop() {
        return stop;
    }

    /**
     * Returns the route associated with this scheduled arrival.
     * @return Route that services this stop
     */
    public Route route() {
        return route;
    }

    /**
     * Returns the direction in which the scheduled trip that passes this stop runs.
     * @return Trip that passes this stop
     */
    public RouteDirection routeDirection() {
        return routeDirection;
    }

    /**
     * Returns a string representation of the scheduled arrival.
     */
    @Override
    public String toString() {
        String str = stop.toString() + "\n  Arrival time: " +
                time.toString();
        return str;
    }

    /**
     * For sorting stops based on arrival time.
     */
    static class LocalTimeAscending implements Comparator<ScheduledArrival> {
        @Override
        public int compare(ScheduledArrival stop1, ScheduledArrival stop2) {
            if (stop1.time().equals(stop2.time())) {
                return 0;
            }
            if (stop1.time().isAfter(stop2.time())) {
                return 1;
            }
            return -1;
        }
    }
}
