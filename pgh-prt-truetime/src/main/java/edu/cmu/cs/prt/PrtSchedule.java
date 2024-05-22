package edu.cmu.cs.prt;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

/**
 * A static utility class that contains methods to get information related to the scheduled
 * arrival of vehicles at stops.
 * <p>
 * For example, get all arrival times at the CMU bus stop for 61D, heading inbound,
 * after 6:00pm today:
 * <pre>
 * Route route = PrtInfo.routeOf("61D").get();
 * // Get scheduled trips for 61D with customizations mentioned above
 * List{@literal <}ScheduledTrip{@literal >} trips = new PrtSchedule.Selector().forRoute(route)
 *                                                   .onDate(LocalDate.now())
 *                                                   .inDirection(RouteDirection.INBOUND)
 *                                                   .after(LocalTime.of(18, 0))
 *                                                   .trips();
 * // Get the CMU bus stop (FORBES AVE + MOREWOOD AVE)
 * Stop stop = PrtInfo.closestStop(new Location(40.443418243876266, -79.94288909575457));
 * List{@literal <}LocalTime{@literal >} arrivalTimes = PrtSchedule.arrivalTimesFor(trips, stop);
 * </pre>
 */
public final class PrtSchedule {
    /**
     * Mapping between trip ID and scheduled arrivals.
     */
    private static Map<String, List<ScheduledArrival>> tripIdToArrivals;

    /**
     * Mapping between trip ID and the trip's direction.
     */
    private static Map<String, RouteDirection> tripIdToDirection;

    /**
     * Mapping between trip ID and the trip's service availability.
     */
    private static Map<String, ServiceAvailability> tripIdToAvailability;

    /**
     * Mapping between trip ID and the trip's corresponding route.
     */
    private static Map<String, Route> tripIdToRoute;

    /**
     * Mapping between a route ID and trip IDs of the trips it services.
     */
    private static Map<String, List<String>> routeIdToTripIds;

    /**
     * Mapping between trip IDs and the actual ScheduledTrip object it refers to.
     */
    private static Map<String, ScheduledTrip> tripIdToScheduledTrip;

    private static final Set<String> inclineRoutes = Set.of("MI", "DQI");

    static {
        init();
    }

    private PrtSchedule() {};

