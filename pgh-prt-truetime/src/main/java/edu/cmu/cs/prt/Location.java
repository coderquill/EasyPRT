package edu.cmu.cs.prt;

import org.apache.lucene.util.SloppyMath;

/**
 * Represents a geographical location in the world.
 * @param latitude the latitude of the location
 * @param longitude the longitude of the location
 */
public record Location(double latitude, double longitude) {
    private enum DistanceUnit {
        FEET(0.3048),
        MILES(1609.344),
        METERS(1),
        KILOMETERS(1000);

        private final double meters;

        DistanceUnit(double meters) {
            this.meters = meters;
        }

        double toMeters() {
            return meters;
        }
    }
    /**
     * Returns the distance in meters between this location and the specified location.
     * @param otherLocation the location to compute the distance from
     * @return the distance in meters between this location and the specified location
     */
    public double metersFrom(Location otherLocation) {
        return SloppyMath.haversinMeters(this.latitude, this.longitude, otherLocation.latitude, otherLocation.longitude);
    }

    /**
     * Returns the distance in feet between this location and the specified location.
     * @param otherLocation the location to compute the distance from
     * @return the distance in feet between this location and the specified location
     */
    public double feetFrom(Location otherLocation) {
        return metersFrom(otherLocation) / DistanceUnit.FEET.toMeters();
    }

    /**
     * Returns the distance in kilometers between this location and the specified location.
     * @param otherLocation the location to compute the distance from
     * @return the distance in kilometers between this location and the specified location
     */
    public double kilometersFrom(Location otherLocation) {
        return metersFrom(otherLocation) / DistanceUnit.KILOMETERS.toMeters();
    }

    /**
     * Returns the distance in miles between this location and the specified location.
     * @param otherLocation the location to compute the distance from
     * @return the distance in miles between this location and the specified location
     */
    public double milesFrom(Location otherLocation) {
        return metersFrom(otherLocation) / DistanceUnit.MILES.toMeters();
    }

    @Override
    public String toString() {
        return "(" + latitude + ", " + longitude + ")";
    }
}
