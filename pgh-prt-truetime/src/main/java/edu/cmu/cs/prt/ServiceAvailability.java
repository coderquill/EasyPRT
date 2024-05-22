package edu.cmu.cs.prt;

import java.time.LocalDate;

/**
 * Represents the service availability of a {@link ScheduledTrip}.
 */
enum ServiceAvailability {
    SUNDAY("1"),
    WEEKDAYS("2"),
    SATURDAY("3"),
    NONE("4"),
    SUNDAY_ALPHA("S"),
    WEEKDAYS_ALPHA("W"),
    SATURDAY_ALPHA("U"),
    UNKNOWN("X");

    /**
     * Matches the input {@code serviceId} with a member of this {@code enum}.
     * @param serviceId 
     * @return
     */
    static ServiceAvailability fromServiceId(String serviceId) {
        switch (serviceId) {
            case "1":
                return SUNDAY;
            case "2":
                return WEEKDAYS;
            case "3":
                return SATURDAY;
            case "4":
                return NONE;
            case "S":
                return SUNDAY_ALPHA;
            case "W":
                return WEEKDAYS_ALPHA;
            case "U":
                return SATURDAY_ALPHA;
            default:
                return UNKNOWN;
        }
    }

    private final String serviceId;

    private final LocalDate startDate = LocalDate.of(2023, 10, 01);

    private final LocalDate expirationDate = LocalDate.of(2024, 02, 17);

    private ServiceAvailability(String serviceId) {
        this.serviceId = serviceId;
    }

    String serviceId() {
        return serviceId;
    }

    LocalDate startDate() {
        return startDate;
    }

    LocalDate expirationDate() {
        return expirationDate;
    }

    boolean availableWeekdays() {
        return (serviceId.equals("2") || serviceId.equals("W"));
    }

    boolean availableSunday() {
        return (serviceId.equals("1") || serviceId.equals("S"));
    }

    boolean availableSaturday() {
        return (serviceId.equals("3") || serviceId.equals("U"));
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder("Available on ");
        if (availableWeekdays()) {
            str.append("Weekdays ");
        }
        if (availableSaturday()) {
            str.append("Saturdays ");
        }
        if (availableSunday()) {
            str.append("Sundays ");
        }
        str.append("\nSchedule valid from ");
        str.append(startDate);
        str.append(" to ");
        str.append(expirationDate);
        return str.toString();
    }
}