    /**
     * Populates schedule by parsing stored text file.
     */
    private static void init() {
        tripIdToArrivals = new HashMap<>();
        tripIdToDirection = new HashMap<>();
        tripIdToAvailability = new HashMap<>();
        tripIdToRoute = new HashMap<>();
        tripIdToScheduledTrip = new HashMap<>();
        routeIdToTripIds = new HashMap<>();
        try {
            File file = new File("schedule-parsing/schedule.txt");
            Scanner sc = new Scanner(file);

            boolean ignoreHeadings = true;
            while (sc.hasNextLine()) {
                if (ignoreHeadings) { // Ignore the column headers (i.e. 1st row)
                    sc.nextLine();
                    ignoreHeadings = false;
                } else {
                    String str = sc.nextLine();

                    // Parse by row
                    String[] strList = str.split(",");
                    String tripId = strList[0];
                    String routeId = strList[1];
                    String arrivalTime = strList[4];
                    String stop_id = strList[6];
                    String stop_name = strList[7];

                    // Skip incline routes
                    if (inclineRoutes.contains(routeId)) {
                        continue;
                    }

                    // Parse routeId, direction, and availability
                    if (!tripIdToDirection.containsKey(tripId)) {
                        // Match tripId with routeId
                        if (routeIdToTripIds.containsKey(routeId)) {
                            routeIdToTripIds.get(routeId).add(tripId);
                        } else {
                            List<String> tripIds = new ArrayList<>(Collections.singleton(tripId));
                            routeIdToTripIds.put(routeId, tripIds);
                        }

                        // Get route from cache; if it doesn't exist, create new
                        Route route;
                        Optional<Route> optRoute = RoutesCache.get(routeId);
                        if (optRoute.isEmpty()) {
                            throw new RuntimeException("Route with id " + routeId + " not found in cache");
                        }
                        route = optRoute.get();

                        ServiceAvailability availability = ServiceAvailability.fromServiceId(strList[2]);
                        RouteDirection direction = RouteDirection.convert(Integer.parseInt(strList[3]));

                        // Store information in temporary maps
                        tripIdToAvailability.put(tripId, availability);
                        tripIdToDirection.put(tripId, direction);
                        tripIdToRoute.put(tripId, route);
                    }

                    // Parse stop information
                    double latitude = Double.parseDouble(strList[8]);
                    double longitude = Double.parseDouble(strList[9]);

                    // Get stop from cache; if it doesn't exist, create new
                    Stop stop;
                    Optional<Stop> optStop = StopsCache.get(stop_id);
                    stop = optStop.orElseGet(() -> Stop.createFrom(stop_id, stop_name, new Location(latitude, longitude)));

                    // Create a ScheduledArrival instance and add to trip's arrivals list
                    ScheduledArrival scheduledArrival =
                            ScheduledArrival.createFrom(stop, arrivalTime, routeId, tripIdToDirection.get(tripId));
                    if (tripIdToArrivals.containsKey(tripId)) {
                        tripIdToArrivals.get(tripId).add(scheduledArrival);
                    } else {
                        List<ScheduledArrival> stops = new ArrayList<>();
                        stops.add(scheduledArrival);
                        tripIdToArrivals.put(tripId, stops);
                    }
                }
            }
            sc.close();
            computeSchedule(); // Create the final schedule
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Create schedule based on temporary maps.
     */
    private static void computeSchedule() {
        for (String tripId : tripIdToArrivals.keySet()) {
            List<ScheduledArrival> stops = tripIdToArrivals.get(tripId);
            ScheduledTrip trip = ScheduledTrip.createFrom(tripIdToDirection.get(tripId), tripId,
                tripIdToRoute.get(tripId), tripIdToAvailability.get(tripId), stops);
            tripIdToScheduledTrip.put(tripId, trip);
        }
    }

    /**
     * Returns the date on which this schedule was put into effect.
     * @return Schedule start date
     */
    public static LocalDate startDate() {
        for (ScheduledTrip trip : tripIdToScheduledTrip.values()) {
            return trip.scheduleStartDate();
        }
        return LocalDate.MIN;
    }

    /**
     * Returns the date on which this schedule will expire.
     * @return Schedule expiration date
     */
    public static LocalDate expirationDate() {
        for (ScheduledTrip trip : tripIdToScheduledTrip.values()) {
            return trip.scheduleExpirationDate();
        }
        return LocalDate.MIN;
    }

    /**
     * Returns a list of all arrival times of input trips at given stop.
     * Returned list will be sorted from the earliest to the latest arrival time.
     * <p>
     * If the input {@code trips} is empty, or if none of the input trips pass
     * through the input stop, an empty list will be returned.
     * 
     * @param trips Scheduled trips to search through
     * @param stop Stop to search for
     * @return List of arrival times for input route at stop
     */
    public static List<LocalTime> arrivalTimesFor(Collection<ScheduledTrip> trips, Stop stop) {
        List<LocalTime> arrivals = new ArrayList<LocalTime>(trips.size());
        for (ScheduledTrip trip : trips) {
            if (trip.hasStop(stop)) {
                arrivals.add(trip.arrivalTimeAt(stop).get());
            }
        }
        Collections.sort(arrivals);
        return arrivals;
    }

    /**
     * Utility function for Schedule.Selector to build its results.
     * @param scheduleSelector
     * @return List of selected trips
     */
    private static List<ScheduledTrip> constructTripsList(Selector scheduleSelector) {
        List<ScheduledTrip> trips = null;

        // Filter by routeId
        if (!scheduleSelector.routeId.isEmpty()) {
            List<String> tripIds = routeIdToTripIds.get(scheduleSelector.routeId);
            if (tripIds != null) {
                trips = new ArrayList<ScheduledTrip>(tripIds.size());
                for (String tripId : tripIds) {
                    trips.add(tripIdToScheduledTrip.get(tripId));
                }
            }
        }
        if (trips == null) {
            trips = new ArrayList<ScheduledTrip>(tripIdToScheduledTrip.values());
        }

        Iterator<ScheduledTrip> tripsIter = trips.iterator();

        while (tripsIter.hasNext()) {
            ScheduledTrip trip = tripsIter.next();
            // Filter by stopId
            if (!scheduleSelector.stopId.isEmpty() && !trip.hasStop(scheduleSelector.stopId)) {
                tripsIter.remove();
                continue;
            }
            // Filter by routeDirection
            if ((scheduleSelector.routeDirection != null) && !trip.direction().equals(scheduleSelector.routeDirection)) {
                tripsIter.remove();
                continue;
            }
            // Filter by date
            if ((scheduleSelector.date != null) && !trip.availableOn(scheduleSelector.date)) {;
                tripsIter.remove();
                continue;
            }
            // Filter by dayOfWeek
            if ((scheduleSelector.dayOfWeek != null) && !trip.availableOn(scheduleSelector.dayOfWeek)) {
                tripsIter.remove();
                continue;
            }
            // Filter by afterTime
            if ((scheduleSelector.afterTime != null) && !trip.startTime().isAfter(scheduleSelector.afterTime)) {
                tripsIter.remove();
                continue;
            }
            // Filter by beforeTime
            if ((scheduleSelector.beforeTime != null) && !trip.startTime().isBefore(scheduleSelector.beforeTime)) {
                tripsIter.remove();
            }
        }

        return trips;
    }

    /**
     * Utility function for getting routes that run through the input stop.
     * @param stop
     * @return
     */
    static List<Route> routesThru(Stop stop) {
        List<ScheduledTrip> trips = new PrtSchedule.Selector().atStop(stop).trips();
        Set<Route> routes = new HashSet<Route>();
        for (ScheduledTrip trip : trips) {
            routes.add(trip.route());
        }
        return new ArrayList<Route>(routes);
    }

    /**
     * Builder pattern that allows customized querying of schedule.
     * <p>
     * To get a list of trips with the customizable parameters, follow the builder
     * pattern by chaining the parameters. To get the result of the customized query,
     * call {@code .trips()}.
     * <p>
     * For example, to get all available trips for 61D, heading outbound, during
     * 6:00pm and 10:30pm today, use the following chain of parameters:
     * <pre>
     * Route route = PrtInfo.routeOf("61D").get();
     * List{@literal <}ScheduledTrip{@literal >} trips = new PrtSchedule.Selector().forRoute(route)
     *                                                   .onDate(LocalDate.now())
     *                                                   .inDirection(RouteDirection.OUTBOUND)
     *                                                   .after(LocalTime.of(18, 0))
     *                                                   .before(LocalTime.of(22, 30))
     *                                                   .trips();
     * </pre>
     */
    public static class Selector {
        private String routeId;
        private String stopId;
        private RouteDirection routeDirection;
        private LocalDate date;
        private DayOfWeek dayOfWeek;
        private LocalTime afterTime;
        private LocalTime beforeTime;

        /**
         * Constructs a selector used for querying the schedule on different conditions.
         */
        public Selector() {
            routeId = "";
            stopId = "";
            routeDirection = null;
            date = null;
            dayOfWeek = null;
            afterTime = null;
            beforeTime = null;
        }

        /**
         * Query the schedule based on specified route.
         * @param route Route to query the schedule for
         * @return This selector
         */
        public Selector forRoute(Route route) {
            this.routeId = route.id();
            return this;
        }

        /**
         * Query the schedule based on specified stop.
         * @param stop Stop to query the schedule for
         * @return This selector
         */
        public Selector atStop(Stop stop) {
            this.stopId = stop.id();
            return this;
        }

        /**
         * Query the schedule based on direction of a trip.
         * @param routeDirection Direction in which a returned scheduled trip head
         * @return This selector
         */
        public Selector inDirection(RouteDirection routeDirection) {
            this.routeDirection = routeDirection;
            return this;
        }

        /**
         * Query the schedule based on the input date.
         * <p>
         * The date's day of the week time must match what is entered for 
         * {@code onDayOfWeek()}, if any was provided.
         * 
         * @param date Date on which a scheduled trip is serviced
         * @return This selector
         * @throws IllegalArgumentException If {@code date} doesn't match what is entered for {@code onDayOfWeek()}
         */
        public Selector onDate(LocalDate date) {
            if ((this.dayOfWeek != null) && (date.getDayOfWeek() != this.dayOfWeek)) {
                throw new IllegalArgumentException(String.format("Date's day of week (%s) incompatible with that of previously specified onDayOfWeek(): %s",
                                                   date.getDayOfWeek().toString(), this.dayOfWeek.toString()));
            }
            this.date = date;
            return this;
        }

        /**
         * Query the schedule based on the input day of the week.
         * <p>
         * The  day of the week time must match that of what is entered for 
         * {@code onDate()}, if any was provided.
         * 
         * @param dayOfWeek Day of the week on which a scheduled trip is serviced
         * @return This selector
         * @throws IllegalArgumentException If {@code dayOfWeek} doesn't match that of what is entered for {@code date()}
         */
        public Selector onDayOfWeek(DayOfWeek dayOfWeek) {
            if ((this.date != null) && (this.date.getDayOfWeek() != dayOfWeek)) {
                throw new IllegalArgumentException(String.format("Day of week (%s) incompatible with that of previously specified onDate(): %s",
                                                   dayOfWeek.toString(), this.date.getDayOfWeek().toString()));
            }
            this.dayOfWeek = dayOfWeek;
            return this;
        }

        /**
         * Query the schedule for trips scheduled after the input time.
         * <p>
         * For example, {@code after(LocalTime.of(6,0))} will limit resulting
         * trips' start times to after {@code 06:00} or 6:00am.
         * <p>
         * The input time must be before the time entered for {@code before()}, if any
         * was provided.
         * 
         * @param time Time after which scheduled trips should start
         * @return This selector
         * @throws IllegalArgumentException If {@code time} is after time entered for {@code before()}
         */
        public Selector after(LocalTime time) {
            if ((this.beforeTime != null) && !time.isBefore(this.beforeTime)) {
                throw new IllegalArgumentException(String.format("%s incompatible with previously specified before(): %s",
                                                   time.toString(), this.beforeTime.toString()));
            }
            this.afterTime = time;
            return this;
        }

        /**
         * Query the schedule for trips scheduled before the input time.
         * <p>
         * For example, {@code before(LocalTime.of(16,0))} will limit resulting
         * trips' start times to before {@code 16:00} or 4:00pm.
         * <p>
         * The input time must be after the time entered for {@code after()}, if any
         * was provided.
         * 
         * @param time Time before which scheduled trips should start
         * @return This selector
         * @throws IllegalArgumentException If {@code time} is before time entered for {@code after()}
         */
        public Selector before(LocalTime time) {
            if ((this.afterTime != null) && !time.isAfter(this.afterTime)) {
                throw new IllegalArgumentException(String.format("%s incompatible with previously specified after(): %s",
                                                   time.toString(), this.afterTime.toString()));
            }
            this.beforeTime = time;
            return this;
        }

        /**
         * Returns all trips that satisfy the parameters given to this selector.
         * @return List of trips that satisfy the input parameters
         */
        public List<ScheduledTrip> trips() {
            return PrtSchedule.constructTripsList(this);
        }
    }
}
