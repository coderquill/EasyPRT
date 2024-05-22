package edu.cmu.cs.prt;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class WebApiClientTest {
    @Test
    void Test1() {
        HttpResponse<String> response = null;
        try {
            response = WebApiClient.SendGetRequest("api.dictionaryapi.dev", "api/v2/entries/en/hello", new HashMap<>());
        } catch (InterruptedException | IOException e) {
            fail("Unexpected exception " + e.toString());
        }
        assertNotNull(response);
        assertEquals(response.statusCode(), 200);
    }
}