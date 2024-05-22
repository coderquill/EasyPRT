package edu.cmu.cs.prt;

/**
 * Represents the different directions that vehicles operate on a route.
 */
public enum RouteDirection {
    /** Inbound direction */
    INBOUND(1),
    /** Outbound direction */
    OUTBOUND(0);

    /**
     * The GTFS integer representation of this direction.
     */
    private final int direction;

    /**
     * Matches the input direction with a member of this enum.
     * @param direction the direction for the route
     * @return an enum representing the route direction
     *
     * @throws IllegalArgumentException if the given direction is invalid
     */
    static RouteDirection convert(int direction) {
        return switch (direction) {
            case 1 -> INBOUND;
            case 0 -> OUTBOUND;
            default -> throw new IllegalArgumentException("Invalid direction " + direction);
        };
    }

    /**
     * Construct a route direction with the given GTFS direction representation.
     * @param direction the GTFS route direction to associate with the enum
     */
    RouteDirection(int direction) {
        this.direction = direction;
    }

    /**
     * Get the integer representation of the direction corresponding to this enum.
     * @return the integer representation of the direction value corresponding to this enum
     */
    int direction() {
        return direction;
    }

    /**
     * Matches the input string with a member of this enum.
     * @param directionString The direction as a string.
     * @return The corresponding RouteDirection enum value.
     * @throws IllegalArgumentException If the input string is null or any value other than "INBOUND" or "OUTBOUND".
     */
    public static RouteDirection fromString(String directionString) {
        if (directionString == null) {
            throw new IllegalArgumentException("Direction cannot be null");
        }

        switch (directionString.toUpperCase()) {
            case "INBOUND":
                return INBOUND;
            case "OUTBOUND":
                return OUTBOUND;
            default:
                throw new IllegalArgumentException("Invalid direction: " + directionString);
        }
    }
}