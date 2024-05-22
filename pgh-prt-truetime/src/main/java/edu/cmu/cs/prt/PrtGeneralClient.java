package edu.cmu.cs.prt;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Sample client for {@code PrtInfo}. and {@code PrtRealTime}
 */
class PrtGeneralClient {
    public static void main(String[] args) {
        getPatterns();
        getPredictedArrivalsForClosestStop();
        getRoutesThruStop();
    }

    private static void getPatterns() {
        Optional<Route> route71B = PrtInfo.routeOf("71B");
        if (route71B.isEmpty()) {
            System.out.println("Route not found");
            return;
        }
        try {
            System.out.println(route71B.get().waypoints(RouteDirection.OUTBOUND));
            System.out.println(route71B.get().stopsInDirection(RouteDirection.OUTBOUND));
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static void getPredictedArrivalsForClosestStop() {
        Location myLocation = new Location(40.443418243876266, -79.94288909575457);
        Stop stop = PrtInfo.closestStop(myLocation);
        List<PredictedArrival> predictedArrivalList = null;
        try {
            predictedArrivalList = PrtRealTime.predictedArrivalsFor(stop, VehicleType.BUS);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        for (PredictedArrival predictedArrival : predictedArrivalList) {
            System.out.println(predictedArrival.route().id() + ": " + predictedArrival.time());
        }
    }

    private static void getPredictedArrivalsForStopAndRoute() {
        Optional<Route> route71B = PrtInfo.routeOf("71B");
        if (route71B.isEmpty()) {
            System.out.println("Route not found");
            return;
        }
        System.out.println(route71B.get());
        Location myLocation = new Location(40.447144, -79.942921);
        Stop stop = PrtInfo.closestStop(myLocation);
        System.out.println(stop);
        List<PredictedArrival> predictions;
        try {
            predictions = PrtRealTime.predictedArrivalsFor(stop, route71B.get());
            System.out.println(predictions);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (!predictions.isEmpty()) {
            PredictedArrival arrival = predictions.get(0);
            System.out.println(arrival.route());
            System.out.println(arrival.stop());
        }

        try {
            List<Stop> stops = PrtInfo.stopsFor(route71B.get(), RouteDirection.OUTBOUND);
            for (Stop newStop : stops) {
                System.out.println(newStop);
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static void getRoutesThruStop() {
        // Get the CMU bus stop (FORBES AVE + MOREWOOD AVE)
        Stop stop = PrtInfo.closestStop(new Location(40.443418243876266, -79.94288909575457));
        List<Route> routes = PrtInfo.routesThru(stop);
        for (Route route : routes) {
            System.out.println(route);
        }
    }
}
