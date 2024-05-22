package edu.cmu.cs.prt;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A prediction provides information about the expected arrival of a vehicle at a stop.
 */
final class TrueTimePrediction {
    @JsonProperty("tmstmp")
    private String creationTime;

    @JsonProperty("typ")
    private String type;

    @JsonProperty("stpnm")
    private String stopName;

    @JsonProperty("stpid")
    private String stopId;

    @JsonProperty("vid")
    private String vehicleId;

    @JsonProperty("dstp")
    private int feetToStop;

    @JsonProperty("rt")
    private String route;

    @JsonProperty("rtdd")
    private String rtdd;

    @JsonProperty("rtdir")
    private String routeDirection;

    @JsonProperty("des")
    private String destination;

    @JsonProperty("prdtm")
    private String predictedDateTime;

    @JsonProperty("tablockid")
    private String taBlockId;

    @JsonProperty("tatripid")
    private String taTripId;

    @JsonProperty("origtatripno")
    private String origTaTripNum;

    @JsonProperty("dly")
    private boolean delayed;

    @JsonProperty("dyn")
    private int dyn;

    /**
     * Note: This parameter is just populated as "DUE" for PRT.
     */
    @JsonProperty("prdctdn")
    private String minutesUntilArrival;

    @JsonProperty("zone")
    private String zone;

    @JsonProperty("psgld")
    private String passengerLoad;

    @JsonProperty("stst")
    private int scheduledTripStartTime;

    @JsonProperty("stsd")
    private String scheduledTripStartDate;

    @JsonProperty("flagstop")
    private int flagStop;

    String creationDateTime() {
        return creationTime;
    }

    String predictedDateTime() {
        return predictedDateTime;
    }

    /**
     * Get the name of the stop that this prediction corresponds to.
     * @return name of the stop that this prediction corresponds to.
     */
    String stopName() {
        return stopName;
    }

    /**
     * Return the identifier of the stop that this prediction corresponds to.
     * @return the identifier of the stop that this prediction corresponds to.
     */
    String stopId() {
        return stopId;
    }

    /**
     * Returns the identifier of the vehicle that this prediction corresponds to.
     * @return the identifier of the vehicle that this prediction corresponds to.
     */
    String vehicleId() {
        return vehicleId;
    }

    /**
     * Returns the distance to the stop in feet.
     * @return distance to the stop in feet.
     */
    double feedToStop() {
        return feetToStop;
    }

    /**
     * Returns the route that the vehicle this prediction is for is running on.
     * @return the route that the vehicle this prediction is for is running on.
     */
    String route() {
        return route;
    }

    /**
     * Returns the direction of the route that the vehicle this prediction is for is running on.
     * @return the direction of the route that the vehicle this prediction is for is running on.
     */
    String routeDirection() {
        return routeDirection;
    }

    String predictionType() {
        return type;
    }

    String rtdd() {
        return rtdd;
    }

    String destination() {
        return destination;
    }

    String taBlockId() {
        return taBlockId;
    }

    String taTripId() {
        return taTripId;
    }

    String origTaTripNum() {
        return origTaTripNum;
    }

    boolean isDelayed() {
        return delayed;
    }

    int dyn() {
        return dyn;
    }

    String minutesUntilArrival() {
        return minutesUntilArrival;
    }

    String zone() {
        return zone;
    }

    String passengerLoad() {
        return passengerLoad;
    }

    int scheduledTripStartTime() {
        return scheduledTripStartTime;
    }

    String scheduledTripStartDate() {
        return scheduledTripStartDate;
    }

    int flagStop() {
        return flagStop;
    }

    @Override
    public String toString() {
        return "Creation time: " + creationTime +
                "\nPredicted time: " +
                predictedDateTime +
                "\nRoute: " +
                route +
                "\nDirection: " +
                routeDirection +
                "\nStop Name: " +
                stopName;
    }
}
