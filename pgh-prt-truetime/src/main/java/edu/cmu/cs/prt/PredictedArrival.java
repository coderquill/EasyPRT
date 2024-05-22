package edu.cmu.cs.prt;

import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;

/**
 * Represents the predicted arrival of a vehicle at a stop. The actual arrival time of a vehicle at
 * the stop may be different that the predicted arrival time. This class is immutable and does not update the prediction
 * once created. If a more up-to-date arrival prediction is required, a new instance must be obtained.
 */
public final class PredictedArrival implements Arrival {
    /**
     * The time of creation of the prediction.
     */
    private final LocalDateTime creationTime;

    /**
     * The stop this prediction is associated with.
     */
    private final Stop stop;

    /**
     * The identifier of the vehicle this prediction is associated with.
     */
    private final String vehicleId;

    /**
     * The distance between the current location of the vehicle and the stop that this prediction is for.
     */
    private final double feetToStop;

    /**
     * The route that the vehicle this prediction is for is operating on.
     */
    private final Route route;

    /**
     * The direction of the route this prediction is associated with.
     */
    private final RouteDirection routeDirection;

    /**
     * The predicted arrival of the vehicle at the stop.
     */
    private final LocalDateTime time;

    /**
     * The scheduled start time of the trip that the vehicle associated with this prediction is operating on.
     */
    private final LocalDateTime tripScheduledStart;

    /**
     * Create a Prediction object from a TrueTimePrediction object.
     * @param prediction the TrueTimePrediction object to use
     * @return the Prediction object
     *
     * @throws IllegalArgumentException if the route direction cannot be parsed
     * @throws IllegalArgumentException if the stop cannot be found
     * @throws DateTimeParseException if a timestamp cannot be parsed
     */
    static PredictedArrival of(TrueTimePrediction prediction) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd HH:mm");
        LocalDateTime creationTime = LocalDateTime.parse(prediction.creationDateTime(), formatter);
        LocalDateTime predictionTime = LocalDateTime.parse(prediction.predictedDateTime(), formatter);

        RouteDirection direction = RouteDirection.valueOf(prediction.routeDirection());

        formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate tripScheduledStartDate = LocalDate.parse(prediction.scheduledTripStartDate(), formatter);
        LocalDateTime tripScheduledStart =
                tripScheduledStartDate.atStartOfDay().plusSeconds(prediction.scheduledTripStartTime());

        Optional<Stop> stop = Stop.of(prediction.stopId());
        if (stop.isEmpty()) {
            throw new IllegalArgumentException("Stop with id " + prediction.stopId() + " not found");
        }

        Optional<Route> route = PrtInfo.routeOf(prediction.route());
        if (route.isEmpty()) {
            throw new IllegalArgumentException("Route with id " + prediction.route() + " not found");
        }

        return new PredictedArrival(
                creationTime,
                stop.get(),
                prediction.vehicleId(),
                prediction.feedToStop(),
                route.get(),
                direction,
                predictionTime,
                tripScheduledStart);
    }

    private PredictedArrival(LocalDateTime creationDateTime,
                             Stop stop,
                             String vehicleId,
                             double feetToStop,
                             Route route,
                             RouteDirection routeDirection,
                             LocalDateTime time,
                             LocalDateTime tripScheduledStart) {
        this.creationTime = creationDateTime;
        this.stop = stop;
        this.vehicleId = vehicleId;
        this.feetToStop = feetToStop;
        this.route = route;
        this.routeDirection = routeDirection;
        this.time = time;
        this.tripScheduledStart = tripScheduledStart;
    }

    /**
     * Returns the time this prediction was created.
     * @return the time this prediction was created
     */
    public LocalDateTime creationTime() {
        return creationTime;
    }

    /**
     * Returns the stop associated with this prediction.
     * @return the stop associated with this prediction
     */
    public Stop stop() {
        return stop;
    }

    /**
     * Returns the distance in feet to the stop that the prediction is for.
     * @return the distance in feet to the stop that the prediction is for
     */
    public double feetToStop() {
        return feetToStop;
    }

    /**
     * Returns the predicted time of arrival or departure at the stop this prediction is for.
     * @return the predicted time of arrival or departure at the stop this prediction is for
     */
    public LocalTime time() {
        return time.toLocalTime();
    }

    /**
     * Returns the route that this prediction is for.
     * @return the route that this prediction is for
     */
    public Route route() {
        return route;
    }

    /**
     * Returns the direction of the route that this prediction is for.
     * @return the direction of the route that this prediction is for
     */
    public RouteDirection routeDirection() {
        return routeDirection;
    }

    /**
     * Returns the vehicle that this prediction is for. This method retrieves the current state of the vehicle from the
     * TrueTime web API.
     * @return the vehicle that this prediction is for
     *
     * @throws IOException if there was an error sending the HTTP request
     * @throws InterruptedException if sending the HTTP request was interrupted
     */
    public Vehicle vehicle() throws IOException, InterruptedException {
        try {
            return Vehicle.of(vehicleId);
        } catch (NoDataFoundException e) {
            throw new RuntimeException("Vehicle with id " + vehicleId + " not found");
        }
    }

    /**
     * Returns a string representation of this predicted arrival.
     * @return a string representation of this predicted arrival
     */
    @Override
    public String toString() {
        return "Creation time: " + creationTime +
                "\nPredicted arrival time at " + stop.name() + ": " + time +
                "\nRoute: " + route +  " (" + routeDirection + ")";
    }
}
