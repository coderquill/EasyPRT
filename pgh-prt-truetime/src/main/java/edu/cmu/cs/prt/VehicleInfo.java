package edu.cmu.cs.prt;

import java.time.LocalDateTime;

/**
 * Real-time information about a vehicle. An instance of this record can be obtained from a {@link Vehicle} instance
 * using {@link Vehicle#info()}.
 *
 * @param destination the destination of the vehicle
 * @param snapshotTimestamp the information about this vehicle is up-to-date as of this time
 * @param location the location of the vehicle
 * @param speedMph the speed of the vehicle in miles per hour
 * @param heading the heading of the vehicle as a 360° value, with North at 0° and East at 90°
 * @param feetTraveled the distance in feet that the vehicle has traveled in the current route
 * @param isDelayed whether the vehicle is delayed
 * @param passengerLoad the passenger load of the vehicle
 * @param route the route that the vehicle is operating on
 * @param tripScheduledStart the scheduled start time of the trip the vehicle is operating on
 *
 * @see Vehicle
 */
public record VehicleInfo(String destination,
                          LocalDateTime snapshotTimestamp,
                          Location location,
                          double speedMph,
                          double heading,
                          double feetTraveled,
                          boolean isDelayed,
                          PassengerLoad passengerLoad,
                          Route route,
                          LocalDateTime tripScheduledStart) {
}
