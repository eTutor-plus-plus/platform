package at.jku.dke.etutor.service.tasktypes.proxy;

import at.jku.dke.etutor.service.exception.DispatcherRequestFailedException;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;

public abstract sealed class AbstractProxyService permits
    AbstractBpmnDispatcherProxyService,
    AbstractDispatcherProxyService {
    private final String baseUrl;
    protected HttpClient client;

    protected HttpResponse.BodyHandler<String> stringHandler = HttpResponse.BodyHandlers.ofString();

    protected AbstractProxyService(String baseUrl){
        this.baseUrl = baseUrl;
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
    protected String encodeValue(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    /**
     * Utility method that returns a Post-HttpRequest builder
     * @param path the url
     * @param json the body
     * @return the HttpRequest builder
     */
    protected HttpRequest.Builder getPostRequestWithBody(String path, String json){
        return HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + path))
            .POST(HttpRequest.BodyPublishers.ofString(json))
            .setHeader(org.springframework.http.HttpHeaders.CONTENT_TYPE, "application/json");
    }

    /**
     * Utility method that returns a Put-HttpRequest
     * @param path the url
     * @param json the body
     * @return the HttpRequest
     */
    @NotNull
    protected HttpRequest getPutRequestWithBody(String path, String json){
        return HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + path))
            .PUT(HttpRequest.BodyPublishers.ofString(json))
            .setHeader(org.springframework.http.HttpHeaders.CONTENT_TYPE, "application/json")
            .build();
    }

    /**
     * Utility method that returns a GET-HttpRequest
     * @param path the path
     * @return the HttpRequest
     */
    protected HttpRequest getGetRequest(String path){
        return HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + path))
            .GET()
            .build();
    }

    /**
     * Utility method that returns a DELETE-HttpRequest
     * @param path the url
     * @return the HttpRequest
     */
    @NotNull
    protected HttpRequest getDeleteRequest(String path){
        return HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + path))
            .DELETE()
            .build();
    }

    /**
     * Utility method that sends an HttpRequest and returns the response-body wrapped inside an ResponseEntity<T>
     * @param request the HttpRequest
     * @return the ResponseEntity
     */
    protected <T> ResponseEntity<T> getResponseEntity(HttpRequest request, HttpResponse.BodyHandler<T> handler) throws DispatcherRequestFailedException {
        try {
            HttpResponse<T> response = this.client.send(request, handler);
            if (response.statusCode() == 500) throw new DispatcherRequestFailedException(((HttpResponse<String>)response).body());
            return ResponseEntity.status(response.statusCode()).body(response.body());
        } catch (IOException | InterruptedException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
