package edu.cmu.cs.prt;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Represents public transit vehicle.
 * Vehicles periodically update their information like location, speed, heading, etc. at an
 * unspecified interval. The information returned by the methods in this class is
 * up-to-date as of {@link VehicleInfo#snapshotTimestamp()}, as obtained from {@link Vehicle#info()}.
 * <p>
 * This class is thread-safe.
 */
public final class Vehicle {
    /**
     * The identifier of the vehicle.
     */
    private final String id;
    /**
     * The type of the vehicle.
     */
    private final VehicleType vehicleType;
    /**
     * The last time information about this vehicle was retrieved from the web API.
     */
    private Instant lastRefresh = Instant.MIN;

    /**
     * The real-time information about this vehicle. This is stored as a single object so that information about the
     * vehicle can be obtained in a consistent manner. Otherwise, calling getters for different information fields might
     * give the client inconsistent state.
     */
    private VehicleInfo info;

    /**
     * The pattern ID of the trip currently being executed by this vehicle.
     */
    private int tripPatternId;

    /**
     * The period at which we refresh information about this vehicle.
     */
    private static final int refreshMinutes = 1;

    /**
     * The lock used to synchronize the information about this vehicle.
     */
    private final Lock lock = new ReentrantLock();

    /**
     * Returns real-time information about this vehicle. The granularity of the real-time data is unspecified. This
     * method may block in order to obtain the latest information about this vehicle.
     * @return real-time information about this vehicle
     *
     * @throws IOException if obtaining the information failed
     * @throws InterruptedException if obtaining the information was interrupted
     */
    public synchronized VehicleInfo info() throws IOException, InterruptedException {
        maybeRefresh();
        return info;
    }

    /**
     * Convert a TrueTimeVehicle object to a Vehicle object.
     * @param vehicle the TrueTimeVehicle object to convert from
     * @return the Vehicle object converted from the given TrueTimeVehicle object
     *
     * @throws NullPointerException if a date string is empty
     * @throws NumberFormatException if unable to parse a double from a string
     * @throws java.time.format.DateTimeParseException if unable to parse a date
     */
    static Vehicle of(TrueTimeVehicle vehicle) {
        VehicleType parsedVehicleType = null;
        if (vehicle.rtpiDataFeed.equals(VehicleType.BUS.getFeedName())) {
            parsedVehicleType = VehicleType.BUS;
        } else if (vehicle.rtpiDataFeed.equals(VehicleType.LIGHT_RAIL.getFeedName())) {
            parsedVehicleType = VehicleType.LIGHT_RAIL;
        }
        LocalDateTime parsedSnapshotTimestamp;
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd HH:mm");
        parsedSnapshotTimestamp = LocalDateTime.parse(vehicle.snapshotTimestamp, dateTimeFormatter);

        double parsedLatitude = Double.parseDouble(vehicle.latitude);
        double parsedLongitude = Double.parseDouble(vehicle.longitude);
        Location location = new Location(parsedLatitude, parsedLongitude);

        double parsedHeading = Double.parseDouble(vehicle.heading);

        PassengerLoad parsedPassengerLoad =
                switch (vehicle.passengerLoad) {
                    case "EMPTY" -> PassengerLoad.EMPTY;
                    case "HALF_EMPTY" -> PassengerLoad.HALF_EMPTY;
                    case "FULL" -> PassengerLoad.FULL;
                    default -> PassengerLoad.UNKNOWN;
                };

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate scheduledStartDate = LocalDate.parse(vehicle.tripScheduledStartDate, dateFormatter);
        int secondsPastMidnight = vehicle.tripScheduledStartTime;
        LocalDateTime parsedTripScheduledStart = scheduledStartDate.atStartOfDay().plusSeconds(secondsPastMidnight);

        Optional<Route> route = RoutesCache.get(vehicle.route);
        if (route.isEmpty()) {
            throw new IllegalArgumentException("Route with id " + vehicle.route + " not found");
        }

        return new Vehicle(
                vehicle.vehicleId,
                parsedVehicleType,
                parsedSnapshotTimestamp,
                location,
                parsedHeading,
                vehicle.tripPatternId,
                route.get(),
                vehicle.destination,
                vehicle.feetTraveledInPattern,
                parsedPassengerLoad,
                parsedTripScheduledStart,
                vehicle.speedMph,
                vehicle.isDelayed);
    }

    private Vehicle(String id,
                    VehicleType vehicleType,
                    LocalDateTime snapshotTimestamp,
                    Location location,
                    double heading,
                    int tripPatternId,
                    Route route,
                    String destination,
                    int feetTraveledInPattern,
                    PassengerLoad passengerLoad,
                    LocalDateTime tripScheduledStart,
                    double speedMph,
                    boolean isDelayed) {
        this.id = id;
        this.vehicleType = vehicleType;
        this.tripPatternId = tripPatternId;
        this.info = new VehicleInfo(
                destination,
                snapshotTimestamp,
                location,
                speedMph,
                heading,
                feetTraveledInPattern,
                isDelayed,
                passengerLoad,
                route,
                tripScheduledStart
        );
    }

    /**
     * Get the vehicle with the given identifier.
     * @param vehicleId the identifier of the vehicle to get
     * @return the vehicle with the given identifier
     * @throws IOException if the HTTP request to the web API fails
     * @throws InterruptedException if the HTTP request to the web API is interrupted
     * @throws NoDataFoundException if a vehicle with this identifier is not found
     */
    static Vehicle of(String vehicleId) throws IOException, InterruptedException, NoDataFoundException {
        List<Vehicle> vehicleList = null;
        try {
            vehicleList = PrtRealTime.getClient().getVehiclesWithVehicleIds(Set.of(vehicleId));
        } catch (NoDataFoundException e) {
            vehicleList = new ArrayList<Vehicle>();
        }
        
        if (vehicleList.isEmpty()) {
            throw new NoDataFoundException("Vehicle with id " + vehicleId + " not found");
        }
        return vehicleList.get(0);
    }

    /**
     * Refresh information about this vehicle from the TrueTime web API if sufficient time has passed since the last
     * refresh.
     * @throws IOException if HTTP request failed
     * @throws InterruptedException if HTTP request was interrupted
     */
    private void maybeRefresh() throws IOException, InterruptedException {
        if (!lock.tryLock()) {
            return;
        }
        try {
            if (ChronoUnit.MINUTES.between(lastRefresh, Instant.now()) < refreshMinutes) {
                return;
            }
            TrueTimeWebApiClient client = PrtRealTime.getClient();
            List<Vehicle> vehicleList = null;
            try {
                vehicleList = client.getVehiclesWithVehicleIds(Set.of(id));
            } catch (NoDataFoundException e) {
                return;
            }
            if (vehicleList.isEmpty()) {
                return;
            }
            Vehicle vehicle = vehicleList.get(0);
            synchronized(this) {
                this.info = vehicle.info;
                this.tripPatternId = vehicle.tripPatternId;
            }
            lastRefresh = Instant.now();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Returns the identifier for this vehicle.
     * @return identifier for this vehicle
     */
    String id() {
        return id;
    }

    /**
     * Returns the type of this vehicle.
     * @return type of this vehicle
     */
    public VehicleType type() {
        return vehicleType;
    }

    synchronized int tripPatternId() throws IOException, InterruptedException {
        maybeRefresh();
        return tripPatternId;
    }

    /**
     * Returns a string representation of the vehicle.
     * @return a string representation of the vehicle
     */
    @Override
    public String toString() {
        return vehicleType.getFeedName() +
                " operating on route " + info.route() +
                " (snapshot at " + info.snapshotTimestamp() + ")" +
                "\nLocation: " + info.location() +
                ", heading: " + info.heading() + " degrees" +
                "\nDestination: " + info.destination() +
                "\nSpeed: " + info.speedMph() + "mph" +
                ", passenger load: " + info.passengerLoad() +
                "\nDelayed: " + info.isDelayed() +
                ", distance traveled: " + info.feetTraveled() + "ft";
    }

    /**
     * Compares the specified object for equality with this vehicle.
     * @param o object to be compared for equality with this vehicle
     * @return true if the specified object is equal to this vehicle
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vehicle vehicle = (Vehicle) o;
        return Objects.equals(id, vehicle.id);
    }

    /**
     * Returns the hash code value for this vehicle.
     * @return the hash code value for this vehicle
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}