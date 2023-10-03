package at.jku.dke.etutor.service.tasktypes.proxy;

import at.jku.dke.etutor.config.ApplicationProperties;
import at.jku.dke.etutor.service.exception.DispatcherRequestFailedException;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
@Service
public class DispatcherProxyService {
    final String dispatcherURL;
    final String bpmnDispatcherURL;
    HttpClient client;

    HttpResponse.BodyHandler<String> stringHandler = HttpResponse.BodyHandlers.ofString();

    public DispatcherProxyService(ApplicationProperties properties){
        this.dispatcherURL = properties.getDispatcher().getUrl();
        this.bpmnDispatcherURL = properties.getBpmnDispatcher().getUrl();
        init();
    }

    private void init(){
        this.client = HttpClient
            .newBuilder()
            .executor(Executors.newFixedThreadPool(20))
            .build();
    }

    /**
     * Encodes a string for URL compatibility
     * @param value the value to encode
     * @return the encoded value
     */
    String encodeValue(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    /**
     * Utility method that returns a Post-HttpRequest
     * @param url the url
     * @param json the body
     * @return the HttpRequest
     */
    HttpRequest.Builder getPostRequestWithBody(String url, String json){
        return HttpRequest.newBuilder()
            .uri(URI.create(url))
            .POST(HttpRequest.BodyPublishers.ofString(json))
            .setHeader(org.springframework.http.HttpHeaders.CONTENT_TYPE, "application/json");
    }

    /**
     * Utility method that returns a Put-HttpRequest
     * @param url the url
     * @param json the body
     * @return the HttpRequest
     */
    @NotNull
    HttpRequest getPutRequestWithBody(String url, String json){
        return HttpRequest.newBuilder()
            .uri(URI.create(url))
            .PUT(HttpRequest.BodyPublishers.ofString(json))
            .setHeader(org.springframework.http.HttpHeaders.CONTENT_TYPE, "application/json")
            .build();
    }

    /**
     * Utility method that returns a GET-HttpRequest
     * @param url the url
     * @return the HttpRequest
     */
    HttpRequest getGetRequest(String url){
        return HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET()
            .build();
    }

    /**
     * Utility method that returns a DELETE-HttpRequest
     * @param url the url
     * @return the HttpRequest
     */
    @NotNull
    HttpRequest getDeleteRequest(String url){
        return HttpRequest.newBuilder()
            .uri(URI.create(url))
            .DELETE()
            .build();
    }

    /**
     * Utility method that sends an HttpRequest and returns the response-body wrapped inside an ResponseEntity<T>
     * @param request the HttpRequest
     * @return the ResponseEntity
     */
    <T> ResponseEntity<T> getResponseEntity(HttpRequest request, HttpResponse.BodyHandler<T> handler) throws DispatcherRequestFailedException {
        try {
            HttpResponse<T> response = this.client.send(request, handler);
            if (response.statusCode() == 500) throw new DispatcherRequestFailedException(((HttpResponse<String>)response).body());
            return ResponseEntity.status(response.statusCode()).body(response.body());
        } catch (IOException | InterruptedException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
