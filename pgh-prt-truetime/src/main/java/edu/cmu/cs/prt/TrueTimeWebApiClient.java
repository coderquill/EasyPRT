package edu.cmu.cs.prt;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.http.client.HttpResponseException;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.function.Function;

/**
 * The TrueTimeWebApiClient is an abstraction for the TrueTime web API.
 */
class TrueTimeWebApiClient {
    /** The API server host **/
    private final String apiServerHost;
    /** The API key **/
    private final String key;
    /**
     * The field name of the root field in the JSON response.
     */
    private static final String jsonRootFieldName = "bustime-response";

    /**
     * Error message returned by the web API when there is no data found for the specified parameters.
     */
    private static final String noDataFoundErrorMsg = "No data found for parameter";

    /**
     * Error message returned by the web API when there are no arrival times found when using the predictions endpoint.
     */
    private static final String noArrivalTimesMsg = "No arrival times";

    /**
     * Error message returned by the web API when there is no service for the specified stop when using the predictions
     * endpoint.
     */
    private static final String noServiceScheduled = "No service scheduled";

    enum TrueTimeEndpoint {
        VEHICLES("getvehicles", "vehicle"),
        ROUTES("getroutes", "routes"),
        STOPS("getstops", "stops"),
        PREDICTIONS("getpredictions", "prd"),
        PATTERNS("getpatterns", "ptr");

        private final String endpointPath;

        /**
         * The response for this endpoint is contained in this field of the root element.
         */
        private final String childFieldName;

        TrueTimeEndpoint(String endpointPath, String childFieldName) {
            this.endpointPath = endpointPath;
            this.childFieldName = childFieldName;
        }

        String getEndpointPath() {
            return endpointPath;
        }

        String getChildFieldName() {
            return childFieldName;
        }
    }

    /**
     * Construct an API client that sends request to the TrueTime web API.
     * @param apiServerHost the host of the web API
     * @param key the API key to use
     */
    TrueTimeWebApiClient(String apiServerHost, String key) {
        this.apiServerHost = apiServerHost;
        this.key = key;
    }

