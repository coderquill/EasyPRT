import numpy as np

def readStops():
    # Code from https://numpy.org/doc/stable/reference/generated/numpy.loadtxt.html
    stops = np.loadtxt("GTFS/stops.txt", dtype='S100', delimiter=",", skiprows=1, usecols=[0, 2, 4, 5], comments=None)
    # 0th col: stop_id, 2nd col: stop_name, 4th col: latitude, 5th col: longitude
    stopsDict = dict()
    for stop in stops:
        stopId = stop[0]
        if stopId not in stopsDict:
            stopsDict[stopId] = stop
    return stops, stopsDict

def readStopTimes():
    # Code from https://numpy.org/doc/stable/reference/generated/numpy.loadtxt.html
    result = np.loadtxt("GTFS/stop_times.txt", dtype='S100', delimiter=",", skiprows=1, usecols=[0, 1, 2, 3], comments=None)
    # 0th col: trip_id, 1st col: arrival_time, 2nd col: departure_time, 3rd col: stop_id
    return result

def readTrips():
    # Code from https://numpy.org/doc/stable/reference/generated/numpy.loadtxt.html
    trips = np.loadtxt("GTFS/trips.txt", dtype='S100', delimiter=",", skiprows=1, usecols=[0, 1, 2, 5], comments=None)
    # 0th col: trip_id, 1st col: route_id, 2nd col: service_id, 5th col: direction_id (1=INBOUND, 0=OUTBOUND)
    tripsDict = dict()
    for trip in trips:
        tripId = trip[0]
        if tripId not in tripsDict:
            tripsDict[tripId] = trip
    return trips, tripsDict

def mergeStopTimesAndTrips():
    stopsTimes = readStopTimes()
    trips, tripsDict = readTrips()
    stops, stopsDict = readStops()

    # Insert 3 columns of 0's for trip info at column 1
    num_trip_cols = 3
    zeros = np.zeros(stopsTimes.shape[0])
    for i in range(num_trip_cols):
        stopsTimes = np.insert(stopsTimes, 1, zeros, axis=1)

    # Append 3 columns of 0's for stop info to the end
    num_stop_cols = 3
    threeZeros = np.zeros((stopsTimes.shape[0], num_stop_cols))
    stopsTimes = np.hstack((stopsTimes, threeZeros))
    endCol = stopsTimes.shape[1] - num_stop_cols

    for stop in stopsTimes:
        tripId = stop[0]
        stopId = stop[3 + num_trip_cols]
        if tripId in tripsDict:
            stop[1] = tripsDict[tripId][1] # route_id
            stop[2] = tripsDict[tripId][2] # service_id
            stop[3] = tripsDict[tripId][3] # direction_id
        if stopId in stopsDict:
            stop[endCol] = stopsDict[stopId][1] # stop_name
            stop[endCol + 1] = stopsDict[stopId][2] # latitude
            stop[endCol + 2] = stopsDict[stopId][3] # longitude
    titles = np.array(["trip_id", "route_id", "service_id", "route_direction", "arrival_time",
                       "departure_time", "stop_id", "stop_name", "stop_latitude",
                       "stop_longitude"])
    stopsTimes = np.insert(stopsTimes, 0, titles, axis=0)
    return stopsTimes.astype('str')

if __name__ == '__main__':
    print("Hello world!")
    result = mergeStopTimesAndTrips()
    print(result)
    np.savetxt('schedule.txt', result, '%s', delimiter=",", comments=None)