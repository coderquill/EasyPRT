package edu.cmu.cs.prt;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A transit route, parsed from result of web API call.
 */
final class TrueTimeRoute {
    @JsonProperty("rt")
    private String routeId;

    @JsonProperty("rtnm")
    private String routeName;

    @JsonProperty("rtclr")
    private String routeColor;

    @JsonProperty("rtdd")
    private String routeDesignator;
    
    @JsonProperty("rtpidatafeed")
    private String rtpidatafeed;

    /**
     * Returns the alphanumeric designator of a route.
     * @return Route's alphabetic ID
     */
    public String routeId() {
        return routeId;
    }

    /**
     * Returns the common name of the route.
     * @return Route's common name
     */
    public String routeName() {
        return routeName;
    }

    /**
     * Returns the color of the route line used in map.
     * <p>
     * Color is represented as a string in RGB: {@code #ffffff}
     * @return Route's RGB color representation
     */
    String routeColor() {
        return routeColor;
    }

    /**
     * Returns the language-specific route designator, meant for display.
     * @return Route's language-specific route designator
     */
    String routeDesignator() {
        return routeDesignator;
    }

    String rtpidatafeed() {
        return rtpidatafeed;
    }

    /**
     * Two Routes are equal to each other if and only if they have the same
     * route ID.
     * 
     * @param anObject Object to compare this Route to
     * @return True if the input is equal to this Route, false otherwise
     */
    @Override
    public boolean equals(Object anObject) {
        try {
            TrueTimeRoute otherRoute = (TrueTimeRoute) anObject;
            if (routeId.equals(otherRoute.routeId())) {
                return true;
            }
            return false;
        } catch (ClassCastException e) {
            return false;
        }
    }

    /**
     * Returns the hash code of this Route.
     * 
     * @return Hash code
     */
    @Override
    public int hashCode() {
        return routeId.hashCode();
    }

    /**
     * Returns the string representation of a route in this format:
     * <pre>
     * Route ID:
     * Name:
     * Designator:
     * Color:
     * </pre>
     * 
     * @return Route's string representation
     */
    @Override
    public String toString() {
        StringBuilder str = new StringBuilder("Route ID: ");
        str.append(routeId);
        str.append("\n  Name: ");
        str.append(routeName);
        str.append("\n  Designator: ");
        str.append(routeDesignator);
        str.append("\n  Color: ");
        str.append(routeColor);
        // str.append("\n  rtpidatafeed: ");
        // str.append(rtpidatafeed);
        return str.toString();
    }
}
