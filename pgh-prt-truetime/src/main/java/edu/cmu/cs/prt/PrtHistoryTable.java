package edu.cmu.cs.prt;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.*;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Represents a collection of {@link HistoryEntry} instances, encapsulating data related to vehicle arrival events.
 * This class provides functionality to filter and manipulate a collection of history entries for
 * various analytical purposes. It supports operations like filtering based on different criteria
 * (e.g., date, route ID, stop), calculating average delays, and sorting entries.
 *
 * <p>The {@code PrtHistoryTable} is designed as a singleton to ensure a consistent and centralized management of
 * history entries across the application. It is loaded from a source file and can be filtered to produce
 * new instances of {@code PrtHistoryTable} containing a subset of the data.</p>
 *
 * The {@code PrtHistoryTable} supports a variety of filter operations, which return a new {@code PrtHistoryTable} that
 * contains a subset of the entries in the original table.
 *
 * Filter operations assume the given object is not null and are valid (e.g. Trip object has valid Route information)
 *
 * This class is immutable.
 */
public class PrtHistoryTable {

    private static PrtHistoryTable instance;
    LocalDateTime createdTime;
    List<HistoryEntry> historyEntryList;

    /**
     * Private constructor for {@code PrtHistoryTable}. Initializes the history entry list and sets the creation time.
     *
     * @param historyEntryList the list of HistoryEntry objects to initialize the table with
     */
    private PrtHistoryTable (List<HistoryEntry> historyEntryList){
        this.historyEntryList = historyEntryList;
        this.createdTime = LocalDateTime.now();
    }

    /**
     * Returns a singleton instance of PrtHistoryTable containing all known history entries.
     * @return a singleton instance of PrtHistoryTable
     */
    public static synchronized PrtHistoryTable fullTable() {
        if (instance == null) {
            List<HistoryEntry> entries = readFromSourceFile();
            instance = new PrtHistoryTable(entries);
        }
        return instance;
    }

