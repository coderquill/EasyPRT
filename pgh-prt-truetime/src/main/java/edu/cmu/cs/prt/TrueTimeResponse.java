package edu.cmu.cs.prt;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

class TrueTimeResponse {
    private final JsonNode response;
    private static final String bustimeResponseKey = "bustime-response";
    TrueTimeResponse(HttpResponse<String> httpResponse) throws JsonProcessingException {
        this.response = parseJson(httpResponse);
    }

    private static JsonNode parseJson(HttpResponse<String> httpResponse) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(httpResponse.body());
        if (node == null) {
            throw new JsonParseException("Unable to find root node");
        }
        if (!node.isObject()) {
            throw new JsonParseException("Root node is not of object type");
        }
        return node;
    }

    static boolean isLegalTrueTimeResponse(HttpResponse<String> response) {
        JsonNode node = null;
        try {
            node = parseJson(response);
        } catch (JsonProcessingException e) {
            return false;
        }
        return node.has(bustimeResponseKey);
    }

    boolean hasError() {
        return response.has(bustimeResponseKey) && response.get(bustimeResponseKey).has("error");
    }

    <T> List<T> convertToList(String fieldName, Class<T> type) throws JsonParseException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode bustimeResponse = response.get(bustimeResponseKey);
        if (!bustimeResponse.has(fieldName)) {
            throw new JsonParseException("Field " + fieldName + " not found");
        }
        JsonNode arrayNode = bustimeResponse.get(fieldName);
        if (!arrayNode.isArray()) {
            throw new JsonParseException("Expected " + fieldName + " to be an array");
        }
        List<T> ret = new ArrayList<>();

        for (JsonNode node : arrayNode) {
            T obj = mapper.convertValue(node, type);
            ret.add(obj);
        }
        return ret;
    }

    JsonNode getField(String fieldName) throws JsonParseException {
        JsonNode bustimeResponse = response.get(bustimeResponseKey);
        if (!bustimeResponse.has(fieldName)) {
            throw new JsonParseException("Field " + fieldName + " not found");
        }
        return bustimeResponse.get(fieldName);
    }
}