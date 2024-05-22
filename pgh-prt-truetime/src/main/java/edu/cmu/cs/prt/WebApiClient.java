package edu.cmu.cs.prt;

import org.apache.http.client.utils.URIBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

/**
 * The WebApiClient is used to send HTTP requests to a web API.
 */
class WebApiClient {
    /**
     * Send a GET request to the specified host at the given path with the query parameters supplied.
     * Example usage:
     * <pre>{@code
     * Map<String, String> queryParams = new HashMap<>();
     * queryParams.put("id", "3");
     * SendGetRequest("my.example.org", "api/v3", queryParams);
     * }</pre>
     * will issue a GET request to "https://my.example.org/api/v3?id=3".
     *
     * @param host the host of the API
     * @param path the path of the API
     * @param queryParams the parameters to pass with the request
     * @return the HTTP response
     * @throws IOException if an I/O error occurs during sending
     * @throws InterruptedException If the send operation is interrupted
     * @throws IllegalArgumentException if the URI could not be created
     */
    static HttpResponse<String> SendGetRequest(String host, String path, Map<String, String> queryParams) throws IllegalArgumentException, InterruptedException, IOException {
        HttpClient client = HttpClient.newHttpClient();
        URIBuilder builder = new URIBuilder()
                .setScheme("https")
                .setHost(host)
                .setPath(path);
        for (Map.Entry<String, String> entry : queryParams.entrySet()) {
            builder.addParameter(entry.getKey(), entry.getValue());
        }
        URI uri = null;
        try {
            uri = builder.build();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("URI could not be created");
        }
        HttpRequest request = HttpRequest.newBuilder(uri).build();
        HttpResponse<String> response;
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response;
    }
}
