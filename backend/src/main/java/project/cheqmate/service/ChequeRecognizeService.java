package project.cheqmate.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.springframework.stereotype.Service;

@Service
public class ChequeRecognizeService {

    private final HttpClient httpClient = HttpClient.newHttpClient();

    public String callPython(String qr) {
        try {
            String jsonBody = "{\"qr\":\"" + qr + "\"}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://cheque-api:8000/cheque"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            return response.body();
        } catch (Exception e) {
            throw new RuntimeException("Error calling python cheque service", e);
        }
    }
}