    /**
     * Get the default query parameters that should be included in each web API call.
     * @return a key-val mapping representing the query parameters
     */
    private Map<String, String> getDefaultQueryParams() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("format", "json");
        queryParams.put("key", this.key);
        return queryParams;
    }

    /**
     * Call the web API at the specified end point with the given query parameters.
     * @param endpoint the end point to use
     * @param queryParams the query parameters to send
     * @return the HTTP response received from the web API
     * @throws IOException if there was an error sending the HTTP request
     * @throws InterruptedException if sending the HTTP request was interrupted
     */
    private HttpResponse<String> callWebApi(String endpoint, Map<String, String> queryParams) throws IOException, InterruptedException {
        return WebApiClient.SendGetRequest(
                this.apiServerHost,
                "bustime/api/v3/" + endpoint,
                queryParams);
    }

    /**
     * Check that the response received from the web API is as expected. This method checks if the status code of the
     * HTTP request was 2xx, and that it contains the expect root tag of "bustime-response".
     *
     * @param response the HTTP response received from the web API.
     * @throws HttpResponseException if the response status code is non-2xx
     * @throws JsonProcessingException if there was an error parsing the response into JSON
     */
    private void validateApiResponse(HttpResponse<String> response) throws HttpResponseException,
                                     JsonProcessingException, NoDataFoundException {
        int statusCode = response.statusCode();
        String statusCodeString = Integer.toString(statusCode);
        assert !statusCodeString.isEmpty();

        // Check if the response code is not 2xx
        if (statusCodeString.charAt(0) != '2') {
            throw new HttpResponseException(statusCode, "Web API request was not successful");
        }
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(response.body());
        if (rootNode == null) {
            throw new JsonParseException("Unable to find root node");
        }
        if (!rootNode.isObject()) {
            throw new JsonParseException("Root node is not of object type");
        }
        JsonNode responseNode = rootNode.get("bustime-response");
        if (responseNode == null) {
            throw new JsonParseException("Could not find bustime-response field in response");
        }
        JsonNode errorNode = responseNode.get("error");
        if (errorNode != null) {
            String errorMsg = errorNode.get(0).get("msg").asText();
            String exceptionMsg =
                    "Received error from web API: " + errorMsg + ". Web API response:\n" +
                            errorNode.get(0);
            if (errorMsg.startsWith(noDataFoundErrorMsg) || errorMsg.startsWith(noArrivalTimesMsg)
                || errorMsg.startsWith(noServiceScheduled)) {
                throw new NoDataFoundException(exceptionMsg);
            }
            throw new IllegalArgumentException(exceptionMsg);
        }
    }

    /**
     * Get the list of vehicles that match the given route designators.
     * @param routeDesignatorSet the set of route designators
     * @return a list of vehicles corresponding to the given route designators
     * @throws IOException If there is an error sending the HTML request to the web API
     * @throws InterruptedException If the HTTP request tto the web API is interrupted
     */
    List<Vehicle> getVehiclesWithRouteDesignators(Set<String> routeDesignatorSet) throws IOException, InterruptedException, NoDataFoundException {
        Map<String, String> queryParams = getDefaultQueryParams();
        String routeDesignators = String.join(",", routeDesignatorSet);
        queryParams.put("rt", routeDesignators);
        HttpResponse<String> response = callWebApi(TrueTimeEndpoint.VEHICLES.getEndpointPath(), queryParams);
        validateApiResponse(response);

        TrueTimeResponse trueTimeResponse = new TrueTimeResponse(response);
        List<TrueTimeVehicle> trueTimeVehicleList =
                trueTimeResponse.convertToList("vehicle", TrueTimeVehicle.class);

        List<Vehicle> vehicleList = new ArrayList<>();
        for (TrueTimeVehicle vehicle : trueTimeVehicleList) {
            vehicleList.add(Vehicle.of(vehicle));
        }
        return vehicleList;
    }

    /**
     * Get the list of vehicles that match the given vehicle IDs.
     * @param vehicleIdSet the set of vehicle ids to query for
     * @return a list of vehicles corresponding to the given route designators
     * @throws IOException If there is an error sending the HTML request to the web API
     * @throws InterruptedException If the HTTP request tto the web API is interrupted
     */
    List<Vehicle> getVehiclesWithVehicleIds(Set<String> vehicleIdSet) throws IOException, InterruptedException, NoDataFoundException {
        Map<String, String> queryParams = getDefaultQueryParams();
        String vehicleIds = String.join(",", vehicleIdSet);
        queryParams.put("vid", vehicleIds);
        HttpResponse<String> response = callWebApi(TrueTimeEndpoint.VEHICLES.getEndpointPath(), queryParams);

        validateApiResponse(response);

        TrueTimeResponse trueTimeResponse = new TrueTimeResponse(response);
        List<TrueTimeVehicle> trueTimeVehicleList =
                trueTimeResponse.convertToList("vehicle", TrueTimeVehicle.class);

        List<Vehicle> vehicleList = new ArrayList<>();
        for (TrueTimeVehicle vehicle : trueTimeVehicleList) {
            vehicleList.add(Vehicle.of(vehicle));
        }
        return vehicleList;
    }

    private List<TrueTimeStop> parseStopsFromArray(JsonNode arrayNode) throws JsonParseException {
        if (!arrayNode.isArray()) {
            throw new JsonParseException("Expect stop field to be an array");
        }
        ObjectMapper mapper = new ObjectMapper();
        List<TrueTimeStop> matchingStops = new ArrayList<>();
        for (JsonNode node : arrayNode) {
            TrueTimeStop stop = mapper.convertValue(node, TrueTimeStop.class);
            matchingStops.add(stop);
        }
        return matchingStops;
    }

    List<TrueTimeStop> getStopsFromStopIds(Set<String> stopIdSet, VehicleType rtpiDataFeed) throws IOException, InterruptedException, NoDataFoundException {
        Map<String, String> queryParams = getDefaultQueryParams();
        String stopIds = String.join(",", stopIdSet);
        queryParams.put("stpid", stopIds);
        queryParams.put("rtpidatafeed", rtpiDataFeed.getFeedName());
        HttpResponse<String> response = callWebApi("getstops", queryParams);
        validateApiResponse(response);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(response.body());
        JsonNode arrayNode = rootNode.get("bustime-response").get("stops");
        if (arrayNode == null) {
            throw new JsonParseException("Unable to parse route array");
        }
        return parseStopsFromArray(arrayNode);
    }

    List<Stop> getStopsForRoute(String routeID, RouteDirection direction, VehicleType vehicleType)
            throws IOException, InterruptedException, NoDataFoundException {
        Map<String, String> queryParams = getDefaultQueryParams();
        queryParams.put("rt", routeID);
        queryParams.put("dir", direction.name());
        queryParams.put("rtpidatafeed", vehicleType.getFeedName());
        Function<TrueTimeStop, Stop> converter = new Function<>() {
            @Override
            public Stop apply(TrueTimeStop stop) {
                return Stop.createFrom(stop);
            }
        };
        try {
            return getPrtObjectsFromAPIEndpoint(queryParams, TrueTimeEndpoint.STOPS, TrueTimeStop.class, converter);
        } catch (NoDataFoundException e) {
            return Collections.emptyList();
        }
    }

    /**
     * Returns Prt objects from the specified endpoint with the given query parameters.
     * @param queryParams the query parameters to pass to the web API
     * @param trueTimeEndpoint the endpoint to use the web API call
     * @param trueTimeObjectClass the class that represents the objects returned by the web API
     * @param converter a converter to convert the objects returned by the web API to Prt objects
     * @return a list of Prt objects obtained from the web API call
     * @param <T> a class that represents the objects returned by the web API
     * @param <R> a Prt class that is exposed to the user
     * @throws IOException if the HTTP request fails
     * @throws InterruptedException if the HTTP request is interrupted
     */
    private <T, R> List<R> getPrtObjectsFromAPIEndpoint(Map<String, String> queryParams,
                                                        TrueTimeEndpoint trueTimeEndpoint,
                                                        Class<T> trueTimeObjectClass,
                                                        Function<T, R> converter)
            throws IOException, InterruptedException, NoDataFoundException {
        HttpResponse<String> response = callWebApi(trueTimeEndpoint.getEndpointPath(), queryParams);
        validateApiResponse(response);
        TrueTimeResponse trueTimeResponse = new TrueTimeResponse(response);
        List<T> trueTimeObjectList =
                trueTimeResponse.convertToList(trueTimeEndpoint.getChildFieldName(), trueTimeObjectClass);
        List<R> prtObjectList = new ArrayList<>();
        for (T obj : trueTimeObjectList) {
            prtObjectList.add(converter.apply(obj));
        }
        return prtObjectList;
    }

    /**
     * Get arrival predictions for the specified routes and stops.
     * @param stopIds the identifiers of the stops to find arrival predictions for
     * @param routeIds the identifiers of the routes to find arrival predictions for
     * @param vehicleType the vehicle type to find arrival predictions for
     * @return the arrival predictions for the specified parameters
     * @throws IOException if the HTTP request to the web API fails
     * @throws InterruptedException if the HTTP request to the web API is interrupted
     */
    List<PredictedArrival> getPredictionsForRoutesAtStops(Set<String> stopIds,
                                                          Set<String> routeIds,
                                                          VehicleType vehicleType) throws IOException, InterruptedException {
        if (stopIds.isEmpty() && routeIds.isEmpty()) {
            return Collections.emptyList();
        }
        Map<String, String> queryParams = getDefaultQueryParams();
        String commaSepRouteIds = String.join(",", routeIds);
        String commaSepStopIds = String.join(",", stopIds);
        queryParams.put("rt", commaSepRouteIds);
        queryParams.put("stpid", commaSepStopIds);
        queryParams.put("rtpidatafeed", vehicleType.getFeedName());

        Function<TrueTimePrediction, PredictedArrival> converter = new Function<>() {
            @Override
            public PredictedArrival apply(TrueTimePrediction prediction) {
                return PredictedArrival.of(prediction);
            }
        };
        try {
            return getPrtObjectsFromAPIEndpoint(queryParams, TrueTimeEndpoint.PREDICTIONS, TrueTimePrediction.class, converter);
        } catch (NoDataFoundException e) {
            return Collections.emptyList();
        }
    }

    /**
     * Get arrival predictions for the specified vehicles.
     * @param vehicleIds the identifiers of the vehicle to find arrival predictions for
     * @param vehicleType the vehicle type to find arrival predictions for
     * @return arrival predictions for the specified vehicles
     * @throws IOException if the HTTP request to the web API fails
     * @throws InterruptedException if the HTTP request to the web API is interrupted
     */
    List<PredictedArrival> getPredictionsForVehicles(Set<String> vehicleIds,
                                                     VehicleType vehicleType) throws IOException, InterruptedException {
        Map<String, String> queryParams = getDefaultQueryParams();
        String commaSepVehicleIds = String.join(",", vehicleIds);
        queryParams.put("vid", commaSepVehicleIds);
        queryParams.put("rtpidatafeed", vehicleType.getFeedName());

        Function<TrueTimePrediction, PredictedArrival> converter = new Function<>() {
            @Override
            public PredictedArrival apply(TrueTimePrediction prediction) {
                return PredictedArrival.of(prediction);
            }
        };
        try {
            return getPrtObjectsFromAPIEndpoint(queryParams, TrueTimeEndpoint.PREDICTIONS, TrueTimePrediction.class, converter);
        } catch (NoDataFoundException e) {
            return Collections.emptyList();
        }
    }

    List<TrueTimePattern> getPatternsForRoute(String routeId, VehicleType vehicleType)
            throws IOException, InterruptedException, NoDataFoundException {
        Map<String, String> queryParams = getDefaultQueryParams();
        queryParams.put("rt", routeId);
        queryParams.put("rtpidatafeed", vehicleType.getFeedName());

        HttpResponse<String> response = callWebApi(TrueTimeEndpoint.PATTERNS.getEndpointPath(), queryParams);
        validateApiResponse(response);
        TrueTimeResponse trueTimeResponse = new TrueTimeResponse(response);
        JsonNode ptrNode = trueTimeResponse.getField(TrueTimeEndpoint.PATTERNS.getChildFieldName());
        if (!ptrNode.isArray()) {
            throw new JsonParseException("Expected " + TrueTimeEndpoint.PATTERNS.getChildFieldName() + " to be an array");
        }
        List<TrueTimePattern> trueTimePatterns = new ArrayList<>();
        for (JsonNode pattern : ptrNode) {
            if (!pattern.has("pt")) {
                throw new JsonParseException("Expected pt field in ptr list");
            }
            JsonNode ptNode = pattern.get("pt");
            if (!ptNode.isArray()) {
                throw new JsonParseException("Expected pt field to be an array");
            }
            List<TrueTimeWaypoint> waypoints = new ArrayList<>();
            for (JsonNode waypoint : ptNode) {
                TrueTimeWaypoint trueTimeWaypoint = new ObjectMapper().treeToValue(waypoint, TrueTimeWaypoint.class);
                waypoints.add(trueTimeWaypoint);
            }
            JsonNode idNode = pattern.get("pid");
            if (!idNode.isValueNode()) {
                throw new JsonParseException("Expected pid to be a value node");
            }
            JsonNode lengthNode = pattern.get("ln");
            if (!lengthNode.isValueNode()) {
                throw new JsonParseException("Expected ln to be a value node");
            }
            JsonNode rtDirNode = pattern.get("rtdir");
            if (!rtDirNode.isValueNode()) {
                throw new JsonParseException("Expected rtdir to be a value node");
            }
            RouteDirection dir = RouteDirection.valueOf(rtDirNode.asText());
            trueTimePatterns.add(new TrueTimePattern(idNode.asText(), lengthNode.asDouble(), dir, waypoints));
        }
        return trueTimePatterns;
    }
}