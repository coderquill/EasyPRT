package edu.cmu.cs.prt;

import com.fasterxml.jackson.annotation.JsonProperty;
class TrueTimeWaypoint {
    @JsonProperty("seq")
    private int seq;
    @JsonProperty("lat")
    private double lat;
    @JsonProperty("lon")
    private double lon;
    @JsonProperty("typ")
    private String typ;
    @JsonProperty("stpid")
    private String stpid;
    @JsonProperty("stpnm")
    private String stpnm;
    @JsonProperty("pdist")
    private double pdist;

    public int seq() {
        return seq;
    }

    public double lat() {
        return lat;
    }

    public double lon() {
        return lon;
    }

    public String typ() {
        return typ;
    }

    public String stpid() {
        return stpid;
    }

    public String stpnm() {
        return stpnm;
    }

    public double pdist() {
        return pdist;
    }
}
