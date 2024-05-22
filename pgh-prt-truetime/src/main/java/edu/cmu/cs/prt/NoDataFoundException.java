package edu.cmu.cs.prt;

/**
 * A {@code NoDataFoundException} is thrown when no data is found for the given parameters.
 */
class NoDataFoundException extends Exception {
    NoDataFoundException(String errorMessage) {
        super(errorMessage);
    }
}