    /**
     * Reads history entries from a source file and returns a list of HistoryEntry objects.
     * This method parses the file containing history data, constructing HistoryEntry
     * objects from each line of the file.
     *
     * @return A List of HistoryEntry objects read from the source file
     */
    private static List<HistoryEntry> readFromSourceFile() {
        List<HistoryEntry> entries = new ArrayList<>();
        String fileName = "history-parsing/history.txt";

        try (Scanner scanner = new Scanner(new File(fileName))) {
            if (!scanner.hasNextLine()) {
                return entries;
            }

            String headerLine = scanner.nextLine();
            Map<String, Integer> headerIndexMap = createHeaderIndexMap(headerLine);

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                HistoryEntry entry = parseLineToHistoryEntry(line, headerIndexMap);
                if (entry != null) {
                    entries.add(entry);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return entries;
    }

    /**
     * Returns the time this history table was created. This history table will not contain any entries timestamped
     * after this time.
     * <p>
     * This method can be used to check how up-to-date this table is.
     *
     * @return time at which this table was created
     */
    public LocalDateTime createdAt(){
        return createdTime;
    }

    /**
     * Returns the number of history entries currently stored in this table. This count reflects the current state of
     * this table, accounting for any filtering operations that may have been performed.
     *
     * @return the total number of history entries in this table
     */
    public long numberOfEntries(){
        return historyEntryList.size();
    }

    /**
     * Returns all history entries in this table. This method returns {@link #numberOfEntries()} entries.
     * <p>
     * <b>Warning:</b> Expensive if called on a unfiltered or lightly filtered table.
     *
     * @return the history entries in this table
     */
    public List<HistoryEntry> entryList(){
        return historyEntryList;
    }

    /**
     * Returns a {@code PrtHistoryTable} which only contains history entries based on the provided generic filter
     * from this {@code PrtHistoryTable}.
     * The filter is a lambda expression that defines the criteria for including an entry in the result.
     *
     * @param <T>       the type of the filter criterion
     * @param extractor a functional interface that extracts a field from a HistoryEntry to be compared
     * @param toEquate  the value to be compared against the extracted field
     * @return a new {@code PrtHistoryTable} containing entries that match the filter criteria
     */
    private <T> PrtHistoryTable filter(PropertyExtractor<T> extractor, T toEquate) {
        List<HistoryEntry> filteredEntries = historyEntryList.stream()
                .filter(entry -> toEquate.equals(extractor.extract(entry)))
                .collect(Collectors.toList());
        return new PrtHistoryTable(filteredEntries);
    }

    /**
     * Returns a {@code PrtHistoryTable} which only contains history entries for the given route from this
     * {@code PrtHistoryTable}.
     *
     * @param route the route to filter by
     * @return a new {@code PrtHistoryTable} instance with entries filtered by the given route
     */
    public PrtHistoryTable ofRoute(Route route) {
        String routeId = route.id();
        return filter(entry -> entry.route().id(), routeId);
    }

    /**
     * Returns a {@code PrtHistoryTable} which only contains history entries for the given stop from this
     * {@code PrtHistoryTable}.
     *
     * @param stop the stop to filter by
     * @return a new {@code PrtHistoryTable} instance with entries filtered by the given stop
     */
    public PrtHistoryTable ofStop(Stop stop) {
        String stopId = stop.id();
        return filter(entry -> entry.stop().id(), stopId);
    }

    /**
     * Returns a {@code PrtHistoryTable} which only contains history entries for the given trip from this
     * {@code PrtHistoryTable}.
     * @param trip the trip to filter by
     * @return a new {@code PrtHistoryTable} with entries filtered by the given trip
     */
    public PrtHistoryTable ofTrip(ScheduledTrip trip) {
        List<ScheduledArrival> stops = trip.stops();

        return filter(entry -> {
            for(ScheduledArrival s : stops){
                if( entry.route().id().equals(trip.route().id())
                        && s.stop().id().equals(entry.stop().id())
                        && Duration.between(s.time(), entry.scheduledTime()).abs().toMinutes() <= 10 )
                    // these times should match exactly because they are from the same source
                    return true;
            }
            return false;
        }, true);
    }

    /**
     * Returns a {@code PrtHistoryTable} which only contains history entries on the given date from this
     * {@code PrtHistoryTable}.
     * @param date the date to filter by
     * @return a new {@code PrtHistoryTable} with entries filtered by the given date
     */
    public PrtHistoryTable onDate(LocalDate date) {
        return filter(entry -> entry.actualArrivalTime().toLocalDate(), date);
    }

    /**
     * Returns a {@code PrtHistoryTable} which only contains history entries of the given day of the week from this
     * {@code PrtHistoryTable}.
     * @param dayOfWeek the day of the week to filter by
     * @return a new {@code PrtHistoryTable} with entries filtered by the given day of the week
     */
    public PrtHistoryTable onDayOfWeek(DayOfWeek dayOfWeek) {
        return filter(entry -> entry.actualArrivalTime().getDayOfWeek(), dayOfWeek);
    }

    /**
     * Returns a {@code PrtHistoryTable} which contains history entries of this {@code PrtHistoryTable} that evaluate
     * as true using the given predicate.
     * @param filter the predicate to filter by
     * @return a new {@code PrtHistoryTable} with entries filtered by the given predicate
     */
    public PrtHistoryTable filter(Predicate<HistoryEntry> filter) {
        List<HistoryEntry> filteredEntries = historyEntryList.stream()
                .filter(filter)
                .collect(Collectors.toList());
        return new PrtHistoryTable(filteredEntries);
    }

    /**
     * Returns a {@code PrtHistoryTable} which only contains history entries after the given timestamp from this
     * {@code PrtHistoryTable}
     *
     * @param time the timestamp to filter by
     * @return a new {@code PrtHistoryTable} with entries after the given timestamp
     */
    public PrtHistoryTable afterDateTime(LocalDateTime time){
        return filter(entry -> entry.actualArrivalTime().isAfter(time));
    }

    /**
     * Returns a {@code PrtHistoryTable} which only contains history entries before the given timestamp from this
     * {@code PrtHistoryTable}.
     *
     * @param time the timestamp to filter by
     * @return a new {@code PrtHistoryTable} with entries before the given date timestamp
     */
    public PrtHistoryTable beforeDateTime(LocalDateTime time){
        return filter(entry -> entry.actualArrivalTime().isBefore(time));
    }

    /**
     * Returns a {@code PrtHistoryTable} which only contains history entries before the given time from this
     * {@code PrtHistoryTable}.
     *
     * @param time the time to filter by
     * @return a new {@code PrtHistoryTable} with entries before the given time
     */
    public PrtHistoryTable beforeTime(LocalTime time) {
        return filter(entry -> entry.actualArrivalTime().toLocalTime().isBefore(time));
    }

    /**
     * Returns a new {@code PrtHistoryTable} which only contains history entries after the given timestamp from this
     * {@code PrtHistoryTable}.
     *
     * @param time the time to filter by
     * @return a new {@code PrtHistoryTable} with entries after the given time
     */
    public PrtHistoryTable afterTime(LocalTime time){
        return filter(entry -> entry.actualArrivalTime().toLocalTime().isAfter(time));
    }

    /**
     * Calculates the average deviation across all entries in this history table.
     * If there are no entries in this table, this method defaults to Duration.ZERO, indicating no known delay.
     * <p>
     * The "deviation" of a {@link HistoryEntry} is the actual arrival time minus the scheduled arrival time for the
     * arrival that the entry corresponds to.
     * Deviation is positive if the arrival is late and negative if the arrival is early.
     * If you want to ignore the magnitude a vehicle is early by, see {@link PrtHistoryTable#averageDelay()}.
     * For details on deviation, see {@link HistoryEntry#arrivalDeviation()}.
     *
     * The precision of the division is determined by {@link Duration#dividedBy(long)}.
     *
     * @return the average deviation, which may be negative
     */
    public Duration averageDeviation(){
        if (historyEntryList.isEmpty()) {
            return Duration.ZERO;
        }

        long delaySeconds = sumValue(entry -> entry.arrivalDeviation().toSeconds());
        Duration totalDelay = Duration.ofSeconds(delaySeconds);
        int delayedVehicles = historyEntryList.size();
        return totalDelay.dividedBy(delayedVehicles);
    }

    /**
     * Calculates the average delay across all entries in this history table.
     * If there are no entries in this table, this method defaults to Duration.ZERO, indicating no known delay.
     * <p>
     * The "delay" of a {@link HistoryEntry} is the duration of lateness of the arrival that the entry corresponds to.
     * The delay is zero if the arrival is on time or early.
     * If you want to consider how early a vehicle is, see {@link PrtHistoryTable#averageDeviation()}.
     * For details on delay, see {@link HistoryEntry#arrivalDelay()}.
     *
     * @return the average delay
     */
    public Duration averageDelay(){
        if (historyEntryList.isEmpty()) {
            return Duration.ZERO;
        }

        long delaySeconds = sumValue(entry -> entry.arrivalDelay().toSeconds());
        Duration totalDelay = Duration.ofSeconds(delaySeconds);
        int delayedVehicles = historyEntryList.size();
        return totalDelay.dividedBy(delayedVehicles);
    }

    /**
     * Takes in an extractor function for evaluating each entry into a long, then sums all longs
     * @param extractor
     * @return
     */
    private long sumValue(PropertyExtractor<Long> extractor){
        long value = 0;
        for (HistoryEntry entry : historyEntryList) {
            long delay = extractor.extract(entry);
            value += delay;
        }

        return value;
    }

    /**
     * Returns a new {@code PrtHistoryTable} with history entries from this {@code PrtHistoryTable} sorted using the
     * given comparator. The results of a sort can be accessed with {@link PrtHistoryTable#topNEntries(int)}.
     *
     * @param comparator the comparator to use for sorting
     * @return a new {@code PrtHistoryTable} with sorted entries
     */
    public PrtHistoryTable sortBy(Comparator<HistoryEntry> comparator) {
        return new PrtHistoryTable(this.entryList().stream()
                .sorted(comparator)
                .collect(Collectors.toList()));
    }

    /**
     * Returns the list of top N entries from this history table, in order from topmost to bottommost.
     * Which entries are considered "topmost" in the history table can be modified using
     * {@link PrtHistoryTable#sortBy(Comparator)}.
     *
     * If this history table is unsorted, the order of the entries is implementation defined.
     *
     * @param n the number of top entries to retrieve
     * @return at most N top entries
     */
    public List<HistoryEntry> topNEntries(int n) {
        return this.entryList().stream()
                .limit(n)
                .collect(Collectors.toList());
    }

    private static Map<String, Integer> createHeaderIndexMap(String headerLine) {
        String[] headers = headerLine.split(",");
        Map<String, Integer> headerIndexMap = new HashMap<>();
        for (int i = 0; i < headers.length; i++) {
            headerIndexMap.put(headers[i].trim(), i);
        }
        return headerIndexMap;
    }

    private static HistoryEntry parseLineToHistoryEntry(String line, Map<String, Integer> headerIndexMap) {
        String[] parts = line.split(",");

        String tripId = parts[headerIndexMap.get("tatripid")].trim();
        String routeId = parts[headerIndexMap.get("route_id")].trim();
        String stopId = parts[headerIndexMap.get("stop_id")].trim();
        String direction = parts[headerIndexMap.get("direction")].trim();

        LocalDateTime actualArrivalTime = LocalDateTime.parse(parts[headerIndexMap.get("log_time")].trim());
        LocalDateTime scheduledTime = LocalDateTime.parse(parts[headerIndexMap.get("scheduled_start_date")].trim() + "T" +
                parts[headerIndexMap.get("scheduled_start_time")].trim());

        try {
            return new HistoryEntry(tripId, 
                PrtInfo.routeOf(routeId).orElseThrow(() -> new IllegalArgumentException("Invalid or unknown route ID: " + routeId)),
                StopsCache.get(stopId).orElseThrow(() -> new IllegalArgumentException("Invalid or unknown stop ID: " + stopId)),
                RouteDirection.fromString(direction), scheduledTime, actualArrivalTime);

        } catch (IllegalStateException e) {
            System.out.println("Failed to create HistoryEntry: " + e.getMessage());
        }
        return null;
    }

}
