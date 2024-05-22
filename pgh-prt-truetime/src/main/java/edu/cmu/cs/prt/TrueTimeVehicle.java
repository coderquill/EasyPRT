package edu.cmu.cs.prt;

import com.fasterxml.jackson.annotation.JsonProperty;

final class TrueTimeVehicle {
    @JsonProperty("vid")
    String vehicleId;

    @JsonProperty("rtpidatafeed")
    String rtpiDataFeed;

    @JsonProperty("tmstmp")
    String snapshotTimestamp;

    @JsonProperty("lat")
    String latitude;

    @JsonProperty("lon")
    String longitude;

    @JsonProperty("hdg")
    String heading;

    @JsonProperty("pid")
    int tripPatternId;

    @JsonProperty("rt")
    String route;

    @JsonProperty("des")
    String destination;

    @JsonProperty("pdist")
    int feetTraveledInPattern;

    @JsonProperty("dly")
    boolean isDelayed;

    @JsonProperty("spd")
    int speedMph;

    @JsonProperty("tatripid")
    String taTripId;

    @JsonProperty("origtatripno")
    String origTaTripNum;

    @JsonProperty("tablockid")
    String taBlockId;

    @JsonProperty("zone")
    String zone;

    @JsonProperty("mode")
    int mode;

    @JsonProperty("psgld")
    String passengerLoad;

    @JsonProperty("stst")
    int tripScheduledStartTime;

    @JsonProperty("stsd")
    String tripScheduledStartDate;

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder("Vehicle ID: ");
        str.append(vehicleId);
        str.append("\n RTPI Data Feed: ");
        str.append(rtpiDataFeed);
        str.append("\n Route: ");
        str.append(route);
        str.append("\n Destination: ");
        str.append(destination);
        str.append("\n Last location update: ");
        str.append(snapshotTimestamp);
        str.append("\n Latitude: ");
        str.append(latitude);
        str.append("\n Longitude: ");
        str.append(longitude);
        str.append("\n Speed (mph): ");
        str.append(speedMph);
        str.append("\n Heading: ");
        str.append(heading);
        str.append("\n Delayed: ");
        str.append(isDelayed);
        str.append("\n Passenger load: ");
        str.append(passengerLoad);
        str.append("\n Trip scheduled start date: ");
        str.append(tripScheduledStartDate);
        str.append("\n Trip scheduled start time: ");
        str.append(tripScheduledStartTime);
        return str.toString();
    }
}
