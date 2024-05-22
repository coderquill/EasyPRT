package edu.cmu.cs.prt;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;

final class RoutesCache {
    static {
        init();
    }
    private RoutesCache() {};

    /**
     * Mapping from route identifiers to the corresponding Route object.
     */
    private static Map<String, Route> routeIdToRoute;

    /**
     * Populates the cache with content from local file {@code schedule-parsing/GTFS/routes.txt}. Should only be called
     * once at the start of a long-running program.
     */
    private static void init() {
        routeIdToRoute = new HashMap<>();
        try {
            File file = new File("schedule-parsing/GTFS/routes.txt");
            Scanner sc = new Scanner(file);

            boolean ignoreHeadings = true;
            while (sc.hasNextLine()) {
                if (ignoreHeadings) { // Ignore the column headers (i.e. 1st row)
                    sc.nextLine();
                    ignoreHeadings = false;
                } else {
                    String str = sc.nextLine();
                    List<String> strList = Arrays.asList(str.split(","));
                    String routeId = strList.get(0);
                    String routeName = strList.get(3);
                    String routeType = strList.get(5);
                    if (routeType.equals("7")) {
                        // Skip incline routes
                        continue;
                    }
                    Route route = Route.createFrom(routeId, routeName, VehicleType.convert(routeType));
                    routeIdToRoute.put(routeId, route);
                }
            }
            sc.close();
        } 
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks whether a route with the given route identifier exists in the cache.
     * @param routeId the route identifier to use for checking
     * @return true if a route with the given route designator exists in the cache, false otherwise
     */
    static boolean containsKey(String routeId) {
        return routeIdToRoute.containsKey(routeId);
    }

    /**
     * Returns the route associated with the specified route identifier, if one exists in the cache.
     * @param routeId the route identifier to use to find the route
     * @return the route associated with the specified route identifier
     */
    static Optional<Route> get(String routeId) {
        if (!routeIdToRoute.containsKey(routeId)) {
            return Optional.empty();
        }
        return Optional.of(routeIdToRoute.get(routeId));
    }

    /**
     * Add the specified route to the cache. If the cache already contains the specified route, this is a no-op.
     * @param route the route to be added to the cache
     * @return true if successful, false otherwise
     */
    static boolean put(Route route) {
        if (routeIdToRoute.containsKey(route.id())) {
            // Route already exists, ignore
            return false;
        }
        routeIdToRoute.put(route.id(), route);
        return true;
    }

    /**
     * Get all routes stored in the cache.
     * @return all the routes stored in the cache
     */
    static List<Route> allRoutes() {
        return new ArrayList<Route>(routeIdToRoute.values());
    }
}
