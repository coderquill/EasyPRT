package edu.cmu.cs.prt;

/**
 * A measure of how crowded a transit vehicle is, derived from the ratio of the current passenger
 * count to the vehicle's total capacity.
 * <p>
 * The exact cutoffs are decided by PRT and are not known by this API.
 */
public enum PassengerLoad {
    /** The vehicle is empty */
    EMPTY,
    /** The vehicle is half empty */
    HALF_EMPTY,
    /** The vehicle is full */
    FULL,
    /** The passenger load of the vehicle is unknown */
    UNKNOWN
}
