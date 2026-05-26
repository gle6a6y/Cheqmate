package project.cheqmate.service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Service;

@Service
public class ChequeRecognizeService {

    private static final String API_URL = "https://proverkacheka.com/api/v1/check/get";
    private static final String TOKEN = "38902.zfRxwRAtOWSQDy22v";

    private final HttpClient httpClient = HttpClient.newHttpClient();

    public String callProverkaCheka(String qr) {
        try {
            String jsonBody = String.format(
                    "{\"token\": \"%s\", \"qrraw\": \"%s\"}",
                    TOKEN, qr
            );
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new RuntimeException("API error: " + response.statusCode());
            }

            return response.body();
        } catch (Exception e) {
            throw new RuntimeException("Error calling proverkacheka", e);
        }
    }
}
