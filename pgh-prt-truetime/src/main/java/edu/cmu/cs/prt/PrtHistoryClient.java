package edu.cmu.cs.prt;


import java.time.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

/**
 * Sample client for {@code PrtHistoryTable}.
 */
class PrtHistoryClient {

    public static void main(String[] args) {
        // Get a full history table
        PrtHistoryTable historyTable = PrtHistoryTable.fullTable();
        List<HistoryEntry> entry0 = historyTable.entryList();
        System.out.println(entry0.get(0));

        // Print the creation time of the history table
        LocalDateTime creationTime = historyTable.createdAt();
        System.out.println("History Table created at: " + creationTime);

        historyTable.afterDateTime(LocalDateTime.of(2023, 12, 1, 0, 0));

        // Print the number of entries
        long numEntries = historyTable.numberOfEntries();
        System.out.println("Number of entries: " + numEntries);

        // Get HistoryTable containing entries for a specific date
        LocalDate today = LocalDateTime.now().toLocalDate(); // Example date: 30 days ago
        PrtHistoryTable todaysEntries = historyTable.onDate(today);
        System.out.println(todaysEntries.entryList().size() + " trips recorded today");

        // Apply a filter based on a specific date
        LocalDateTime specificDate = LocalDateTime.now().minusDays(30); // Example date: 30 days ago
        PrtHistoryTable filteredTable = historyTable.afterDateTime(specificDate);

        // Get the average delay from the filtered table
        Duration averageDelay = filteredTable.averageDelay();
        System.out.println("Average delay: " + averageDelay.toMinutes() + " minutes");

        Comparator<HistoryEntry> arrivalDeviationComparator = (entry1, entry2) ->
                Long.compare(entry2.arrivalDeviation().toMinutes(),
                        entry1.arrivalDeviation().toMinutes());


        // Get the top 3 delayed vehicles for a route
        List<Route> routes = new ArrayList<>(); // Assume we have a list of routes

        Map<Route, List<HistoryEntry>> routeToDelayedEntriesMap = new HashMap<>();

        routes.forEach(route -> {
            List<HistoryEntry> top3DelayedEntries = historyTable
                    .ofRoute(route)
                    .sortBy(arrivalDeviationComparator)
                    .topNEntries(3);

            routeToDelayedEntriesMap.put(route, top3DelayedEntries);
        });


        // FORBES AVE + MOREWOOD (CARNEGIE MELLON)
        Stop stop = PrtInfo.closestStop(new Location(40.444458, -79.942290999999));
        Route route = PrtInfo.routeOf("61C").get();

        // Check if 61C is late for the stop on average on Friday
        Duration averageDelayOnFriday = historyTable
                .ofRoute(route)
                .ofStop(stop)
                .onDayOfWeek(DayOfWeek.FRIDAY)
                .averageDelay();
        System.out.println("On average, for stop 7117, 61C is "+ averageDelayOnFriday.toMinutes() + " minutes late on Friday");

        // Check how much 61C deviates from schedule, for the stop between 4 pm to 5 pm, on average
        Duration averageDelayBetweenTime = historyTable
                .ofRoute(route)
                .ofStop(stop)
                .afterTime(LocalTime.of(8, 0, 0))
                .beforeTime(LocalTime.of(13, 0, 0))
                .averageDelay();
        System.out.println("On average, for stop 7117, 61C deviates "+ averageDelayBetweenTime.toMinutes() + " minutes on average");
    }

}



