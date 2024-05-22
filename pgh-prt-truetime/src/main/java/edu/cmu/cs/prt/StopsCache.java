package edu.cmu.cs.prt;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

final class StopsCache {
    static {
        init();
    }
    private StopsCache() {};

    /**
     * Mapping from stop identifiers to stop objects.
     */
    private static Map<String, Stop> stopIdToStop;

    /**
     * Populates the cache with content from local file 
     * {@code schedule-parsing/GTFS/stops.txt}.
     * Should only be called once at the start of a long-running program.
     */
    private static void init() {
        stopIdToStop = new HashMap<>();
        try {
            File file = new File("schedule-parsing/GTFS/stops.txt");
            Scanner sc = new Scanner(file);

            boolean ignoreHeadings = true;
            while (sc.hasNextLine()) {
                if (ignoreHeadings) { // Ignore the column headers (i.e. 1st row)
                    sc.nextLine();
                    ignoreHeadings = false;
                } else {
                    String str = sc.nextLine();
                    List<String> strList = Arrays.asList(str.split(","));
                    String stopId = strList.get(1);
                    String stopName = strList.get(2);
                    try { 
                        double latitude = Double.parseDouble(strList.get(4));
                        double longitude = Double.parseDouble(strList.get(5));
                        Stop stop = Stop.createFrom(stopId, stopName, new Location(latitude, longitude));
                        stopIdToStop.put(stopId, stop);
                    } catch (NumberFormatException e) {
                        System.out.printf("Parse error for stop_id %s, %s%n", stopId, stopName);
                    }
                }
            }
            sc.close();
        } 
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get all the stops stored in the cache.
     * @return all the stops stored in the cache
     */
    static List<Stop> allStops() {
        return new ArrayList<>(stopIdToStop.values());
    }

    /**
     * Checks whether the key exists in cache.
     * @param routeId
     * @return
     */
    static boolean containsKey(String stopId) {
        return stopIdToStop.containsKey(stopId);
    }

    /**
     * Returns the value to which the specified key is mapped, or null if this
     * cache contains no mapping for the key.
     * @param stopId
     * @return
     */
    static Optional<Stop> get(String stopId) {
        if (!stopIdToStop.containsKey(stopId)) {
            return Optional.empty();
        }
        return Optional.of(stopIdToStop.get(stopId));
    }

    /**
     * Add input stop to cache.
     * If cache already contains stop, call will be ignored.
     * @param stop
     * @return True if successful; false otherwise
     */
    static boolean put(Stop stop) {
        if (stopIdToStop.containsKey(stop.id())) {
            // Stop already exists, ignore
            return false;
        }
        stopIdToStop.put(stop.id(), stop);
        return true;
    }
}
