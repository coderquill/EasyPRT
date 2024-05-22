package edu.cmu.cs.prt;

/**
 * Represents a type of public transit vehicle.
 */
public enum VehicleType {
    /**
     * Bus
     */
    BUS("Port Authority Bus"),
    /**
     * Light rail
     */
    LIGHT_RAIL("Light Rail");

    /**
     * The name of the vehicle type that is recognized by the TrueTime web API and GTFS.
     */
    private final String description;

    /**
     * Convert from GTFS or TrueTime web API vehicle type representation to VehicleType.
     * @param vehicleType the GTFS or TrueTime web API vehicle type representation
     * @return a VehicleType object corresponding to the specified vehicle type
     *
     * @throws IllegalArgumentException if the vehicle type specified is invalid
     */
    static VehicleType convert(String vehicleType) {
        if (vehicleType.equals("3") || vehicleType.equals("Port Authority Bus")) {
            return BUS;
        }
        if (vehicleType.equals("0") || vehicleType.equals("Light Rail")) {
            return LIGHT_RAIL;
        }
        throw new IllegalArgumentException("Invalid vehicle type: " + vehicleType);
    }

    /**
     * Construct a VehicleType object with the given description.
     * @param description the description to use
     */
    VehicleType(String description) {
        this.description = description;
    }

    /**
     * @return rtpidatafeed name
     */
    String getFeedName() {
        return description;
    }
}


