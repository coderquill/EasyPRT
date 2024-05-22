package edu.cmu.cs.prt;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Sample code for {@code PrtSchedule}
 */
class PrtScheduleClient {
    public static void main(String[] args) {
        Route route = PrtInfo.routeOf("61D").get();
        List<ScheduledTrip> trips = new PrtSchedule.Selector().forRoute(route)
                                                          .onDate(LocalDate.now())
                                                          .inDirection(RouteDirection.OUTBOUND)
                                                          .after(LocalTime.of(9, 0))
                                                          .before(LocalTime.of(10, 0))
                                                          .trips();
        for (ScheduledTrip trip : trips) {
            System.out.println(trip);
            System.out.println("\n");
        }

        Stop stop = PrtInfo.closestStop(new Location(40.443418243876266, -79.94288909575457));

        List<LocalTime> arrivals = PrtSchedule.arrivalTimesFor(trips, stop);

        for (LocalTime arrival : arrivals) {
            System.out.println(arrival);
        }

        System.out.println("_____________________________\n");
        System.out.println(stop);
        System.out.println("\n");
        List<ScheduledTrip> tripsFromStop = new PrtSchedule.Selector().atStop(stop)
                                                          .onDayOfWeek(DayOfWeek.SUNDAY)
                                                          .inDirection(RouteDirection.OUTBOUND)
                                                          .after(LocalTime.of(9, 0))
                                                          .before(LocalTime.of(10, 0))
                                                          .trips();
        for (ScheduledTrip trip : tripsFromStop) {
            System.out.println(trip);
            System.out.println("\n");
        }
    }
}