package edu.cmu.cs.prt;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import java.util.List;

/**
 * Represents a scheduled trip for a route.
 * <p>
 * A trip is defined as one service (from start stop to destination stop) 
 * through a transit route in a direction.
 * <p>
 * Each stop in a trip will be associated with a scheduled arrival time and a departure time.
 */
public final class ScheduledTrip {
    static ScheduledTrip createFrom(RouteDirection direction, String tripId, Route route,
                                    ServiceAvailability availability, List<ScheduledArrival> scheduledArrivals) {
        Map<String, ScheduledArrival> stopIdToArrival = new HashMap<>(scheduledArrivals.size());
        for (ScheduledArrival arrival : scheduledArrivals) {
            stopIdToArrival.put(arrival.stop().id(), arrival);
        }
        return new ScheduledTrip(direction, tripId, route, availability, stopIdToArrival);
    }

    private final RouteDirection direction;

    private final String tripId;

    private final Route route;

    private final ServiceAvailability availability;

    private final Map<String, ScheduledArrival> stopIdToStops;

    private final List<ScheduledArrival> sortedStops;

    private ScheduledTrip(RouteDirection direction, String tripId, Route route,
                          ServiceAvailability availability, Map<String, ScheduledArrival> stops) {
        this.direction = direction;
        this.tripId = tripId;
        this.route = route;
        this.availability = availability;
        this.stopIdToStops = stops;
        this.sortedStops = new ArrayList<ScheduledArrival>(stops.size());
        this.sortedStops.addAll(this.stopIdToStops.values());
        this.sortedStops.sort(new ScheduledArrival.LocalTimeAscending());
    }

    /**
     * Returns the direction (INBOUND or OUTBOUND) that the trip is servicing.
     * @return Direction of trip
     */
    public RouteDirection direction() {
        return direction;
    }

    /**
     * Returns the unique identifier of this trip.
     * @return Trip ID
     */
    public String tripId() {
        return tripId;
    }

    /**
     * Returns the route that this trip is servicing.
     * <p>
     * For example, a trip that operates on the 61B route will return
     * a {@code Route} instance of 61B.
     * 
     * @return Route this trip belongs to
     */
    public Route route() {
        return route;
    }

    /**
     * Returns whether this trip is available on the input date.
     * @param date Date to check availability for
     * @return True if trip is available on given date; false otherwise
     */
    public boolean availableOn(LocalDate date) {
        return availableOn(date.getDayOfWeek());
    }

    /**
     * Returns whether this trip is available on the input day of the week.
     * @param dayOfWeek Day of the week to check availability for
     * @return True if trip is available on given day of the week; false otherwise
     */
    public boolean availableOn(DayOfWeek dayOfWeek) {
        switch (dayOfWeek) {
            case SATURDAY:
                return availability.availableSaturday();
            case SUNDAY:
                return availability.availableSunday();
            case MONDAY:
            case TUESDAY:
            case WEDNESDAY:
            case THURSDAY:
            case FRIDAY:
                return availability.availableWeekdays();
            default:
                return false;
        }
    }

    /**
     * @return Start date of this schedule
     */
    LocalDate scheduleStartDate() {
        return availability.startDate();
    }

    /**
     * @return Expiration date of this schedule
     */
    LocalDate scheduleExpirationDate() {
        return availability.expirationDate();
    }

    /**
     * Returns the arrival time of the trip's first stop.
     * <p>
     * Time is represented in 24 hours (00:00 to 23:59) and precise to the 
     * seconds.
     * @return Arrival time of the trip's first stop
     */
    public LocalTime startTime() {
        return sortedStops.get(0).time();
    }

    /**
     * Returns the scheduled stops for the trip.
     * <p>
     * The stops are sorted based on arrival time in ascending order (from
     * soonest to the most faraway timewise, i.e. 06:00:00 &lt; 06:00:01).
     * 
     * @return Scheduled stops for the trip
     */
    public List<ScheduledArrival> stops() {
        List<ScheduledArrival> stopList = new ArrayList<ScheduledArrival>(stopIdToStops.size());
        for (ScheduledArrival stop : sortedStops) {
            stopList.add(stop);
        }
        return stopList;
    }

    /**
     * Returns whether this trip services the input stop.
     * @param stop Stop to check for
     * @return True if this trip passes through the input stop; false otherwise
     */
    public boolean hasStop(Stop stop) {
        return stopIdToStops.containsKey(stop.id());
    }

    boolean hasStop(String stopId) {
        return stopIdToStops.containsKey(stopId);
    }

    /**
     * Returns the time at which this trip arrives at the input stop, if applicable.
     * <p>
     * If this trip doesn't service the input stop, {@code Optional.empty()}
     * will be returned.
     * 
     * @param stop Stop to check for
     * @return Time at which this trip arrives at the input stop
     */
    public Optional<LocalTime> arrivalTimeAt(Stop stop) {
        return arrivalTimeAt(stop.id());
    }

    Optional<LocalTime> arrivalTimeAt(String stopId) {
        if (!hasStop(stopId)) {
            return Optional.empty();
        }
        return Optional.of(stopIdToStops.get(stopId).time());
    }

    /**
     * Returns the string representation of a {@code ScheduledTrip}.
     */
    @Override
    public String toString() {
        StringBuilder str = new StringBuilder("Trip ID: ");
        str.append(tripId);
        str.append("\nStart time: ");
        str.append(startTime().toString());
        str.append("\n");
        str.append(availability.toString());
        str.append("\nDirection: ");
        str.append(direction.name());
        str.append("\n-----\nRoute Info:\n");
        str.append(route.toString());
        str.append("\n-----\nStop Info:");
        for (ScheduledArrival stop : stops()) {
            str.append("\n");
            str.append(stop.toString());
        }
        return str.toString();
    }
}
