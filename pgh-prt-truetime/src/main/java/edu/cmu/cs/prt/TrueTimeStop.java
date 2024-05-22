package edu.cmu.cs.prt;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

final class TrueTimeStop {
    @JsonProperty("stpid")
    private String id;

    @JsonProperty("stpnm")
    private String name;

    @JsonProperty("lat")
    private double latitude;

    @JsonProperty("lon")
    private double longitude;

    @JsonProperty("dtradd")
    private List<Integer> detoursThruStop;

    @JsonProperty("dtrrem")
    private List<Integer> detoursAroundStop;

    @JsonProperty("gtfsseq")
    private int gtfsStopSequence;

    @JsonProperty("ada")
    private boolean isAdaAccessible;

    String id() {
        return id;
    }

    String name() {
        return name;
    }

    double latitude() {
        return latitude;
    }

    double longitude() {
        return longitude;
    }

    /**
     * Returns the string representation of a stop in this format:
     * <pre>
     * Stop ID:
     * Stop Name:
     * Position: ( Latitude: , Longitude: )
     * detoursThroughThisStop:
     * detoursGoingAroundThisStop:
     * gtfsStopSequence:
     * isAdaAccessible:
     * </pre>
     *
     * @return Stop's string representation
     */
    @Override
    public String toString() {
        StringBuilder str = new StringBuilder("\n Stop ID: ");
        str.append(id);
        str.append("\n  Stop Name: ");
        str.append(name);
        str.append("\n Position: ( Latitude: ");
        str.append(latitude);
        str.append(",  Longitude: ");
        str.append(longitude);
        str.append(" )");
        str.append("\n  Current Detours Going Through This Stop: ");
        str.append(detoursThruStop);
        str.append("\n  Current Detours Going Around This Stop: ");
        str.append(detoursAroundStop);
        str.append("\n GTFS Stop Sequence: ");
        str.append(gtfsStopSequence);
        str.append("\n Is ADA accessible: ");
        str.append(isAdaAccessible);

        return str.toString();
    }
}
